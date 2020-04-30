package com.example.coronadiagnosticapp.ui.fragments.questions.viewmodels

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.Converters
import com.example.coronadiagnosticapp.data.db.entity.ExtraData
import com.example.coronadiagnosticapp.data.db.entity.Question
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class QuestionFragmentViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    suspend fun sendData() = repository.sendUserAnswers()

    private var currentQuestion: Question? = null


    suspend fun getNextQuestion(): Question? {
        currentQuestion =
            repository.getNextSelectableQuestion(currentQuestion)

        return currentQuestion
    }

    suspend fun saveSelected(
        questionId: Long,
        selected: List<ExtraData>
    ) {
        val json = Converters().fromList(selected)
        val answer = AnswersResponse(questionId, json, questionId)
        repository.addAnswer(answer)
    }
}
