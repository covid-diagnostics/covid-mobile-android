package com.example.coronadiagnosticapp.ui.activities

import android.os.Bundle
import com.afollestad.vvalidator.util.hide
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /**
     * use this method from fragment when needed
     */
    fun setStepperCount(count: Int) {
        stepperIndicator.currentStep = count
    }

    fun showStepperLayout() {
//        stepperLayout?.show()
//        TODO uncomment
    }

    fun hideStepperLayout() = stepperLayout?.hide()

}
