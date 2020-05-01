package com.example.coronadiagnosticapp.ui.fragments.questions.viewmodels

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.Converters
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class QuestionFragmentViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    suspend fun sendData() = repository.sendUserAnswers()

    private var currentQuestion: SelectQuestion? = null


    suspend fun getNextQuestion() =
        repository.getNextSelectableQuestion(currentQuestion).also {
            currentQuestion = it
        }


    suspend fun saveSelected(
        questionId: Long,
        selected: List<SelectQuestion.ExtraData>
    ) {
        val json = Converters().fromSelectList(selected)
        val answer = AnswersResponse(questionId, json, questionId)
        repository.addAnswer(answer)
    }
}
