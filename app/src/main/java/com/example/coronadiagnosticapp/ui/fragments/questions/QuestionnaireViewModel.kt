package com.example.coronadiagnosticapp.ui.fragments.questions

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.CHECKBOX
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.TEXT
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class QuestionnaireViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    suspend fun getQuestions() =
        repository.getQuestions().filter {
            when (it.type) {
                TEXT, CHECKBOX -> true
                else -> false
            }
        }
}
