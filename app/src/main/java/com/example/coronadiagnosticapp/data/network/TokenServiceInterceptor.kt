package com.example.coronadiagnosticapp.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenServiceInterceptor @Inject constructor() :
    Interceptor {
    private val AUTH_HEADER_KEY = "Authorization"
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