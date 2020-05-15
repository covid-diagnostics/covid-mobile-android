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

    private var player: MediaPlayer? = null

    constructor(fileLocation: String, callback: Callback) : this() {
        this.filePath = fileLocation
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
        val mp = player ?: return
        if (mp.isPlaying) {
            play_pause_btn.setImageResource(R.drawable.ic_play)
            mp.pause()
        } else {
            play_pause_btn.setImageResource(R.drawable.ic_pause)
            mp.start()
        }
    }

    private fun loadRecording(filePath: String) {

        play_pause_btn.isEnabled = false
        player = MediaPlayer().apply {
            setDataSource(filePath)
            prepareAsync()
            setOnPreparedListener {
                play_pause_btn.isEnabled = true
                sound_seekBar.max = duration
                updateProgress()
            }
        }

        updateProgress()
    }

    private fun updateProgress() {
        GlobalScope.launch {
            while (player != null) {
                if (player!!.isPlaying) {
                    withContext(Main) {
                        if (player != null){
                            sound_seekBar.progress = player!!.currentPosition
                        }
                        else{
                            sound_seekBar.progress = 0
                        }

                    }
                }
                delay(100)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player?.apply {
            if (isPlaying) {
                toggleRecording()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.apply {
            stop()
            release()
        }
        player = null
    }

    interface Callback {
        fun onContinueTapped()
        fun onRecordTapped()
    }
}