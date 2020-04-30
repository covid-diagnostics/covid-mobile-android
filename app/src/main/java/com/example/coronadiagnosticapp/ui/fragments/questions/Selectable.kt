package com.example.coronadiagnosticapp.ui.fragments.questions

import com.example.coronadiagnosticapp.data.db.entity.ExtraData

//TODO could be generic for other types of selection obj
interface Selectable {
    fun getSelected(): List<ExtraData>
}