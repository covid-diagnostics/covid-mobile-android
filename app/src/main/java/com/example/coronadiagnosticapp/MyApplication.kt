package com.example.coronadiagnosticapp

import androidx.multidex.MultiDexApplication
import com.example.coronadiagnosticapp.data.di.AppComponent
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent

class MyApplication : MultiDexApplication() {
    private lateinit var appComponent: AppComponent
    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.factory().create(applicationContext)
    }

    fun getAppComponent(): AppComponent = appComponent
}