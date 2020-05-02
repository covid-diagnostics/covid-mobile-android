package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.temp_question.view.*

class TemperatureQuestionView : LinearLayout {

    val temperature: Double?
        get() = temp_et.text.toString().toDoubleOrNull()

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        inflate(context, R.layout.temp_question, this)

        temp_ynq.setOnChoiceChangedListener {
            temp_group.visibility = if (it) View.VISIBLE else View.GONE
        }
    }
}