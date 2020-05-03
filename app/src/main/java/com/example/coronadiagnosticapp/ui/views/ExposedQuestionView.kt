package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.exposed_question_view.view.*
import java.util.*

class ExposedQuestionView : LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        View.inflate(context, R.layout.exposed_question_view,this)

        did_expose_view.setOnChoiceChangedListener { didExpose ->
            date_picker_view.visibility = if (didExpose) VISIBLE else GONE
        }
    }

    val exposedDate: Date?
        get() = date_picker_view.selectedDate

}