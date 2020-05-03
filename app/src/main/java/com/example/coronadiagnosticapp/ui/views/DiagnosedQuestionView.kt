package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.diagnosed_question_view.view.*
import java.util.*

class DiagnosedQuestionView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.diagnosed_question_view, this)

        did_check_corona.setOnChoiceChangedListener { isPositive ->
            corona_result.visibility = if (isPositive) VISIBLE else GONE
            date_picker_view.visibility = if (isPositive) VISIBLE else GONE
        }
    }

    val checkedDates: Pair<Date?, Date?>
        get() {
            val selectedDate = date_picker_view.selectedDate
            return when (did_check_corona.currentSelection) {
                YesNoQuestionView.YesNo.YES -> Pair(selectedDate, null)
                YesNoQuestionView.YesNo.NO -> Pair(null, selectedDate)
                YesNoQuestionView.YesNo.NON, null -> Pair(null, null)
            }
        }

}