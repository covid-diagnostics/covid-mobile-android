package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.GeneralFeeling
import com.example.coronadiagnosticapp.utils.toDP
import kotlinx.android.synthetic.main.enum_question_view.view.*


class FeelingQuestionView : LinearLayout {

    var selectedFeeling: GeneralFeeling = GeneralFeeling.SAME

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val arr = context.obtainStyledAttributes(
            attrs, R.styleable.FeelingQuestionView, 0, 0
        )

        question_tv.text = arr.getString(R.styleable.FeelingQuestionView_enum_question)

        arr.recycle()
    }

    init {
        View.inflate(context, R.layout.enum_question_view, this)

        addFeelings()

        options_group.setOnCheckedChangeListener { _, checkedId ->
            val feel = GeneralFeeling.values().getOrNull(checkedId)
            if (feel != null)
                selectedFeeling = feel
        }
    }

    private fun addFeelings() = GeneralFeeling.values().forEach(this::addFeeling)

    private fun addFeeling(feeling: GeneralFeeling) {
        val btn = EkRadioBtn(context).apply {
            id = feeling.ordinal
            text = feeling.getString(context)
            val params = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            params.setMargins(0, 0, 0, 16.toDP(context))
            layoutParams = params
        }

        options_group.addView(btn)
    }
}