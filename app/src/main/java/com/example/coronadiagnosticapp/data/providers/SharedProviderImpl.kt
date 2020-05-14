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
    }

    override fun getToken(): String? =
        preferences.getString(USER_TOKEN, null)

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

    override fun getIsFirstTime() = getBool(IS_FIRST_TIME)

    override fun setIsFirstTime(isFirstTime: Boolean) = putBool(IS_FIRST_TIME, isFirstTime)

    override fun didSetNotificationTime() = getBool(DID_SET_NOTIF_TIME)

    override fun setNotificationTime(didSet: Boolean) = putBool(DID_SET_NOTIF_TIME, didSet)

    private fun putBool(key: String, flag: Boolean) =
        preferences.edit()
            .putBoolean(key, flag)
            .apply()

    private fun getBool(key: String, defValue: Boolean = false) =
        preferences.getBoolean(key, defValue)
}