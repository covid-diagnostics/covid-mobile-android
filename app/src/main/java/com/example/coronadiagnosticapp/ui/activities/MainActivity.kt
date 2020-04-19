package com.example.coronadiagnosticapp.ui.activities

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.Reminder.RegisterNotificationService


// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 215

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.INTERNET,
    Manifest.permission.RECORD_AUDIO
)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val res: Resources = this.resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
//        conf.setLocale(Locale("iw")) // API 17+ only.
//        res.updateConfiguration(conf, dm)
//
        //Reminder.createNotification2(applicationContext, getSystemService(Context.ALARM_SERVICE) as AlarmManager);
        val i = Intent(applicationContext,  RegisterNotificationService::class.java)
        startService(i)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}
