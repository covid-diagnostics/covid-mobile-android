package com.example.coronadiagnosticapp.data.network

import androidx.lifecycle.LiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
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

    suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean): ResponseMetric

    suspend fun uploadAudioRecording(file: File, id: Int)
}