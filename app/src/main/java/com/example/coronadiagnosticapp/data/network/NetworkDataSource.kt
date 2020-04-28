package com.example.coronadiagnosticapp.data.network

import androidx.lifecycle.LiveData
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.responseMetric.ResponseMetric
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

    suspend fun updateUserMetrics(cough: Int, isWet: Boolean, temp: String): ResponseMetric

    suspend fun uploadAudioRecording(file: File, id: Int)

    suspend fun getQuestions(): List<JsonObject>
    suspend fun sendAnswers(answers: List<AnswersResponse>)
}