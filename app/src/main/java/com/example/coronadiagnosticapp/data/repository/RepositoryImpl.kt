package com.example.coronadiagnosticapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.TokenProvider
import java.io.File
import java.lang.Exception
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val networkDataSource: NetworkDataSource,
    val dao: DbDao,
    val tokenProvider: TokenProvider,
    val tokenServiceInterceptor: TokenServiceInterceptor
) : Repository {

    private lateinit var responseUser: ResponseUser
    override val error: MutableLiveData<String> = MutableLiveData()


    override suspend fun registerUser(userRegister: UserRegister) {
        try {
            val responseUser = networkDataSource.registerUser(userRegister)
            dao.upsertUser(responseUser.user)
            tokenServiceInterceptor.sessionToken = responseUser.token.access
        } catch (e: Exception) {
            error.postValue(e.message)
        }

    }

    override fun isLoggedIn(): Boolean = !tokenServiceInterceptor.sessionToken.isNullOrBlank()

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


    override suspend fun saveResult(healthResult: HealthResult) {
        dao.insertHealth(healthResult)
    }

    override fun getLastResult(): LiveData<HealthResult> = dao.getLastHealthResult()

    override suspend fun uploadAudioRecording(file: File) {
        try {
            val id = dao.getMetric().id
            networkDataSource.uploadAudioRecording(file, id)

        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }


}