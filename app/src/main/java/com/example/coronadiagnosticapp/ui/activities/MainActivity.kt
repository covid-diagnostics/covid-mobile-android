package com.example.coronadiagnosticapp.ui.activities

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
}
