package com.example.coronadiagnosticapp.data.network

import androidx.lifecycle.LiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import java.io.File

interface NetworkDataSource {
    val responseUser: LiveData<ResponseUser>
    val error: LiveData<String>

    suspend fun registerUser(
        userRegister: UserRegister
    ): ResponseUser

    suspend fun updateUserPersonalInformation(
        user: User
    ): User?

    suspend fun submitMeasurement(measurement: Measurement): Measurement
    suspend fun submitPpgMeasurement(measurement: PpgMeasurement): PpgMeasurement
    suspend fun uploadAudioRecording(file: File, id: Int)
}