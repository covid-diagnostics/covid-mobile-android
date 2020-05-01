package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.toDP

class EkRadioBtn : RadioButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.EkRadioBtn,
            0, 0
        ).apply {

            text = getText(R.styleable.EkRadioBtn_description)

            recycle()
        }
    }

    init {
        setButtonDrawable(R.drawable.radio_btn_selector)
        val padd = 8.toDP(context)
        setPadding(padd, 0, padd, 0)
        textSize = 16f
    }

}