package com.example.coronadiagnosticapp.ui.fragments.recorder

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class RecorderViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    val error = repository.error

    suspend fun uploadFile(file: File){
        repository.uploadAudioRecording(file)

    }
}
