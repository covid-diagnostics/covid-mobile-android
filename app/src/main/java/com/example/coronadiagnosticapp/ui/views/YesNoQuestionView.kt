package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StyleableRes
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.R.styleable.*
import kotlinx.android.synthetic.main.yes_no_question_view.view.*

class YesNoQuestionView : LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val arr = context.obtainStyledAttributes(
            attrs, YesNoQuestionView, 0, 0
        )

        setValueFromXml(arr, yes_no_question_tv, YesNoQuestionView_question)
        setValueFromXml(arr, yes_tv, YesNoQuestionView_positiveText)
        setValueFromXml(arr, no_tv, YesNoQuestionView_negativeText)

        arr.recycle()
    }

    private fun setValueFromXml(array: TypedArray, textView: TextView, @StyleableRes res: Int) {
        if (array.hasValue(res)) {
            array.getText(res)?.let { textView.text = it }
        }
    }

    init {
        inflate(context, R.layout.yes_no_question_view, this)
    }

    fun setOnChoiceChangedListener(changedCallback: YesNoCallback) =
        yes_no_group.setOnCheckedChangeListener { _, i ->
            val answer = when (i) {
                R.id.yes_radio_btn -> true
                R.id.no_radio_btn -> false
                else -> false
            }
            changedCallback(answer)
        }
}
