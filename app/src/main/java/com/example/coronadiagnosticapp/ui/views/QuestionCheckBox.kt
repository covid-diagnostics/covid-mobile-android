package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.CheckBox
import com.example.coronadiagnosticapp.data.db.Question

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
//        TODO add some swag
    }

}