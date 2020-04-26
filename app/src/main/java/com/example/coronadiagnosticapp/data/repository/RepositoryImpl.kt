package com.example.coronadiagnosticapp.data.repository

import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.Question
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.SharedProvider
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val networkDataSource: NetworkDataSource,
    val dao: DbDao,
    val sharedProvider: SharedProvider,
    val tokenServiceInterceptor: TokenServiceInterceptor
) : Repository {
    companion object {
        private var lastHealthResult: HealthResult? = null
        private var breathingRate_: Double = -1.0
    }

    override var breathingRate: Double
        get() = breathingRate_
        set(value) {
            breathingRate_ = value
        }

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
        if (!tokenInterceptor.isNullOrBlank()) return true

        val tokenFromPreference = sharedProvider.getToken()
        if (tokenFromPreference.isNullOrBlank()) return false

        tokenServiceInterceptor.sessionToken = tokenFromPreference
        return true
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


    override suspend fun saveResult(healthResult: HealthResult) {
        lastHealthResult = healthResult
        dao.insertHealth(healthResult)
    }

    override fun getLastResult() = lastHealthResult
    override fun getUserName() = sharedProvider.getName()

    override suspend fun uploadAudioRecording(file: File) {
        try {
            val id = dao.getMetric().id
            networkDataSource.uploadAudioRecording(file, id)

        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }

    override suspend fun getQuestions(): List<Question> {
//        TODO could save a flag after saving for not reloading every time
        //        dao.insert(questions)//TODO is using room necessary here?
        return networkDataSource.getQuestions()
    }

}