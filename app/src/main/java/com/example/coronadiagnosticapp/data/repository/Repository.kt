package com.example.coronadiagnosticapp.data.repository

import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.Question
import com.example.coronadiagnosticapp.data.db.UserAnswers
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import java.io.File

interface Repository {
    val error: MutableLiveData<String>
    var breathingRate: Double

    suspend fun registerUser(userRegister: UserRegister)

    fun isLoggedIn(): Boolean

    suspend fun updateUserPersonalInformation(firstName: String, lastName: String, age: Int)

    suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean)

    suspend fun updateUserAnswers(answers: List<UserAnswers>)

    suspend fun saveResult(healthResult: HealthResult)

    fun getLastResult(): HealthResult?

    fun getUserName(): String?

    suspend fun uploadAudioRecording(file: File)

    suspend fun getQuestions(): List<Question>
}