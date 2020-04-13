package com.example.coronadiagnosticapp.data.repository


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import com.example.coronadiagnosticapp.ui.activities.testing_flow.BasicsInformation
import java.io.File
import java.util.*

interface Repository {
    val error: MutableLiveData<String>

    suspend fun registerUser(userRegister: UserRegister)
    fun isLoggedIn(): Boolean
    suspend fun updateUserPersonalInformation(firstName: String, lastName: String, age: Int)
    suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean)
    fun getUserName(): String?
    suspend fun uploadAudioRecording(file: File)
    suspend fun insertBasicsInformation(basicsInformation: BasicsInformation)
    fun getBasicsInformationExist(): LiveData<BasicsInformation>
    fun getLastHealth(): LiveData<HealthResult>
    suspend fun sendTestResult(appHeartRate: Int,deviceHeartRate:Int?, appSaturation: Int, deviceSaturation:Int?, deviceModel: String)

}