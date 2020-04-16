package com.example.coronadiagnosticapp.ui.activities

import android.Manifest
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


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
        conf.setLocale(Locale("iw")) // API 17+ only.

        // Use conf.locale = new Locale(...) if targeting lower versions
        // Use conf.locale = new Locale(...) if targeting lower versions
        res.updateConfiguration(conf, dm)
//
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val stepsLabels = arrayOf(

            resources.getString(R.string.medical_questionnaire),
            resources.getString(R.string.camera_test),
            resources.getString(R.string.recorder_test)
        )
        stepperIndicator.setLabels(stepsLabels)
        stepperIndicator.layoutDirection = View.LAYOUT_DIRECTION_LTR
    }

}
