package com.example.coronadiagnosticapp.data.repository

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.Measurement
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterAverages
import java.io.File

interface Repository {
    val error: MutableLiveData<String>
    var breathingRate: Double

    suspend fun registerUser(userRegister: UserRegister)
    fun isLoggedIn(): Boolean
    suspend fun updateUserPersonalInformation(firstName: String, lastName: String, age: Int)
    suspend fun saveResult(healthResult: HealthResult)
    fun getLastResult(): HealthResult?
    fun getUserName(): String?
    suspend fun uploadAudioRecording(file: File)
    suspend fun submitMeasurement(measurement: Measurement): Measurement
    @RequiresApi(value = 23)
    suspend fun submitPpgMeasurement(
        oxymeterAverages: OxymeterAverages,
        cc: CameraCharacteristics
    )
}