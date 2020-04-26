package com.example.coronadiagnosticapp.ui.audioAnalyzer

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.filters.BandPass
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import be.tarsos.dsp.onsets.ComplexOnsetDetector


class AudioAnalyzerImpl : AudioAnalyzer {
    private var numberOfPeaks = 0

    companion object {
        const val TAG = "AudioAnalyzer"
    }

    override fun breathingRateFromAudioStream(audioStream: TarsosDSPAudioInputStream): Double {
        // how much samples are processed in one step
        val audioBufferSize = 4096
        // How much consecutive buffers overlap (in samples). Half of the AudioBufferSize is common (512, 1024) for an FFT
        val bufferOverlap = 2048

        val sampleRate = audioStream.format.sampleRate
        val dispatcher = AudioDispatcher(audioStream, audioBufferSize, bufferOverlap)

//        val newSampleRate = 6000
//        dispatcher.addAudioProcessor(RateTransposer(newSampleRate / sampleRate.toDouble()))
        dispatcher.addAudioProcessor(BandPass(600F, 200F, sampleRate))
        val onsetDetector = ComplexOnsetDetector(4096, 0.3, 0.5)


        onsetDetector.setHandler { _, _ -> numberOfPeaks++ }
        dispatcher.addAudioProcessor(onsetDetector)
        dispatcher.run()
        Log.i(TAG, "Total peaks: $numberOfPeaks")
        return numberOfPeaks.toDouble() / dispatcher.secondsProcessed()
    }
}