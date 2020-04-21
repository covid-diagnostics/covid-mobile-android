package com.example.coronadiagnosticapp.ui.audioAnalyzer

import android.util.Log
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.filters.BandPass
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import be.tarsos.dsp.resample.RateTransposer


class AudioAnalyzerImpl : AudioAnalyzer {
    private var numberOfPeaks = 0

    override fun heartRateFromAudioStream(audioStream: TarsosDSPAudioInputStream): Double {
        // how much samples are processed in one step
        val audioBufferSize = 4096
        // How much consecutive buffers overlap (in samples). Half of the AudioBufferSize is common (512, 1024) for an FFT
        val bufferOverlap = 2048

        val sampleRate = audioStream.format.sampleRate
        val dispatcher = AudioDispatcher(audioStream, audioBufferSize, bufferOverlap)
        val newSampleRate = 6000



        //dispatcher.addAudioProcessor(RateTransposer(newSampleRate / sampleRate.toDouble()))
        //dispatcher.addAudioProcessor(BandPass(600F, 200F, sampleRate))
        val onsetDetector = ComplexOnsetDetector(1024, 0.3)


        onsetDetector.setHandler { time, salience ->
            numberOfPeaks++
            Log.i("ASDASD", "PEAK!!")
        }
        dispatcher.addAudioProcessor(object : AudioProcessor {
            override fun processingFinished() {
                Log.i("a", "finished")
            }

            override fun process(p0: AudioEvent?): Boolean {
                Log.i("a", "process")
                return true
            }

        })
        dispatcher.addAudioProcessor(onsetDetector)
        Log.i("asd", "%%%%%%%%%%%%%%%%%%")
        Log.i("asd", audioStream.format.frameRate.toString())
        Log.i("asd", audioStream.format.frameSize.toString())
        Log.i("asd", audioStream.frameLength.toString())


        dispatcher.run()
        return dispatcher.secondsProcessed() / (numberOfPeaks.toDouble() / 2)


//        val amplitudeAverages = mutableListOf<Double>()
//        dispatcher.addAudioProcessor(object : AudioProcessor {
//            val fft = FFT(1024)
//            val currentPhaseOffsets = FloatArray(audioBufferSize / 2)
//            val magnitudes = FloatArray(audioBufferSize / 2)
//
//            override fun processingFinished() {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//            override fun process(audioEvent: AudioEvent): Boolean {
//                val audio = audioEvent.floatBuffer
//                fft.powerPhaseFFT(audio.clone(), magnitudes, currentPhaseOffsets)
//                amplitudeAverages.add(magnitudes.average())
//                return true
//            }
//        })
    }
}