package com.example.coronadiagnosticapp.data.network

import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.Measurement
import com.example.coronadiagnosticapp.data.db.entity.PpgMeasurement
import com.example.coronadiagnosticapp.data.db.entity.UserInfo
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.User
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    @POST(USER_INFO_URL)
    fun updateUserInfo(
        @Body userInfo: UserInfo
    ): Deferred<UserInfo>

    @POST(PPG_MEASUREMENT_URL)
    fun submitPpgMeasurement(
        @Body measurement: PpgMeasurement
    ): Deferred<PpgMeasurement>

    @POST(MEASUREMENT_URL)
    fun submitMeasurement(
        @Body measurement: Measurement
    ): Deferred<Measurement>

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

    @GET(MEASUREMENT_COUNT_URL)
    fun getMeasurementCount(): Deferred<Int>

    @POST(SEND_ANSWERS)
    fun sendUserAnswer(
        @Body answer: AnswersResponse
    ): Deferred<AnswersResponse>

    @GET(NUMBER_OF_MEASUREMENTS)
    fun getNumberOfMeasurements(): Deferred<Int>

    companion object {
        operator fun invoke(interceptor: TokenServiceInterceptor): ApiServer {
            val logging = HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            }

            val okHttpClient = OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
            //TODO fix for server to get default
            //    TODO add this converter to api instead of using it explicitly
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
                .build()
                .create(ApiServer::class.java)
        }
    }
}