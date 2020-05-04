package com.example.coronadiagnosticapp.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.util.*
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

    override suspend fun updateUserInfo(
        userInfo: UserInfo
    ): UserInfo? {
        try {
            return api.updateUserInfo(userInfo).await()

        } catch (e: HttpException) {
            Log.e("HTTP", e.response().toString())
        }
        return null
    }


    override suspend fun submitMeasurement(measurement: Measurement): Measurement {
        return api.submitMeasurement(measurement).await()
    }

    override suspend fun submitPpgMeasurement(measurement: PpgMeasurement): PpgMeasurement {
        return api.submitPpgMeasurement(measurement).await()
    }


    override suspend fun uploadAudioRecording(file: File, id: Int) {
        val filePart = MultipartBody.Part.createFormData(
            "chestRecording",
            file.name,
            RequestBody.create("audio/*".toMediaType(), file)
        )
        val idPart = MultipartBody.Part.createFormData("id", id.toString())

        return api.uploadAudioRecording(filePart, idPart).await()
    }

    override suspend fun getQuestions(): List<JsonObject> {
        val language = Locale.getDefault().language
        return api.getQuestions(language).await()
    }

    override suspend fun sendAnswers(answers: List<AnswersResponse>) {
        answers.forEach { api.sendUserAnswer(it).await() }
    }
}