package com.example.coronadiagnosticapp.data.providers

import android.content.Context
import javax.inject.Inject


class SharedProviderImpl @Inject constructor(context: Context) : SharedProvider,
    PreferenceProvider(context) {

    companion object {
        const val USER_TOKEN = "USER_TOKEN"
        const val USER_NAME = "USER_NAME"
    }

    override fun getToken() = preferences.getString(USER_TOKEN, null)

    override fun setToken(token: String?) =
        with(preferences.edit()) {
            putString(USER_TOKEN, token)
            apply()
        }

    override fun getName() = preferences.getString(USER_NAME, null)

    override fun setName(userName: String?) =
        with(preferences.edit()) {
            putString(USER_NAME, userName)
            apply()
        }
}
