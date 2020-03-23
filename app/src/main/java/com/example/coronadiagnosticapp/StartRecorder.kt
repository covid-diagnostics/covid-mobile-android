package com.example.coronadiagnosticapp

import android.content.Context
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FileDataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.github.kittinunf.fuel.json.responseJson
import kotlinx.android.synthetic.main.activity_start_recorder.*
import com.github.kittinunf.result.Result
import java.io.File
import java.io.IOException

private const val LOG_TAG = "AudioRecording"
private const val UPLOAD_URL = "/api/me/submit-raw-info/"

class StartRecorder : AppCompatActivity() {
    private var fileName: String = ""

    private var recordButton: RecordButton? = null
    private var recorder: MediaRecorder? = null

    private fun onRecord(start: Boolean) = if (start) {
        startRecording()
    } else {
        stopRecording()
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }

            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        val preferencesHelper = SharedPreferencesHelper(this)
        val submissionId = preferencesHelper.getSubmissionId()
        val param = listOf("id" to submissionId)
        Fuel.upload(UPLOAD_URL, method = Method.PUT, parameters = param)
            .add(
                FileDataPart(
                    File(fileName),
                    name = "chestRecording",
                    filename = "chestrecording.3gp"
                )
            ).timeout(150 * 1000)
            .response() { _, response, result ->
                when (result) {
                    is Result.Failure -> {

                        println("SHITTT")

                    }
                    is Result.Success -> {
                        println("YEAAAHHH")
                    }
                }
            }
    }

    inner class RecordButton(ctx: Context) : androidx.appcompat.widget.AppCompatButton(ctx) {

        var mStartRecording = true

        var clicker: OnClickListener = OnClickListener {
            onRecord(mStartRecording)
            text = when (mStartRecording) {
                true -> "Stop recording"
                false -> "Start recording"
            }
            mStartRecording = !mStartRecording
        }

        init {
            text = "Start recording"
            setOnClickListener(clicker)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        recordButton = RecordButton(this)
        val ll = LinearLayout(this).apply {
            addView(
                recordButton,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0f
                )
            )
        }
        setContentView(ll)

        //setContentView(R.layout.activity_start_recorder)
    }
}
