package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RadioGroup
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.R.styleable.*
import kotlinx.android.synthetic.main.yes_no_question_view.view.*

class YesNoQuestionView : LinearLayout, RadioGroup.OnCheckedChangeListener {

    constructor(context: Context) : super(context)
    private var changedCallback: YesNoCallback?
    var currentSelection: YesNo? = null
        set(value) {
            field = value
            when (field) {
                YesNo.YES -> yes_no_group.check(R.id.yes_radio_btn)
                YesNo.NO -> yes_no_group.check(R.id.no_radio_btn)
                YesNo.NON,null -> yes_no_group.clearCheck()
            }
        }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val arr = context.obtainStyledAttributes(
            attrs, YesNoQuestionView, 0, 0
        )

        yes_no_question_tv.text = arr.getString(YesNoQuestionView_yn_question)
        arr.getString(YesNoQuestionView_positiveText)?.let {
            yes_radio_btn.text = it
        }
        arr.getString(YesNoQuestionView_negativeText)?.let {
            no_radio_btn.text = it
        }

        arr.recycle()
    }

    init {
        inflate(context, R.layout.yes_no_question_view, this)
        changedCallback = null
        yes_no_group.setOnCheckedChangeListener(this)
    }

    fun setOnChoiceChangedListener(changedCallback: YesNoCallback) {
        this.changedCallback = changedCallback
    }
    enum class YesNo {
        YES, NO, NON

    }

    override fun onCheckedChanged(group: RadioGroup?, id: Int) {
        currentSelection = when (id) {
            R.id.yes_radio_btn -> {
                changedCallback?.invoke(true)
                YesNo.YES
            }
            R.id.no_radio_btn -> {
                changedCallback?.invoke(false)
                YesNo.NO
            }
            else -> {
                changedCallback?.invoke(false)
                YesNo.NON
            }
        }

    }
}
