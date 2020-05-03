package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.question.Question
import kotlinx.android.synthetic.main.question_view.view.*

class QuestionView : LinearLayout, TextWatcher, QuestionPresenter {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        View.inflate(context, R.layout.question_view, this)
        answer_et.addTextChangedListener(this)
    }

    override var question: Question? = null
        set(value) {
            field = value
            title_tv.text = value?.displayName
        }

    fun getAnswer() = answer_et.text.toString()


    override fun afterTextChanged(editable: Editable) {
        if (editable.isEmpty()) {
            answer_et.error = "Answer something"
            return
        }

        if (editable.length < 2) {
            answer_et.error = "Answer must be at least 2 characters long"
            return
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


}