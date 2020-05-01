package com.example.coronadiagnosticapp.ui.fragments.questions.adapters

import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion


//TODO could be generic for other types of selection obj
interface Selectable {
    fun getSelected(): List<SelectQuestion.ExtraData>
}