package com.example.coronadiagnosticapp.ui.fragments.oxymeter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.hardware.*
import android.hardware.Camera.PreviewCallback
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_oxymeter.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class OxymeterFragment : Fragment() {
    private var readyBtn: Button? = null

    //ProgressBar
    private var mSeries: LineGraphSeries<DataPoint>? = null
    private var makeVertical: RotateAnimation? = null

    //TextView
    private var putFingerAlert: TextView? = null
    private var improveLightningAlert: TextView? = null

    // This value actually stores FPS * 1000 (because that's how the `Camera` module handles it's data).
    private var previewFps = -1
    private var previewSize: Camera.Size? = null
    private var framesQueue: Queue<ByteArray>? = null
    private var oxymeterUpdater: OxymeterThread? = null
    private var currentHeartRate = 0

    @JvmField
    @Inject
    var viewModel: OxymeterViewModel? = null
    private val previewCallback = PreviewCallback { data, camera -> // if we have started recording
        if (framesQueue != null) {
            framesQueue!!.add(data)
        }
    }
    private val surfaceCallback: SurfaceHolder.Callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            try {
                camera!!.setPreviewDisplay(previewHolder)
                camera!!.setPreviewCallback(previewCallback)
            } catch (t: Throwable) {
                Log.e(
                    TAG,
                    "Exception in setPreviewDisplay()",
                    t
                )
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_oxymeter, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val app = context!!.applicationContext as MyApplication
        app.getAppComponent().inject(this)
        activity!!.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // XML - Java Connecting

        putFingerAlert = view.findViewById(R.id.putFingerMessage)
        improveLightningAlert = view.findViewById(R.id.improve_lightning)
        previewHolder = preview.holder
        previewHolder?.let {
            it.addCallback(surfaceCallback)
            it.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        }

        //Button
        readyBtn = view.findViewById(R.id.ready_btn)
        progressBarView = view.findViewById(R.id.barTimer)
        timeLeftView = view.findViewById(R.id.textTimer)
        heartRate = view.findViewById(R.id.heartRate)
        graphHeartRate = view.findViewById(R.id.graphHeartRate)
        mSeries = LineGraphSeries()
        mSeries!!.color = Color.WHITE
        graphHeartRate.removeAllSeries()
        graphHeartRate.addSeries(mSeries)
        graphHeartRate.getViewport().isXAxisBoundsManual = true
        graphHeartRate.getViewport().setMinX(0.0)
        graphHeartRate.getViewport().setMaxX(DATA_POINTS.toDouble())
        graphHeartRate.getGridLabelRenderer().isVerticalLabelsVisible = false
        graphHeartRate.getGridLabelRenderer().isHorizontalLabelsVisible = false
        graphHeartRate.getGridLabelRenderer().gridStyle = GridLabelRenderer.GridStyle.NONE
        graphHeartRate.setBackgroundColor(Color.rgb(0x62, 0x00, 0xEE))
        /*Animation*/makeVertical = RotateAnimation(
            0,
            -90,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        makeVertical!!.fillAfter = true
        progressBarView.startAnimation(makeVertical)
        progressBarView.setProgress(0)
        readyBtn.setOnClickListener(View.OnClickListener { v: View? ->
            Log.i(TAG, "Pressed start oxymeter button.")
            initializeOxymeterUpdater()
            readyBtn.setClickable(false)
        })
        val mySensorManager =
            context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val lightSensor =
            mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        if (lightSensor != null) {
            mySensorManager.registerListener(
                lightSensorListener,
                lightSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        } else {
            Log.e(TAG, "Couldn't find light sensor.")
        }
    }

    private val lightSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(
            sensor: Sensor,
            accuracy: Int
        ) {
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                Log.d(
                    TAG,
                    "light sensor value:" + event.values[0]
                )
                if (event.values[0] < MIN_LIGHT_VALUE) {
                    improveLightningAlert!!.visibility = View.VISIBLE
                    lightningImageView!!.visibility = View.VISIBLE
                } else {
                    improveLightningAlert!!.visibility = View.INVISIBLE
                    lightningImageView!!.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun initializeOxymeterUpdater() {
        framesQueue = LinkedList()
        val totalFrames = 900
        oxymeterUpdater = OxymeterThread(framesQueue,
            camera,
            previewSize,
            totalFrames,
            previewFps.toDouble(),
            { heartRate: Int ->
                updateView(heartRate)
                null
            },
            { frame: Int, point: Double ->
                updateGraphView(frame, point)
                null
            }
            ,
            object : OxymeterThreadEventListener {
                override fun onFrame(frameNumber: Int) {
                    Log.i(
                        TAG,
                        "Current frame:$frameNumber"
                    )
                    activity!!.runOnUiThread {
                        setProgress(
                            frameNumber,
                            totalFrames
                        )
                    }
                }

                override fun onSuccess(oxymeter: Oxymeter) {
                    Log.i(
                        TAG,
                        "finished processing all frames"
                    )
                    finishWithOxymeter(oxymeter)
                }

                override fun onFingerRemoved() {
                    fingerRemoved()
                }

                override fun onInvalidData() {
                    Log.w(TAG, "Invalid measurement")
                    measurementFailed()
                }

                override fun onStartWithNewOxymeter() {
                    activity!!.runOnUiThread {
                        showProgressBarAndShowAlert(
                            getString(R.string.things_look_ok)
                        )
                    }
                }
            })
        Log.i(TAG, "starting oxymeter.")
        oxymeterUpdater!!.start()
    }

    fun finishWithOxymeter(oxymeter: Oxymeter) {
        val result = oxymeter.finish(previewFps / 1000.0)
        if (result != null) {
            Log.i(TAG, "Oxymeter finished successfully!")
            submitMeasurement(oxymeter)
            val returnIntent = Intent()
            returnIntent.putExtra(
                "OXYGEN_SATURATION",
                Integer.toString(result.oxSaturation)
            )
            returnIntent.putExtra("BEATS_PER_MINUTE", Integer.toString(result.heartRate))
            returnIntent.putExtra(
                "BREATHS_PER_MINUTE",
                Integer.toString(result.breathRate)
            )
            //            setResult(Activity.RESULT_OK, returnIntent);
//            finish();
            // TODO: 07/05/2020 convert to nav
        } else {
            Log.w(TAG, "Oxymeter returned null")
            measurementFailed()
        }
    }

    fun submitMeasurement(oxymeter: Oxymeter) {
        Log.i(TAG, "Got camera permissions.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val cameraManager =
                context!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            try {
                val cameraCharacteristics =
                    cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[0])
                viewModel!!.submitPpgMeasurement(oxymeter.getAverages(), cameraCharacteristics)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        } else {
            Log.w(
                TAG,
                "Android API doesn't support camera2, not sending camera characteristics."
            )
        }
    }

    fun fingerRemoved() {
        Log.w(TAG, "Finger not recognised!")
        activity!!.runOnUiThread { removeProgressBarAndShowAlert(getString(R.string.please_put_your_finger_on_camera)) }
    }

    fun measurementFailed() {
        activity!!.runOnUiThread {
            removeProgressBarAndShowAlert(getString(R.string.measurement_failed))
            readyBtn!!.isClickable = true
        }
    }

    fun updateView(heartRate: Int) {
        currentHeartRate = heartRate
        activity!!.runOnUiThread { updateMeasurements() }
    }

    private fun updateMeasurements() {
        heartRate!!.text = Integer.toString(currentHeartRate)
    }

    private fun updateGraphView(frame: Int, point: Double) {
        activity!!.runOnUiThread { updateGraph(frame, point) }
    }

    private fun updateGraph(frame: Int, point: Double) {
        mSeries!!.appendData(
            DataPoint(frame, point),
            true,
            DATA_POINTS,
            false
        )
        graphHeartRate!!.onDataChanged(false, false)
    }

    fun setProgress(currentFrame: Int, totalFrames: Int) {
        progressBarView!!.max = totalFrames
        progressBarView!!.secondaryProgress = totalFrames
        progressBarView!!.progress = currentFrame
        val secondsLeft =
            (totalFrames - currentFrame) / (previewFps.toDouble() / 1000)
        timeLeftView!!.text = String.format("%s", secondsLeft.toInt())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    //Wakelock + Open device camera + set orientation to 90 degree
    //store system time as a start time for the analyzing process
    //your activity to start interacting with the user.
    // This is a good place to begin animations, open exclusive-access devices (such as the camera)
    override fun onResume() {
        super.onResume()
        camera = Camera.open()
        camera.setDisplayOrientation(90)
    }

    //call back the frames then release the camera + wakelock and Initialize the camera to null
    //Called as part of the activity lifecycle when an activity is going into the background, but has not (yet) been killed. The counterpart to onResume().
    //When activity B is launched in front of activity A,
    // this callback will be invoked on A. B will not be created until A's onPause() returns, so be sure to not do anything lengthy here.
    override fun onPause() {
        super.onPause()
        camera!!.setPreviewCallback(null)
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    override fun onStop() {
        super.onStop()
        if (oxymeterUpdater != null) {
            oxymeterUpdater!!.interrupt()
        }
    }

    private fun removeProgressBarAndShowAlert(alertText: String) {
        tickImageView!!.setImageDrawable(resources.getDrawable(R.drawable.ic_warning))
        putFingerAlert!!.text = alertText
        progressBarView!!.clearAnimation()
        progressBarView!!.visibility = View.INVISIBLE
        timeLeftView!!.visibility = View.INVISIBLE
        mSeries!!.resetData(arrayOf())
        heartRate!!.text = "-"
    }

    private fun showProgressBarAndShowAlert(alertText: String) {
        tickImageView!!.setImageDrawable(resources.getDrawable(R.drawable.ic_tick))
        putFingerAlert!!.text = alertText
        if (progressBarView!!.visibility != View.VISIBLE) {
            progressBarView!!.startAnimation(makeVertical)
            progressBarView!!.visibility = View.VISIBLE
            timeLeftView!!.visibility = View.VISIBLE
        }
    }

    companion object {
        // Variables Initialization
        private const val TAG = "HeartRateMonitor"
        private val processing =
            AtomicBoolean(false)
        private const val MIN_LIGHT_VALUE = 170 // lux units.
        private const val DATA_POINTS = 200
        private var previewHolder: SurfaceHolder? = null
        private var camera: Camera? = null

        //Freq + timer variable
        private const val startTime: Long = 0
        private fun getSmallestPreviewSize(
            width: Int,
            height: Int,
            parameters: Camera.Parameters
        ): Camera.Size? {
            var result: Camera.Size? = null
            for (size in parameters.supportedPreviewSizes) {
                if (size.width <= width && size.height <= height) {
                    if (result == null) {
                        result = size
                    } else {
                        val resultArea = result.width * result.height
                        val newArea = size.width * size.height
                        if (newArea < resultArea) result = size
                    }
                }
            }
            return result
        }
    }
}