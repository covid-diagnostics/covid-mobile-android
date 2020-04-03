package com.example.coronadiagnosticapp

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.example.coronadiagnosticapp.data.di.AppComponent
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
import com.example.coronadiagnosticapp.ui.activities.ApplicationLanguageHelper

class MyApplication : MultiDexApplication() {
    private lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.factory().create(applicationContext)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ApplicationLanguageHelper.wrap(newBase!!, "he"))
    }

    fun getAppComponent(): AppComponent = appComponent
}