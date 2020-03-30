package com.example.coronadiagnosticapp.ui.fragments.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.recorder_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// This is an array of all the permission specified in the manifest.

private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class RecorderFragment : ScopedFragment() {

    private val MAX_DURATION = 1000 * 3
    private var isRecording = false
    private val recordPermission = Manifest.permission.RECORD_AUDIO
    private var PERMISSION_CODE: Int = 21

    private var mediaRecorder: MediaRecorder? = null
    private var recordFile: String? = null

    private var fileLocation: String? = null


    @Inject
    lateinit var viewModel: RecorderViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
        recordFile = context!!.externalCacheDir!!.absolutePath

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
            if (isRecording) {
                // Stop
                stopRecording()

                record_btn.setImageResource(R.drawable.record_btn_stopped)
                isRecording = false
            } else {
                // Check permission
                if (checkPermissions()) {
                    // Start
                    startRecording()

                    record_btn.apply {
                        setImageResource(R.drawable.record_btn_recording)
                        isEnabled = false
                    }
                    isRecording = true
                }
            }
        }
    }

    private fun stopRecording() {
        //Stop Timer, very obvious
        //Stop Timer, very obvious
        record_timer.stop()

        //Change text on page to file saved
        //Change text on page to file saved
        record_filename.text = "Recording Stopped, File Saved : $recordFile"

        //Stop media recorder and set it to null for further use to record new audio
        //Stop media recorder and set it to null for further use to record new audio
        mediaRecorder!!.stop()
        mediaRecorder!!.release()
        mediaRecorder = null


    }

    private fun startRecording() {
        //Start timer from 0
        //Start timer from 0
        record_timer.setBase(SystemClock.elapsedRealtime())
        record_timer.start()

        //Get app external directory path
        //Get app external directory path
        val recordPath = activity!!.getExternalFilesDir("/")!!.absolutePath

        //Get current date and time
        //Get current date and time
        val formatter =
            SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.CANADA)
        val now = Date()

        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        //initialize filename variable with date and time at the end to ensure the new file wont overwrite previous file
        //recordFile = "Recording_" + formatter.format(now) + ".3gp"
        recordFile = "Recording_.3gp"

        fileLocation = "$recordPath/$recordFile"

        //Setup Media Recorder for recording
        //Setup Media Recorder for recording
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile(fileLocation)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder!!.setMaxDuration(MAX_DURATION)
        mediaRecorder!!.setOnInfoListener { mr, what, extra ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                isRecording = false
                stopRecording()


                showLoading(true)
                launch(Dispatchers.IO) {
                    viewModel.uploadFile(File(fileLocation))
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Log.d("Record", "file finish")
                        findNavController().navigate(R.id.action_recorderFragment_to_resultFragment)
                    }
                }
            }
        }


        try {
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //Start Recording
        //Start Recording
        mediaRecorder!!.start()

    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_recordFragment.visibility = View.VISIBLE
            false -> progressBar_recordFragment.visibility = View.GONE
        }
    }

    private fun checkPermissions(): Boolean { //Check permission
        return if (ActivityCompat.checkSelfPermission(
                context!!,
                recordPermission
            ) == PackageManager.PERMISSION_GRANTED
        ) { //Permission Granted
            true
        } else { //Permission not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(recordPermission),
                PERMISSION_CODE
            )
            false
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRecording) {
            stopRecording()
        }
    }

}
