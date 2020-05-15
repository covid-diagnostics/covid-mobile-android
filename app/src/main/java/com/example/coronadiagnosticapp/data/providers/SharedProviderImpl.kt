package com.example.coronadiagnosticapp.data.providers

import android.content.Context
import javax.inject.Inject

class SharedProviderImpl @Inject constructor(context: Context) : SharedProvider,
    PreferenceProvider(context) {

    companion object{
        const val USER_TOKEN = "USER_TOKEN"
        const val USER_NAME = "USER_NAME"
        const val IS_FIRST_TIME = "IS_FIRST_TIME"
        const val DID_SET_NOTIF_TIME = "DID_SET_NOTIF_TIME"
        const val HAS_CONSENT = "HAS_CONSENT"
    }

    override fun getToken(): String? = getString(USER_TOKEN)

    override fun setToken(token: String?) = putString(USER_TOKEN, token)

    override fun getName(): String? = getString(USER_NAME)

    override fun setName(token: String?) = putString(USER_NAME, token)

    override fun getIsFirstTime() = getBool(IS_FIRST_TIME)

    override fun setIsFirstTime(isFirstTime: Boolean) = putBool(IS_FIRST_TIME, isFirstTime)

    override fun didSetNotificationTime() = getBool(DID_SET_NOTIF_TIME)

    override fun setNotificationTime(didSet: Boolean) = putBool(DID_SET_NOTIF_TIME, didSet)

    override fun getHasConsent() = getBool(HAS_CONSENT)

    override fun setHasConsent(hasConsent: Boolean) = putBool(HAS_CONSENT, hasConsent)

    private fun putBool(key: String, flag: Boolean) =
        preferences.edit()
            .putBoolean(key, flag)
            .apply()

    private fun getBool(key: String, defValue: Boolean = false) =
        preferences.getBoolean(key, defValue)

     private fun putString(key: String, value: String?) =
        preferences.edit()
            .putString(key, value)
            .apply()

    private fun getString(key: String, defValue: String? = null) =
        preferences.getString(key, defValue)

}