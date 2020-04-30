package com.example.coronadiagnosticapp.ui.fragments.questions.viewmodels

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.Question
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.CHECKBOX
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.TEXT
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class QuestionnaireViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    suspend fun getQuestions(): List<Question> {
        repository.loadQuestionsToDB()
        return repository.getQuestions(TEXT, CHECKBOX)
    }

    suspend fun addAnswers(answers: List<AnswersResponse>) {
        repository.addAnswers(answers)
    }

}
