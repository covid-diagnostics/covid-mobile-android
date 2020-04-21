package com.example.coronadiagnosticapp.ui.audioAnalyzer
import be.tarsos.dsp.io.TarsosDSPAudioInputStream

interface AudioAnalyzer {
    fun heartRateFromAudioStream(audioStream: TarsosDSPAudioInputStream) : Double
}
