package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.question.Question
import com.example.coronadiagnosticapp.utils.toDP

class QuestionCheckBoxView : CheckBox, QuestionPresenter {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override var question: Question? = null
        set(value) {
            field = value
            text = value?.displayName
//        TODO maybe use question image
        }

    init {
        setButtonDrawable(R.drawable.radio_btn_selector)
        val padd = 8.toDP(context)
        setPadding(padd, 0, padd, 0)
        textSize = 16f
        LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            setMargins(left, top, right, 16.toDP(context))
        }
    }

}