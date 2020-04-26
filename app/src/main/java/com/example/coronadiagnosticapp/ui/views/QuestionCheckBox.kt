package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import com.example.coronadiagnosticapp.data.db.Question

class QuestionCheckBox : AppCompatCheckBox {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var question: Question? = null

    fun setQuestion(question: Question) {
        this.question = question
        with(question) {
            text = displayName
//            TODO add other things
        }
    }

}