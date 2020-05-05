package com.example.coronadiagnosticapp.ui.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import com.afollestad.vvalidator.util.hide
import com.afollestad.vvalidator.util.show
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.Reminder.RegisterNotificationService
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val languageToLoad = "iw" // your language
//
//        val locale = Locale(languageToLoad)
//        Locale.setDefault(locale)
//        val config = Configuration()
//        config.locale = locale
//        baseContext.resources.updateConfiguration(
//            config,
//            baseContext.resources.displayMetrics
//        )
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
