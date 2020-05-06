package com.example.coronadiagnosticapp.ui.fragments.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import be.tarsos.dsp.io.PipedAudioStream
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.audioAnalyzer.AudioAnalyzerImpl
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.recorder_fragment2.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class RecorderFragment2 : ScopedFragment() {
    companion object {
        private const val TAG = "RecorderFragment2"
        private const val RECORDER_PERMISSION = Manifest.permission.RECORD_AUDIO
        private const val PERMISSION_CODE: Int = 21
        private const val MAX_DURATION = 1000 * 10 // in milliseconds
        private const val VISUALIZATION_FREQUENCY: Long = 30
    }

    @Inject
    lateinit var viewModel: RecorderViewModel

    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String? = null
    private var fileLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
        recordFile = context!!.externalCacheDir!!.absolutePath
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val stepperIndicator = view.findViewById<StepperIndicator>(R.id.stepperIndicator)
        stepperIndicator?.currentStep = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recorder_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Use viewModel
        initButton()
    }

    private fun initButton() {
        record_btn.setOnClickListener {
            // The button can only start the recording
            if (!isRecording) {
                // Check permission
                if (checkPermissions()) {
                    startRecording()
                    record_btn.apply {
                        setImageResource(R.drawable.mic_button_recording)
                        isEnabled = false
                    }
                }
            }
        }
    }

    private fun stopRecording() {
        isRecording = false

        //Stop Timer, very obvious
        record_timer.stop()

        //Change text on page to file saved
        record_filename.text = "Recording Stopped, File Saved : $recordFile"

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        mediaRecorder!!.release()
        mediaRecorder = null
        Log.i("ASQWEQWE", "asd")
        processRecording()
    }

    private fun startRecording() {
        isRecording = true

        //Start timer from 0
        record_timer.base = SystemClock.elapsedRealtime()
        record_timer.start()

        //Get app external directory path
        val recordPath = activity!!.getExternalFilesDir("/")!!.absolutePath

        //Get current date and time as string
        val now = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA).format(Date())

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_$now.m4a"
        fileLocation = "$recordPath/$recordFile"

        //Setup Media Recorder for recording
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder!!.setOutputFile(fileLocation)
        launch {
            delay(MAX_DURATION.toLong())
            stopRecording()
            showLoading(true)
            // Upload file
            launch(Dispatchers.IO) {
                viewModel.uploadFile(File(fileLocation!!))
                withContext(Dispatchers.Main) { showLoading(false) }
                Log.d(TAG, "File finished uploading!")
                findNavController().navigate(R.id.action_recorderFragment_to_resultFragment)
            }
            processRecording()
        }

        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
        startUpdatingVisualizer()
    }

    private fun startUpdatingVisualizer() {
        launch {
            while (isRecording) {
                visualizer.addAmplitude(mediaRecorder!!.maxAmplitude.toFloat())
                visualizer.invalidate()
                delay(VISUALIZATION_FREQUENCY)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_recordFragment.visibility = View.VISIBLE
            false -> progressBar_recordFragment.visibility = View.GONE
        }
    }

    private fun checkPermissions(): Boolean { // Check permission
        return if (ActivityCompat.checkSelfPermission(
                context!!,
                RECORDER_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED
        ) { // Permission Granted
            true
        } else { // Permission not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(RECORDER_PERMISSION),
                PERMISSION_CODE
            )
            false
        }
    }

    override fun onStop() {
        super.onStop()
        // Stop recording is someone presses back
        if (isRecording) { stopRecording() }
    }


    // Extracts the breathing rate from the recording
    private fun processRecording() {
        AndroidFFMPEGLocator(this.context)
        Log.i(TAG, "Starting to process recording")
        val audioStream = PipedAudioStream(fileLocation).getMonoStream(44100, 0.0)
        val breathingRate = AudioAnalyzerImpl().breathingRateFromAudioStream(audioStream)
        Log.i(TAG, "Breathing rate: $breathingRate")
        viewModel.setBreathingRate(breathingRate)
    }


}
