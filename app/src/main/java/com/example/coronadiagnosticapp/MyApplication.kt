package com.example.coronadiagnosticapp

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.example.coronadiagnosticapp.data.di.AppComponent
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
import io.sentry.android.core.SentryAndroid

class MyApplication : MultiDexApplication() {
    private lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(applicationContext)
        if (!BuildConfig.DEBUG) {
            SentryAndroid.init(this)
        }
    }

    fun getAppComponent(): AppComponent = appComponent
}