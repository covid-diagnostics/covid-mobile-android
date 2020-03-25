package com.example.coronadiagnosticapp.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.lang.Exception
import javax.inject.Inject

class NetworkDataSourceImpl @Inject constructor(val api: ApiServer) : NetworkDataSource {

    private val _responseUser = MutableLiveData<ResponseUser>()
    private val _error = MutableLiveData<String>()
    override val responseUser: LiveData<ResponseUser> = _responseUser
    override val error: LiveData<String> = _error


    override suspend fun registerUser(userRegister: UserRegister): ResponseUser {

        try {
            return api.registerUser(userRegister).await()
        }catch (e: HttpException) {
            Log.e("HTTP", e.response().toString())
        }
        return api.registerUser(userRegister).await()



    }

    override suspend fun updateUserPersonalInformation(
        user: User
    ): User? {
        try {
            return api.updateUserInformation(user).await()

        } catch (e: HttpException) {
            Log.e("HTTP", e.response().toString())
        }
        return null
    }

    override suspend fun updateUserMetrics(
        temp: String,
        cough: Int,
        isWet: Boolean
    ): ResponseMetric {
        return api.updateUserMetrics(
            SendMetric(
                temperature = temp,
                coughStrength = cough,
                isCoughDry = isWet
            )
        ).await()

    }
}