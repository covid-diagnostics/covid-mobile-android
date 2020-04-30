package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.GONE
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.diagnosed_question_view.view.*

class DiagnosedQuestionView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    init {
        View.inflate(context, R.layout.diagnosed_question_view,this)

        did_check_corona.setOnChoiceChangedListener { isPositive ->
            corona_result.visibility = if (isPositive) VISIBLE else GONE
        }
    }

//    TODO add other stuff to get results
}