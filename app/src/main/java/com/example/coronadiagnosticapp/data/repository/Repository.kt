package com.example.coronadiagnosticapp.data.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import java.io.File

interface Repository {
    val error: MutableLiveData<String>

    suspend fun registerUser(userRegister: UserRegister)
    fun isLoggedIn(): Boolean
    suspend fun updateUserPersonalInformation(firstName: String, lastName: String, age: Int)
    suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean)

    suspend fun saveResult(healthResult: HealthResult)
    fun getLastResult(): LiveData<HealthResult>

    suspend fun uploadAudioRecording(file: File)

}