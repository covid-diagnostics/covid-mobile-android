package com.example.coronadiagnosticapp.data.network

import com.example.coronadiagnosticapp.data.db.entity.*
import com.google.gson.JsonObject
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import javax.inject.Inject
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

    @POST(SEND_ANSWERS)
    fun sendUserAnswer(
        @Body answer: AnswersResponse
    ): Deferred<AnswersResponse>


    companion object {
        operator fun invoke(interceptor: TokenServiceInterceptor): ApiServer {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            val okHttpClient = OkHttpClient
                .Builder()
                .addInterceptor(interceptor)
                .addInterceptor(logging)
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
                .build()
                .create(ApiServer::class.java)
        }
    }
}


@Singleton
class TokenServiceInterceptor @Inject constructor() : Interceptor {
    val AUTH_HEADER_KEY = "Authorization"
    var sessionToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        if (sessionToken != null) {
            requestBuilder.addHeader(
                AUTH_HEADER_KEY, "JWT $sessionToken"
            )
        }
        return chain.proceed(requestBuilder.build())
    }
}