package com.example.coronadiagnosticapp.ui.fragments.dailtyMetric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.Measurement
import com.example.coronadiagnosticapp.ui.activities.MainActivity
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.daily_metric_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DailyMetricFragment : ScopedFragment() {
    @Inject
    lateinit var viewModel: DailyMetricViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.daily_metric_fragment, container, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.getAppComponent()?.inject(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.run {
            setStepperCount(0)
            showStepperLayout()
        }

        button_metricSubmit.setOnClickListener {
            createMeasurement()
        }
    }

    private fun createMeasurement() {

        val temp = temp_question_view.temperature
        val exposureDate = exposed_question_view.exposedDate

        val (positiveTestDate, negativeTestDate) = diagnosed_question_view.checkedDates

        val feeling = feeling_question_view.selectedFeeling

        val measurement = Measurement(
            tempMeasurement = temp,
            exposureDate = exposureDate,
            positiveTestDate = positiveTestDate,
            negativeTestDate = negativeTestDate,
            generalFeeling = feeling
        )

        showLoading(progress_bar, true)
        GlobalScope.launch(Dispatchers.IO) {

            viewModel.submitMeasurement(measurement)

            withContext(Dispatchers.Main) {
                progress_bar?.let {
                    showLoading(it, false)
                }
                findNavController().navigate(R.id.action_dailyMetricFragment_to_questioneerFragment)
            }
        }
    }


}
