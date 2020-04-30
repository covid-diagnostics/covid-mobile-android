package com.example.coronadiagnosticapp.ui.fragments.dailtyMetric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.GeneralFeeling
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.daily_metric_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class DailyMetricFragment : ScopedFragment() {
    @Inject
    lateinit var viewModel: DailyMetricViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.getAppComponent()?.inject(this)

        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 0
        activity?.findViewById<View>(R.id.stepperLayout)?.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.daily_metric_fragment, container, false)
    }


//    TODO add was tested , ask if positive or negative
//    TODO add how are you feeling with enum @GeneralFeeling

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 0
    }

    private fun initForm() {
//        form {
//            inputLayout(activity_metrics_inp_temp) {
//                isNotEmpty().description(getString(R.string.required))
//            }
//            checkable(activity_metrics_chk_cough_wet) {
//            }
//            submitWith(button_metricSubmit) { res ->
//                submitDailyMetrics(
//                    res[temp],
//                    res[exposureDate],
//                    res[posTstDate],
//                    res[negTstDate],
//                    res[feeling]
//                )
//            }
//        }
    }

    private val progress get() = progressBar_metricFragment

    private fun submitDailyMetrics(
        temp: Double?, exposureDate: Date?,
        posTstDate: Date?, negTstDate: Date?,
        feeling: GeneralFeeling
    ) {
        showLoading(progress, true)
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.submitMeasurement(temp, exposureDate, posTstDate, negTstDate, feeling)

            withContext(Dispatchers.Main) {
                showLoading(progress, false)
                findNavController().navigate(R.id.action_dailyMetricFragment_to_questioneerFragment)
            }
        }
    }


}
