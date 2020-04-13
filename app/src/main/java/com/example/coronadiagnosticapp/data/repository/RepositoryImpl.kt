package com.example.coronadiagnosticapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.db.entity.AnonymousMetrics
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.SharedProvider
import com.example.coronadiagnosticapp.ui.activities.testing_flow.BasicsInformation
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val networkDataSource: NetworkDataSource,
    val dao: DbDao,
    val sharedProvider: SharedProvider,
    val tokenServiceInterceptor: TokenServiceInterceptor
) : Repository {

    private lateinit var responseUser: ResponseUser
    override val error: MutableLiveData<String> = MutableLiveData()


    override suspend fun registerUser(userRegister: UserRegister) {
        try {
            val responseUser = networkDataSource.registerUser(userRegister)
            dao.upsertUser(responseUser.user)
            tokenServiceInterceptor.sessionToken = responseUser.token.access
            sharedProvider.setToken(responseUser.token.access)
        } catch (e: Exception) {
            error.postValue(e.message)
        }

    }

    override fun isLoggedIn(): Boolean {
        val tokenInterceptor = tokenServiceInterceptor.sessionToken
        val tokenFromPreference = sharedProvider.getToken()
        if (!tokenInterceptor.isNullOrBlank()) return true
        return if (!tokenFromPreference.isNullOrBlank()) {
            tokenServiceInterceptor.sessionToken = tokenFromPreference
            true
        } else {
            false
        }
    }

    override suspend fun updateUserPersonalInformation(
        firstName: String,
        lastName: String,
        age: Int
    ) {
        val user = dao.getUser()
        user.apply {
            this.firstName = firstName
            this.lastName = lastName
            this.age = age
        }
        val userRes = networkDataSource.updateUserPersonalInformation(user)
        if (userRes != null) {
            dao.upsertUser(userRes)
            sharedProvider.setName(userRes.firstName)
        }
    }

    override suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean) {
        try {
            val responseMetric = networkDataSource.updateUserMetrics(temp, cough, isWet)
            dao.upsertMetric(responseMetric)

        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }

    override fun getUserName() = sharedProvider.getName()

    override suspend fun uploadAudioRecording(file: File) {
        try {
            val id = dao.getMetric().id
            networkDataSource.uploadAudioRecording(file, id)

        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }

    override suspend fun insertBasicsInformation(basicsInformation: BasicsInformation) {
        dao.insertBasicsInformation(basicsInformation)
    }


    override fun getBasicsInformationExist(): LiveData<BasicsInformation> =
        dao.getBasicsInformation()

    override fun getLastHealth(): LiveData<HealthResult> {
        return dao.getLastHealthResult()
    }


    override suspend fun sendTestResult(
        appHeartRate: Int,
        deviceHeartRate: Int?,
        appSaturation: Int,
        deviceSaturation: Int?,
        deviceModel: String
    ) {
        val info = dao.getBasicsInformationWithoutLiveData()
        val item = networkDataSource.sendTestResult(
            AnonymousMetrics(
                appHeartRate,
                deviceHeartRate,
                appSaturation,
                deviceSaturation,
                deviceModel,
                info.measurement,
                info.position,
                info.lighting,
                info.age,
                info.medicalHistory
            )
        )
        dao.insertAnonymousMetrics(item)
    }
}