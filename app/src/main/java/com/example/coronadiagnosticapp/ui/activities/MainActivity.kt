package com.example.coronadiagnosticapp.ui.activities

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources

import android.os.Bundle
import android.view.View
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stepperLayout.visibility = View.INVISIBLE
    }
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ApplicationLanguageHelper.wrap(newBase!!, "he"))
    }

}
