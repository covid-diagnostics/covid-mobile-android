package com.example.coronadiagnosticapp.ui.fragments.resultFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.afollestad.vvalidator.util.show
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.getColor
import com.example.coronadiagnosticapp.utils.setLinearGradientColors
import kotlinx.android.synthetic.main.result_fragment.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResultFragment : Fragment() {

    @Inject
    lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)

        GlobalScope.launch(IO) {
            val count = viewModel.getMeasurementCount()
            val measurementId = viewModel.getMeasurementId()
            withContext(Main) {
                textView_summaryText2?.show()
                textView_summaryText2?.text =
                    getString(R.string.tests_done_so_far, count)
                textViewMeasurementIdDisplay?.text= getString(R.string.thank_you, measurementId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.result_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        continue_text.setLinearGradientColors(
            getColor(R.color.text_outer_color),
            getColor(R.color.text_mid_color)
        )
    }
}