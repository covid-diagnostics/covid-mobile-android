package com.example.coronadiagnosticapp.data.providers

import android.content.Context
import android.util.Log
import javax.inject.Inject

const val USER_TOKEN = "USER_TOKEN"

class TokenProviderImpl @Inject constructor(context: Context) : TokenProvider,
    PreferenceProvider(context) {


    override  fun getToken(): String? {
        Log.d("token","get")
        val token =  preferences.getString(USER_TOKEN, null)
        return token

    }

    override  fun setToken(token: String?) {
        Log.d("token","set")
        with(preferences.edit()) {
            putString(USER_TOKEN, token)
            apply()
        }
    }
}