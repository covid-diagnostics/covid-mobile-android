package com.example.coronadiagnosticapp.ui.fragments.resultFragment

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.coronadiagnosticapp.MyApplication

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.result_fragment.*
import javax.inject.Inject

class ResultFragment : ScopedFragment() {
    val GREEN_COLOR_CODE = "#3b9b19"
    val YELLOW_COLOR_CODE = "#ffcc00"
    val RED_COLOR_CODE = "#cc3300"
    val NORMAL_COLOR = Color.parseColor(GREEN_COLOR_CODE)
    val RISKY_COLOR = Color.parseColor(YELLOW_COLOR_CODE)
    val SEVERE_COLOR = Color.parseColor(RED_COLOR_CODE)

    @Inject
    lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            it.applicationContext.let { ctx ->
                (ctx as MyApplication).getAppComponent().inject(this)
            }
            it.findViewById<StepperIndicator>(R.id.stepperIndicator)?.apply {
                visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.result_fragment, container, false)
    }

    fun setTextHTML(html: String): Spanned
    {
        val result: Spanned = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(html)
        }
        return result
    }

    fun colorizeText(text: String, color: String): String {
        return "<font color='${color}'>${text}</font>"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val NORMAL_TEXT = colorizeText(resources.getString(R.string.normal), GREEN_COLOR_CODE)
        val SEVERELY_LOW_TEXT = colorizeText(resources.getString(R.string.severely_low), RED_COLOR_CODE)
        val SEVERELY_HIGH_TEXT = colorizeText(resources.getString(R.string.severely_high), RED_COLOR_CODE)
        val HIGH_TEXT = colorizeText(resources.getString(R.string.high), YELLOW_COLOR_CODE)
        val LOW_TEXT = colorizeText(resources.getString(R.string.low), YELLOW_COLOR_CODE)

        var normalRates = 0
        val healthResult : HealthResult = viewModel.getLastHealth()!!
        val breathingRate = viewModel.getBreathingRate()
        textView_Oxygen.text = "${healthResult.oxygenSaturation}"
        textView_heartRate.text = "${healthResult.beatsPerMinute}"
        textView_respiration.text = "%.1f".format(breathingRate * 60)
        when {
            healthResult.oxygenSaturation < 90 -> {
//                 VERY LOW
                textView_Oxygen.setTextColor(SEVERE_COLOR)
                textView_OxygenText.setText(setTextHTML("${textView_OxygenText.text} ${SEVERELY_LOW_TEXT}"))
            }
            healthResult.oxygenSaturation < 95 -> {
//                 LOW
                textView_Oxygen.setTextColor(RISKY_COLOR)
                textView_OxygenText.setText(setTextHTML("${textView_OxygenText.text} ${LOW_TEXT}"))
            }
            else -> {
//                 normal
                textView_Oxygen.setTextColor(NORMAL_COLOR)
                textView_OxygenText.setText(setTextHTML("${textView_OxygenText.text} ${NORMAL_TEXT}"))
                normalRates++
            }
        }
        when {
            healthResult.beatsPerMinute < 60 -> {
//                 LOW
                textView_heartRate.setTextColor(RISKY_COLOR)
                textView_heartRateText.setText(setTextHTML("${textView_heartRateText.text} ${LOW_TEXT}"))
            }
            healthResult.beatsPerMinute < 100 -> {
//                 NORAML
                textView_heartRate.setTextColor(NORMAL_COLOR)
                textView_heartRateText.setText(setTextHTML("${textView_heartRateText.text} ${NORMAL_TEXT}"))
                normalRates++
            }
            healthResult.beatsPerMinute < 120 -> {
//                 HIGH
                textView_heartRate.setTextColor(RISKY_COLOR)
                textView_heartRateText.setText(setTextHTML("${textView_heartRateText.text} ${HIGH_TEXT}"))
            }
            else -> {
//                 VERY HIGH
                textView_heartRate.setTextColor(RISKY_COLOR)
                textView_heartRateText.setText(setTextHTML("${textView_heartRateText.text} ${SEVERELY_HIGH_TEXT}"))
            }
        }
        when {
            breathingRate < 5 -> {
//                 LOW
                textView_respiration.setTextColor(RISKY_COLOR)
                textView_respirationText.setText(setTextHTML("${textView_respirationText.text} ${LOW_TEXT}"))
            }
            breathingRate < 15 -> {
//                 NORMAL
                textView_respiration.setTextColor(NORMAL_COLOR)
                textView_respirationText.setText(setTextHTML("${textView_respirationText.text} ${NORMAL_TEXT}"))
                normalRates++
            }
            breathingRate < 20 -> {
//                 HIGH
                textView_respiration.setTextColor(RISKY_COLOR)
                textView_respirationText.setText(setTextHTML("${textView_respirationText.text} ${HIGH_TEXT}"))
            }
            else -> {
//                 VERY HIGH
                textView_respiration.setTextColor(SEVERE_COLOR)
                textView_respirationText.setText(setTextHTML("${textView_respirationText.text} ${SEVERELY_HIGH_TEXT}"))
            }
        }

        if (normalRates <= 1) {
            textView_summaryText.text = resources.getString(R.string.call_mda)
            imageView_summary.setImageResource(R.drawable.attention)
        }
    }
}