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
import androidx.navigation.fragment.findNavController
import be.tarsos.dsp.io.PipedAudioStream
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.MainActivity
import com.example.coronadiagnosticapp.ui.audioAnalyzer.AudioAnalyzerImpl
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.recorder_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RecorderFragment : ScopedFragment() {
    companion object {
        private const val TAG = "RecorderFragment"
        private const val PERMISSION_CODE: Int = 21
        private const val MAX_DURATION:Long = 1000 * 10 // in milliseconds
    }

    @Inject
    lateinit var viewModel: RecorderViewModel

    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String? = null
    private var fileLocation: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.getAppComponent()?.inject(this)

        recordFile = context!!.externalCacheDir!!.absolutePath
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (context as? MainActivity)?.setStepperCount(2)
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

        //Change text
//        instruction_tv.text =

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
            delay(MAX_DURATION)
            if (isVisible.not()) return@launch
            // this thread will stay alive after the page is dead, so this is to avoid null reference
            stopRecording()
            // Upload file
            launch(Dispatchers.IO) {
                viewModel.uploadFile(File(fileLocation!!))
                withContext(Dispatchers.Main) {
                    findNavController().navigate(R.id.action_recorderFragment_to_recorderFragment2)
                }
                Log.d(TAG, "File finished uploading!")
            }
            processRecording()
        }

        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
    }

    private fun checkPermissions(): Boolean { // Check permission
        // Permission Granted
        if (ActivityCompat.checkSelfPermission(context!!, RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED)
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
        AndroidFFMPEGLocator(this.context)
        Log.i(TAG, "Starting to process recording")
        val audioStream = PipedAudioStream(fileLocation).getMonoStream(44100, 0.0)
        val breathingRate = AudioAnalyzerImpl().breathingRateFromAudioStream(audioStream)
        Log.i(TAG, "Breathing rate: $breathingRate")
        viewModel.setBreathingRate(breathingRate)
    }


}
