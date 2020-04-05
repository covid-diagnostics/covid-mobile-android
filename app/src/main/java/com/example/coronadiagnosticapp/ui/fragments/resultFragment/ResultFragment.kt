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
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.result_fragment.*
import javax.inject.Inject

class ResultFragment : ScopedFragment() {
    val NORMAL_COLOR = Color.parseColor("green")
    val RISKY_COLOR = Color.parseColor("yellow")
    val SEVERE_COLOR = Color.parseColor("red")
    val NORMAL_TEXT = colorizeText("normal", "green")
    val SEVERELY_LOW_TEXT = colorizeText("severely low", "red")
    val SEVERELY_HIGH_TEXT = colorizeText("severely high", "red")
    val HIGH_TEXT = colorizeText("high", "yellow")
    val LOW_TEXT = colorizeText("low", "yellow")
    @Inject
    lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
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

        var normalRates = 0

        viewModel.getLastHealth().observe(viewLifecycleOwner, Observer { healthResult ->
            textView_Oxygen.text = "${healthResult.oxygenSaturation}"
            textView_heartRate.text = "${healthResult.beatsPerMinute}"
            textView_respiration.text = "${healthResult.breathsPerMinute}"
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
                healthResult.breathsPerMinute < 5 -> {
//                 LOW
                    textView_respiration.setTextColor(RISKY_COLOR)
                    textView_respirationText.setText(setTextHTML("${textView_respirationText.text} ${LOW_TEXT}"))
                }
                healthResult.breathsPerMinute < 15 -> {
//                 NORMAL
                    textView_respiration.setTextColor(NORMAL_COLOR)
                    textView_respirationText.setText(setTextHTML("${textView_respirationText.text} ${NORMAL_TEXT}"))
                    normalRates++
                }
                healthResult.breathsPerMinute < 20 -> {
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
                textView_summaryText.text = "An MDA representative will contact you as soon as possible"
                imageView_summary.setImageResource(R.drawable.attention)
            }
        })


    }

}