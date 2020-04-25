package com.example.coronadiagnosticapp.ui.fragments.dailtyMetric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.daily_metric_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DailyMetricFragment : ScopedFragment() {
    private var coughStrengthValue = 0

    @Inject
    lateinit var viewModel: DailyMetricViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            it.getAppComponent().inject(this)
            it.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 0
            it.findViewById<View>(R.id.stepperLayout)?.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.daily_metric_fragment, container, false)
    }

    private fun updateCoughStrength(strength: Int) {
        coughStrengthValue = strength

        cough_strength.text =
            getString(R.string.coughStrength, strength)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initCoughStrength()
        initForm()
        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 0
    }

    private fun initCoughStrength() {
        updateCoughStrength(coughStrengthValue)
        activity_metrics_inp_cough_strength.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            //            TODO make adapter for only onProgressChanged impl
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                updateCoughStrength(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initForm() = form {
        inputLayout(activity_metrics_inp_temp) {
            isNotEmpty().description(getString(R.string.required))
        }
        checkable(activity_metrics_chk_cough_wet) {
        }

        submitWith(button_metricSubmit) { res ->
//                TODO use not hardcoded keys
            val temp = res["activity_metrics_inp_temp"]?.value?.toString()
                ?: return@submitWith
            val coughIsWet = res["activity_metrics_chk_cough_wet"]?.value as? Boolean
                ?: return@submitWith

            submitDailyMetrics(temp, coughStrengthValue, coughIsWet)
        }
    }


    private fun submitDailyMetrics(temp: String, cough: Int, isWet: Boolean) {
        val progress = progressBar_metricFragment
        showLoading(progress, true)
        launch(Dispatchers.IO) {
            viewModel.updateUserMetrics(temp, cough, isWet)

            withContext(Dispatchers.Main) {
                showLoading(progress, false)
                findNavController().navigate(R.id.action_dailyMetricFragment_to_cameraFragment)
            }
        }
    }

}
