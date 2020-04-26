package com.example.coronadiagnosticapp.ui.fragments.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.repository.Repository
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class CameraViewModel @Inject
constructor(val repository: Repository) : ViewModel() {
    suspend fun uploadVideo(file: File) {
        delay(3000)

    }

    suspend fun saveResult(healthResult: HealthResult){
        repository.saveResult(healthResult)
    }

    suspend fun updateUserCameraCharacteristics(cameraCharacteristics : CameraCharacteristics) {
        repository.updateUserCameraCharacteristics(cameraCharacteristics)
    }
}
