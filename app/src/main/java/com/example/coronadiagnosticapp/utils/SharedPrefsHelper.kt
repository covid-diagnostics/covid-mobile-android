package com.example.coronadiagnosticapp.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsHelper {

    private var prefs: SharedPreferences? = null

    fun initWithContext(context: Context) {
        prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    }

    fun save(key: String, value: String?) = prefs!!.edit()
        .putString(key, value).apply()

    fun save(key: String, value: Boolean) = prefs!!.edit()
        .putBoolean(key, value).apply()

    fun getString(key: String) = prefs?.getString(key, null)

    fun getBool(key: String) = prefs!!.getBoolean(key, false)

}