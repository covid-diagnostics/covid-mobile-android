package com.example.coronadiagnosticapp.utils

import androidx.multidex.MultiDexApplication
import com.example.coronadiagnosticapp.BuildConfig
import com.example.coronadiagnosticapp.data.di.AppComponent
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
import io.sentry.android.core.SentryAndroid

class MyApplication : MultiDexApplication() {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent
            .factory().create(applicationContext)
    }
    override fun onCreate() {
        super.onCreate()
        appComponent//Create
        if (!BuildConfig.DEBUG) {
            SentryAndroid.init(this)
        }
    }
}