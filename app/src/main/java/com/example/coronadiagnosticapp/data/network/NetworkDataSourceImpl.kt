package com.example.coronadiagnosticapp.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import javax.inject.Inject


class NetworkDataSourceImpl @Inject constructor(val api: ApiServer) : NetworkDataSource {

    private val _responseUser = MutableLiveData<ResponseUser>()
    private val _error = MutableLiveData<String>()
    override val responseUser: LiveData<ResponseUser> = _responseUser
    override val error: LiveData<String> = _error


    override suspend fun registerUser(userRegister: UserRegister): ResponseUser {

        try {
            return api.registerUser(userRegister).await()
        } catch (e: HttpException) {
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

    override suspend fun uploadAudioRecording(file: File, id: Int) {
        val filePart = MultipartBody.Part.createFormData(
            "chestRecording",
            file.name,
            RequestBody.create(MediaType.parse("audio/*"), file)
        )

        val idPart = MultipartBody.Part.createFormData("id", id.toString())


        return api.uploadAudioRecording(filePart, idPart).await()
    }
}