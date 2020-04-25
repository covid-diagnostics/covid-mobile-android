package com.example.coronadiagnosticapp.ui.fragments.resultFragment

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.R.color.*
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.result_fragment.*
import javax.inject.Inject

class ResultFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: ResultViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.result_fragment, container, false)

    private fun setTextHTML(html: String) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    }

    //    TODO move to HTML Helper
    private fun colorizeText(@StringRes textRes: Int, @ColorRes colorRes: Int): String {
        val colorInt = resources.getColor(colorRes)
        val hexColor = Integer.toHexString(colorInt)
        val text = resources.getText(textRes)
        return "<font color='#$hexColor'>$text</font>"
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val normalText = colorizeText(R.string.normal, colorNormal)

        val severelyLowText = colorizeText(R.string.severely_low, colorSevere)

        val severelyHighText = colorizeText(R.string.severely_high, colorSevere)

        val highText = colorizeText(R.string.high, colorRisky)

        val lowText = colorizeText(R.string.low, colorRisky)

        var normalRates = 0
        val healthResult: HealthResult = viewModel.getLastHealth()!!
        val breathingRate = viewModel.getBreathingRate()

        val oxygenSaturation = healthResult.oxygenSaturation
        textView_Oxygen.text = "$oxygenSaturation"

        val beatsPerMinute = healthResult.beatsPerMinute
        textView_heartRate.text = "$beatsPerMinute"

        textView_respiration.text = "%.1f".format(breathingRate * 60)

        when {
//                 VERY LOW
            oxygenSaturation < 90 -> setOxygenText(severelyLowText, colorSevere)
//                 LOW
            oxygenSaturation < 95 -> setOxygenText(lowText, colorRisky)
            else -> {
//                 NORMAL
                setOxygenText(normalText, colorNormal)
                normalRates++
            }
        }
        when {
//                 LOW
            beatsPerMinute < 60 -> setHRText(lowText, colorRisky)
            beatsPerMinute < 100 -> {
//                 NORMAL
                setHRText(normalText, colorNormal)
                normalRates++
            }
//                 HIGH
            beatsPerMinute < 120 -> setHRText(highText, colorRisky)
            //                 VERY HIGH
            else -> setHRText(severelyHighText, colorRisky)
        }
        when {
            //                 LOW
            breathingRate < 5 -> setRespirationText(lowText, colorRisky)
            breathingRate < 15 -> {
//                 NORMAL
                setRespirationText(normalText, colorNormal)
                normalRates++
            }
            //                 HIGH
            breathingRate < 20 -> setRespirationText(highText, colorRisky)
            //                 VERY HIGH
            else -> setRespirationText(severelyHighText, colorSevere)
        }

        if (normalRates <= 1) {
            textView_summaryText.text = resources.getString(R.string.call_mda)
            imageView_summary.setImageResource(R.drawable.attention)
        }
    }

    private fun setOxygenText(state: String, @ColorRes color: Int) =
        setTextWithColor(textView_Oxygen, textView_OxygenText, state, color)

    private fun setRespirationText(state: String, @ColorRes color: Int) =
        setTextWithColor(textView_Oxygen, textView_OxygenText, state, color)

    private fun setHRText(state: String, @ColorRes color: Int) =
        setTextWithColor(textView_heartRate, textView_heartRateText, state, color)


    private fun setTextWithColor(
        textViewToColor: TextView, textView: TextView,
        state: String, @ColorRes colorRes: Int
    ) {
        val color = resources.getColor(colorRes)
        textViewToColor.setTextColor(color)
        textView.apply {
            text = setTextHTML("$text $state")
        }
    }
}