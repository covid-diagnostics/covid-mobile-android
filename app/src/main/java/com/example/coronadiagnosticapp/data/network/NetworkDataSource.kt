package com.example.coronadiagnosticapp.data.network

import androidx.lifecycle.LiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.User
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.google.gson.JsonObject
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
    suspend fun getQuestions(): List<JsonObject>
    suspend fun sendAnswers(answers: List<AnswersResponse>)
    suspend fun getNumberOfMeasurements(): Int
}