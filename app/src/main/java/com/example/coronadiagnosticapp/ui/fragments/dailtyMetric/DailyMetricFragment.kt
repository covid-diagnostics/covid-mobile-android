package com.example.coronadiagnosticapp.ui.fragments.dailtyMetric

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.MyApplication

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.daily_metric_fragment.*
import kotlinx.android.synthetic.main.information_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DailyMetricFragment : ScopedFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
    }

    @Inject
    lateinit var viewModel: DailyMetricViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.daily_metric_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initForm()
    }

    private fun initForm() {
        form {
            inputLayout(activity_metrics_inp_temp) {
                isNotEmpty().description(getString(R.string.required))
            }
            inputLayout(activity_metrics_inp_cough_strength) {
                isNotEmpty()


            }
            checkable(activity_metrics_chk_cough_wet) {
            }
            submitWith(button_metricSubmit) { res ->
                submitDailyMetrics(
                    res.get("activity_metrics_inp_temp")?.value.toString(),
                    res.get("activity_metrics_inp_cough_strength")?.value.toString().toInt(),
                    res.get("activity_metrics_chk_cough_wet")?.value as Boolean
                )
            }
        }
    }

    private fun submitDailyMetrics(temp: String, cough: Int, isWet: Boolean) {
        showLoading(true)
        launch(Dispatchers.IO) {
            viewModel.updateUserMetrics(temp, cough, isWet)

            withContext(Dispatchers.Main) {
                showLoading(false)
                findNavController().navigate(R.id.action_dailyMetricFragment_to_cameraFragment)
            }
        }

    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_metricFragment.visibility = View.VISIBLE
            false -> progressBar_metricFragment.visibility = View.GONE
        }
    }
}
