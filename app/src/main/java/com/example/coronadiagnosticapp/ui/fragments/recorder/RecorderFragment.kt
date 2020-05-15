package com.example.coronadiagnosticapp.ui.fragments.recorder

import android.Manifest.permission.RECORD_AUDIO
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import be.tarsos.dsp.io.PipedAudioStream
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.MainActivity
import com.example.coronadiagnosticapp.ui.audioAnalyzer.AudioAnalyzerImpl
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.recorder_fragment.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RecorderFragment() : ScopedFragment() {
    companion object {
        private const val TAG = "RecorderFragment"
        private const val PERMISSION_CODE: Int = 21
        private const val MAX_DURATION: Long = 1000 * 10 // in milliseconds
        private const val VISUALIZATION_FREQUENCY: Long = 30
    }

    @Inject
    lateinit var viewModel: RecorderViewModel

    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String? = null
    private var fileLocation: String? = null
    private var recordingCallback:Callback? = null

    constructor(recordingCallback: Callback) : this() {
        this.recordingCallback = recordingCallback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity!!.getAppComponent().inject(this)

        recordFile = context!!.externalCacheDir!!.absolutePath
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.recorder_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (context as? MainActivity)?.setStepperCount(2)
        resetUI()
        initButton()
    }

    fun resetUI() {
        visualizerView.recreate()
        record_btn.setImageResource(R.drawable.mic_button)
        record_btn.isEnabled = true
        isRecording = false
    }

    private fun initButton() {
        record_btn.setOnClickListener {
            // The button can only start the recording
            if (!isRecording && checkPermissions()) {
                startRecording()
                record_btn.apply {
                    setImageResource(R.drawable.mic_button_recording)
                    isEnabled = false
                }
                tap_to_record_tv.visibility = View.INVISIBLE
            }
        }
    }

    private fun stopRecording() {
        isRecording = false

        tap_to_record_tv.visibility = View.VISIBLE

        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
        Log.i("ASQWEQWE", "asd")
        processRecording()
    }

    private fun startRecording() {
        isRecording = true

        //Get app external directory path
        val recordPath = context!!.getExternalFilesDir("/")!!.absolutePath

        //Get current date and time as string
        val formatter = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault())
        val now = formatter.format(Date())

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        recordFile = "Recording_$now.m4a"
        fileLocation = "$recordPath/$recordFile"

        //Setup Media Recorder for recording
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(fileLocation)

            prepare()
            start()
        }
        startUpdatingVisualizer()

        launch {
            delay(MAX_DURATION)
            if (isVisible.not()) return@launch
            // this thread will stay alive after the page is dead, so this is to avoid null reference
            stopRecording()
            // Upload file
            recordingCallback?.onRecordingFinished(fileLocation!!)
        }
    }

    private fun startUpdatingVisualizer() {
        launch {
            while (isRecording) {
                val amplitude = mediaRecorder!!.maxAmplitude
                visualizerView.update(amplitude * 4)//for more visualization
                delay(VISUALIZATION_FREQUENCY)
            }
        }
    }

    private fun checkPermissions(): Boolean { // Check permission
        // Permission Granted
        if (ActivityCompat.checkSelfPermission(context!!, RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        )
            return true

        // Permission not granted, ask for permission
        ActivityCompat.requestPermissions(activity!!, arrayOf(RECORD_AUDIO), PERMISSION_CODE)

        return false
    }

    override fun onStop() {
        super.onStop()
        // Stop recording is someone presses back
        if (isRecording) {
            stopRecording()
        }
    }

    // Extracts the breathing rate from the recording
    private fun processRecording() {
        AndroidFFMPEGLocator(context)
        Log.i(TAG, "Starting to process recording")
        val audioStream = PipedAudioStream(fileLocation)
            .getMonoStream(44100, 0.0)
        val breathingRate = AudioAnalyzerImpl()
            .breathingRateFromAudioStream(audioStream)
        Log.i(TAG, "Breathing rate: $breathingRate")
        viewModel.setBreathingRate(breathingRate)
    }

    interface Callback{
        fun onRecordingFinished(fileLocation: String)
    }
}
