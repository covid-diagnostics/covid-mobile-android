package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.widget.TextView
import com.example.coronadiagnosticapp.utils.asHTML

class SpannableTextView : TextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        setSpanText(text)
    }

    fun setSpanText(newText: CharSequence) {
        text = newText.toString().asHTML
    }
}