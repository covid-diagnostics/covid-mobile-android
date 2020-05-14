package com.example.coronadiagnosticapp.data.providers

interface SharedProvider {
    fun getToken(): String?
    fun setToken(token: String?)

    fun getName(): String?
    fun setName(token: String?)

    fun getIsFirstTime(): Boolean
    fun setIsFirstTime(isFirstTime: Boolean)
    fun didSetNotificationTime(): Boolean
    fun setNotificationTime(didSet:Boolean)
}