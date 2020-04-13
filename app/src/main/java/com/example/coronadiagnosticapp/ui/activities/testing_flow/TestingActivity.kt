package com.example.coronadiagnosticapp.ui.activities.testing_flow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.activity_testing.*
import javax.inject.Inject

class TestingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)
        setSupportActionBar(toolbar)
    }
}
