package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.step_view.view.*

class StepView:LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs){
        val arr = context.obtainStyledAttributes(
            attrs, R.styleable.StepView, 0, 0
        )
        arr.getString(R.styleable.StepView_stepDescription)?.let {
            desc_tv.text = it
        }
        arr.getString(R.styleable.StepView_stepNumber)?.let {
            number_circle.text = it
        }

        arr.recycle()
    }

    init {
        View.inflate(context, R.layout.step_view,this)
    }
}