package com.example.coronadiagnosticapp.data.network

import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.responseMetric.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.responseMetric.SendMetric
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.User
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import javax.inject.Singleton


@Singleton
interface ApiServer {
    @POST(SIGNUP_URL)
    fun registerUser(
        @Body user: UserRegister
    ): Deferred<ResponseUser>

    @PUT(FILL_DETAILS_URL)
    fun updateUserInformation(
        @Body user: User
    ): Deferred<User>

    @POST(DAILY_METRICS_URL)
    fun updateUserMetrics(
        @Body sendMetric: SendMetric
    ): Deferred<ResponseMetric>

    @Multipart
    @PUT(VIDEO_UPLOAD)
    fun uploadVideo(@Part video: MultipartBody.Part)

    @Multipart
    @PUT(AUDIO_UPLOAD)
    fun uploadAudioRecording(
        @Part chestRecording: MultipartBody.Part,
        @Part id: MultipartBody.Part
    ): Deferred<Unit>

    @GET(QUESTIONS)
    fun getQuestions(
        @Header("Accept-Language") language: String
    ): Deferred<List<JsonObject>>

    @POST(SEND_ANSWERS)
    fun sendUserAnswers(
        @Body answers: List<AnswersResponse>
    ): Deferred<AnswersResponse>

    companion object {
        operator fun invoke(interceptor: TokenServiceInterceptor): ApiServer =
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient(interceptor))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
                .create(ApiServer::class.java)

        private fun createOkHttpClient(interceptor: TokenServiceInterceptor) =
            OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
    }
}