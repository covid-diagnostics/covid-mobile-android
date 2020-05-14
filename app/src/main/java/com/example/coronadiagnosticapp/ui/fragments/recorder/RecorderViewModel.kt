package com.example.coronadiagnosticapp.ui.fragments.recorder

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import java.io.File
import javax.inject.Inject

class RecorderViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    suspend fun uploadFile(file: File) = repository.uploadAudioRecording(file)

    fun setBreathingRate(x: Double) {
        repository.breathingRate = x
    }

    private val recordings = RecordingsFactory.getRecordings()
    private var currentRecordingIndex = 0

    fun getCurrentRecording() = recordings.getOrNull(currentRecordingIndex)
    fun getNextRecording(): Recording? {
        currentRecordingIndex++
        return getCurrentRecording()
    }

    val recordingCount: Int = recordings.size
    fun getCurrentRecordingIndex() = currentRecordingIndex
}
