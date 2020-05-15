package com.example.coronadiagnosticapp.ui.fragments.recorder

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.recording_fragment.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecordingFragment() : Fragment() {

    @Inject
    lateinit var viewModel: RecorderViewModel

    private lateinit var filePath: String
    private lateinit var callback: Callback

    constructor(fileLocation: String, callback: Callback) : this() {
        this.filePath = fileLocation
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.recording_fragment, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context!!.getAppComponent().inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRecording(filePath)
        continue_btn.setOnClickListener {
            callback.onContinueTapped()
        }
        player_mic_btn.setOnClickListener {
            callback.onRecordTapped()
        }
        play_pause_btn.setOnClickListener {
            toggleRecording()
        }
    }

    private fun toggleRecording() {
        TODO("Not yet implemented")
    }

    private fun loadRecording(filePath: String) {
        val player = MediaPlayer()
        player.setDataSource(filePath)
        player.prepare()//async
        sound_seekBar.max = player.duration
        player.start()
        GlobalScope.launch {
            while (player.isPlaying) {
                withContext(Main) {
                    sound_seekBar.progress = player.currentPosition
                }
                delay(1000 * 1)
            }
        }
    }

    interface Callback {
        fun onContinueTapped()
        fun onRecordTapped()
    }
}