package com.example.coronadiagnosticapp.ui.views

import android.app.DatePickerDialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.R.styleable.DateQuestionView
import kotlinx.android.synthetic.main.date_question_view.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateQuestionView : LinearLayout, DatePickerDialog.OnDateSetListener {

    var selectedDate: Date?
        set(value) {
            field = value
            value ?: return
            val formatted = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM).format(value)
            selected_date_tv.text = formatted
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val arr = context.obtainStyledAttributes(
            attrs, DateQuestionView,
            0, 0
        )

        if (arr.hasValue(R.styleable.DateQuestionView_date_question)) {
            question_tv.text = arr.getString(R.styleable.DateQuestionView_date_question)
        }

        arr.recycle()
    }

    init {
        View.inflate(context, R.layout.date_question_view, this)
        selectedDate = null

        select_date_btn.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val locale = Locale.getDefault()

        val cal = Calendar.getInstance(locale)
        val datePickerDialog = DatePickerDialog(
            context, this,
            cal[Calendar.YEAR],
            cal[Calendar.MONTH],
            cal[Calendar.DAY_OF_MONTH]
        )

        datePickerDialog.datePicker.maxDate = cal.time.time
//        set the minimum date to pick to last year
        datePickerDialog.datePicker.minDate = cal.apply {
            add(Calendar.YEAR,-1)
        }.time.time

        datePickerDialog.show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        selectedDate = Calendar.getInstance().apply {
            set(year, month, day)
        }.time
    }

}