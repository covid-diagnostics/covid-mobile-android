package com.example.coronadiagnosticapp.ui.activities

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.Reminder.RegisterNotificationService
import java.util.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val i = Intent(applicationContext,  RegisterNotificationService::class.java)
        startService(i)

        setContentView(R.layout.activity_main)
        stepperLayout.visibility = View.INVISIBLE
    }
}
