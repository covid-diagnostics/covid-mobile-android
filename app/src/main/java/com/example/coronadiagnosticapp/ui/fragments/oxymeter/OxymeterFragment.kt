package com.example.coronadiagnosticapp.ui.fragments.oxymeter

import android.content.Context
import android.graphics.Color
import android.hardware.*
import android.hardware.Camera.PreviewCallback
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.RotateAnimation
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_oxymeter.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class OxymeterFragment : Fragment(), SurfaceHolder.Callback, SensorEventListener {
    //ProgressBar
    private var mSeries: LineGraphSeries<DataPoint>? = null
    private var makeVertical: RotateAnimation? = null

    // This value actually stores FPS * 1000 (because that's how the `Camera` module handles it's data).
    private var previewFps = -1
    private var previewSize: Camera.Size? = null
    private var framesQueue: Queue<ByteArray>? = null
    private var oxymeterUpdater: OxymeterThread? = null
    private var currentHeartRate = 0

    private var previewHolder: SurfaceHolder? = null
    private var camera: Camera? = null

    @Inject
    lateinit var viewModel: OxymeterViewModel

    private val previewCallback = PreviewCallback { data, _ -> // if we have started recording
        framesQueue?.add(data)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_oxymeter, container, false)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        context?.getAppComponent()?.inject(this)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        previewHolder = preview.holder
        previewHolder?.let {
            it.addCallback(this)
            it.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        mSeries = LineGraphSeries()
        mSeries!!.color = Color.WHITE
        graphHeartRate.apply {
            removeAllSeries()
            addSeries(mSeries)
            with(viewport) {
                isXAxisBoundsManual = true
                setMinX(0.0)
                setMaxX(DATA_POINTS.toDouble())
            }

            with(gridLabelRenderer) {
                isVerticalLabelsVisible = false
                isHorizontalLabelsVisible = false
                gridStyle = GridLabelRenderer.GridStyle.NONE
            }
            val color = context!!.resources.getColor(R.color.colorPrimary)
            setBackgroundColor(color)
        }

        /*Animation*/
        makeVertical = RotateAnimation(
            0f, -90f,
            RELATIVE_TO_SELF, 0.5f,
            RELATIVE_TO_SELF, 0.5f
        )
        makeVertical!!.fillAfter = true
        barTimer.startAnimation(makeVertical)
        barTimer.progress = 0
        ready_btn.setOnClickListener {
            Log.i(TAG, "Pressed start oxymeter button.")
            initializeOxymeterUpdater()
            it.isClickable = false
        }
        val mySensorManager =
            context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor =
            mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            mySensorManager.registerListener(
                this,
                lightSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } else {
            Log.e(TAG, "Couldn't find light sensor.")
        }
    }

    private fun initializeOxymeterUpdater() {
        framesQueue = LinkedList()
        val totalFrames = 900
        oxymeterUpdater = OxymeterThread(framesQueue,
            camera,
            previewSize,
            totalFrames,
            previewFps.toDouble(), this::updateView,
            { frame, point ->
                updateGraphView(frame, point)
            }
            ,
            object : OxymeterThreadEventListener {
                override fun onFrame(frameNumber: Int) {
                    Log.i(TAG, "Current frame:$frameNumber")
                    GlobalScope.launch(Main) {
                        setProgress(frameNumber, totalFrames)
                    }
                }

                override fun onSuccess(oxymeter: Oxymeter) {
                    Log.i(TAG, "finished processing all frames")
                    finishWithOxymeter(oxymeter)
                }

                override fun onFingerRemoved() = fingerRemoved()

                override fun onInvalidData() {
                    Log.w(TAG, "Invalid measurement")
                    measurementFailed()
                }

                override fun onStartWithNewOxymeter() {
                    GlobalScope.launch(Main) {
                        showProgressBarAndShowAlert(getString(R.string.things_look_ok))
                    }
                }
            })
        Log.i(TAG, "starting oxymeter.")
        oxymeterUpdater!!.start()
    }

    fun finishWithOxymeter(oxymeter: Oxymeter) {
        val oxyData = oxymeter.finish(previewFps / 1000.0)
        if (oxyData == null) {
            Log.w(TAG, "Oxymeter returned null")
            measurementFailed()
            return
        }

        Log.i(TAG, "Oxymeter finished successfully!")
        submitMeasurement(oxymeter)
        val dataBundle = bundleOf(EXTRA_OXY_DATA to oxyData)
        findNavController().navigate(
            R.id.action_oxymeterFragment_to_cameraFragment,
            dataBundle
        )
    }

    private fun submitMeasurement(oxymeter: Oxymeter) {
        Log.i(TAG, "Got camera permissions.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager =
                context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                val cameraCharacteristics =
                    cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[0])
                viewModel.submitPpgMeasurement(oxymeter.getAverages(), cameraCharacteristics)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        } else {
            Log.w(TAG, "Android API doesn't support camera2, not sending camera characteristics.")
        }
    }

    private fun fingerRemoved() {
        Log.w(TAG, "Finger not recognised!")
        GlobalScope.launch(Main) { removeProgressBarAndShowAlert(getString(R.string.please_put_your_finger_on_camera)) }
    }

    private fun measurementFailed() {
        GlobalScope.launch(Main) {
            removeProgressBarAndShowAlert(getString(R.string.measurement_failed))
            ready_btn.isClickable = true
        }
    }

    private fun updateView(heartRate: Int) {
        currentHeartRate = heartRate
        GlobalScope.launch(Main) { updateMeasurements() }
    }

    private fun updateMeasurements() {
        heartRate.text = currentHeartRate.toString()
    }

    private fun updateGraphView(frame: Int, point: Double) {
        GlobalScope.launch(Main) { updateGraph(frame, point) }
    }

    private fun updateGraph(frame: Int, point: Double) {
        mSeries?.appendData(
            DataPoint(frame.toDouble(), point),
            true,
            DATA_POINTS,
            false
        )
        graphHeartRate.onDataChanged(false, false)
    }

    fun setProgress(currentFrame: Int, totalFrames: Int) {
        with(barTimer) {
            max = totalFrames
            secondaryProgress = totalFrames
            progress = currentFrame
        }
        val secondsLeft =
            (totalFrames - currentFrame) / (previewFps / 1000.0)
        textTimer.text = String.format("%s", secondsLeft.toInt())
    }

    //Wakelock + Open device camera + set orientation to 90 degree
    //store system time as a start time for the analyzing process
    //your activity to start interacting with the user.
    // This is a good place to begin animations, open exclusive-access devices (such as the camera)
    override fun onResume() {
        super.onResume()
        Camera.open().also {
            camera = it
            it.setDisplayOrientation(90)
        }
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    // this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    override fun onPause() {
        super.onPause()
        camera?.let {
            it.setPreviewCallback(null)
            it.stopPreview()
            it.release()
        }
        camera = null
    }

    override fun onStop() {
        super.onStop()
        oxymeterUpdater?.interrupt()
    }

    private fun removeProgressBarAndShowAlert(alertText: String) {
        fingerTickImage.setImageDrawable(resources.getDrawable(R.drawable.ic_warning))
        putFingerMessage.text = alertText
        barTimer.clearAnimation()
        barTimer.visibility = View.INVISIBLE
        textTimer.visibility = View.INVISIBLE
        mSeries?.resetData(emptyArray())
        heartRate.text = "-"
    }

    private fun showProgressBarAndShowAlert(alertText: String) {
        //lightningTickImage.setImageDrawable(resources.getDrawable(R.drawable.ic_tick))
        putFingerMessage.text = alertText
        if (barTimer.visibility != View.VISIBLE) {
            barTimer.startAnimation(makeVertical)
            barTimer.visibility = View.VISIBLE
            textTimer.visibility = View.VISIBLE
        }
    }

    companion object {
        // Variables Initialization
        private const val TAG = "HeartRateMonitor"
        private const val MIN_LIGHT_VALUE = 170 // lux units.
        private const val DATA_POINTS = 200
        const val EXTRA_OXY_DATA = "EXTRA_OXYMETER_DATA"


        private fun getSmallestPreviewSize(
            width: Int,
            height: Int,
            parameters: Camera.Parameters
        ): Camera.Size? {
            var result: Camera.Size? = null
            for (size in parameters.supportedPreviewSizes) {
                if (size.width > width || size.height > height)
                    continue

                if (result == null) {
                    result = size
                    continue
                }
                val resultArea = result.width * result.height
                val newArea = size.width * size.height
                if (newArea < resultArea)
                    result = size
            }
            return result
        }
    }

    //    Surface Listener impl
    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera!!.setPreviewDisplay(previewHolder)
            camera!!.setPreviewCallback(previewCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in setPreviewDisplay()", e)
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        Log.e("surfaceChanged:", "OK")
        val parameters =
            camera!!.parameters
        parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
        previewSize =
            getSmallestPreviewSize(width, height, parameters)
        parameters.setPreviewSize(previewSize!!.width, previewSize!!.height)
        Log.d(
            TAG,
            "Using width=" + previewSize!!.width + " height=" + previewSize!!.height
        )

        // Finds the fastest stable fps the preview can support.
        val fpsRanges =
            parameters.supportedPreviewFpsRange
        Log.i(TAG, "Available preview fps ranges:")
        for (range in fpsRanges) {
            Log.i(
                TAG,
                "Range: " + range[0] + " - " + range[1]
            )
            // The fps range should be stable (min fps equals max fps)
            if (range[0] == range[1] && range[0] > previewFps) {
                previewFps = range[0]
            }
        }
        if (previewFps == -1) {
            throw RuntimeException("Could not find any stable fps range")
        }
        if (previewFps % 1000 != 0) {
            Log.w(
                TAG,
                "Preview FPS is not a whole number"
            )
        }
        Log.i(
            TAG,
            "Preview running on $previewFps FPS"
        )
        parameters.setPreviewFpsRange(previewFps, previewFps)
        camera!!.parameters = parameters
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Ignore
    }

//    Lighting Event Sensor impl

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            Log.d(TAG, "light sensor value:${event.values[0]}")
            val visibility = if (event.values[0] < MIN_LIGHT_VALUE) View.VISIBLE else View.GONE
            //improve_lightning?.visibility = visibility
            //lightningTickImage?.visibility = visibility
        }
    }
}