package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.Question

class QuestionCheckBox : CheckBox, QuestionPresenter {

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
        }

    init {
        textSize = 16f
        val tintColor = resources.getColor(R.color.colorPrimaryDark)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
//        TODO add some swag
        (layoutParams as? LinearLayout.LayoutParams)?.apply {
            setMargins(left, top, right, 16)
        }

    }

}