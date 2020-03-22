package com.example.coronadiagnosticapp

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(private val context: Context) {
    private val IS_LOGGED_IN = "IS_LOGGED_IN"
    private val FIRST_NAME = "FIRST_NAME"
    private val TOKEN = "TOKEN"
    private val GENDER = "GENDER"
    private val app_prefs: SharedPreferences

    init {
        app_prefs = context.getSharedPreferences(
            "shared",
            Context.MODE_PRIVATE
        )
    }

    fun putIsLoggedIn(is_logged_in: Boolean) {
        val edit = app_prefs.edit()
        edit.putBoolean(IS_LOGGED_IN, is_logged_in)
        edit.commit()
    }

    fun getIsLoggedIn(): Boolean {
        return app_prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun putFirstName(first_name: String) {
        val edit = app_prefs.edit()
        edit.putString(FIRST_NAME, first_name)
        edit.commit()
    }

    fun getFirstName(): String? {
        return app_prefs.getString(FIRST_NAME, "")
    }

    fun putToken(token: String) {
        val edit = app_prefs.edit()
        edit.putString(TOKEN, token)
        edit.commit()
    }

    fun getToken(): String? {
        return app_prefs.getString(TOKEN, "")
    }


}