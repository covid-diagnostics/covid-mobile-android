package com.example.coronadiagnosticapp.data.providers

import android.content.Context
import javax.inject.Inject

const val USER_TOKEN = "USER_TOKEN"
const val USER_NAME = "USER_NAME"
const val IS_FIRST_TIME = "IS_FIRST_TIME"

class SharedProviderImpl @Inject constructor(context: Context) : SharedProvider,
    PreferenceProvider(context) {

    companion object{
        const val HAS_CONSENT = "HAS_CONSENT"
    }


    override fun getToken(): String? {
        return preferences.getString(USER_TOKEN, null)

    }

    override fun setToken(token: String?) {
        with(preferences.edit()) {
            putString(USER_TOKEN, token)
            apply()
        }
    }

    override fun getName(): String? {
        return preferences.getString(USER_NAME, null)
    }

    override fun setName(userName: String?) {
        with(preferences.edit()) {
            putString(USER_NAME, userName)
            apply()
        }
    }

    override fun getIsFirstTime(): Boolean {
        return preferences.getBoolean(IS_FIRST_TIME, true)
    }

    override fun setIsFirstTime(isFirstTime: Boolean) {
        with(preferences.edit()) {
            putBoolean(IS_FIRST_TIME, isFirstTime)
            apply()
        }
    }

    override fun getHasConsent()=
        preferences.getBoolean(HAS_CONSENT, false)


    override fun setHasConsent(hasConsent: Boolean) {
        with(preferences.edit()) {
            putBoolean(HAS_CONSENT, hasConsent)
            apply()
        }
    }
}