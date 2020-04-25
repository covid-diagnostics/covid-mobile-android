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
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.audioAnalyzer.AudioAnalyzerImpl
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.recorder_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class RecorderFragment : ScopedFragment() {
    companion object {
        private const val TAG = "RecorderFragment"
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
        context?.getAppComponent()?.inject(this)

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
    ): View? = inflater.inflate(R.layout.recorder_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Use viewModel
        initButton()
    }

    private fun initButton() {
        record_btn.setOnClickListener {
            // The button can only start the recording
            if (!isRecording && checkPermissions()) {
                startRecording()

                record_btn.setImageResource(R.drawable.mic_button_recording)
                record_btn.isEnabled = false
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
        mediaRecorder!!.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
        Log.i(TAG, "stop recording")
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
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(fileLocation)
        }
        launch {
            delay(MAX_DURATION.toLong())
            stopRecording()

            val progress = progressBar_recordFragment

            showLoading(progress, true)
            // Upload file
            launch(Dispatchers.IO) {
                viewModel.uploadFile(File(fileLocation!!))
                withContext(Dispatchers.Main) { showLoading(progress, false) }
                Log.d(TAG, "File finished uploading!")
                findNavController().navigate(R.id.action_recorderFragment_to_resultFragment)
            }
            processRecording()
        }

        mediaRecorder!!.apply {
            prepare()//TODO use prepare async + listener
            start()//TODO call start when prepared
        }
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
        if (isRecording) stopRecording()
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
