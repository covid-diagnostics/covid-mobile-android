package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.toDP

class EkRadioBtn : RadioButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        setButtonDrawable(R.drawable.radio_btn_selector)
        val padd = 8.toDP(context)
        setPadding(padd, 0, padd, 0)
        textSize = 14f
    }

}