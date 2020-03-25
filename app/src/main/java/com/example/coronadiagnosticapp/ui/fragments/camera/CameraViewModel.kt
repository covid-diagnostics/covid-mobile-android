package com.example.coronadiagnosticapp.ui.fragments.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class CameraViewModel @Inject
constructor() : ViewModel() {
    suspend fun uploadVideo(file: File) {
        delay(3000)

    }
}
