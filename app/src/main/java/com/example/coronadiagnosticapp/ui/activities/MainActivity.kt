package com.example.coronadiagnosticapp.ui.activities

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.afollestad.vvalidator.util.hide
import com.afollestad.vvalidator.util.show
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
    }

    /**
     * use this method from fragment when needed
     */
    fun setStepperCount(count: Int) {
        stepperIndicator.currentStep = count
    }

    fun showStepperLayout() = stepperLayout.show()

    fun hideStepperLayout() = stepperLayout.hide()

}
