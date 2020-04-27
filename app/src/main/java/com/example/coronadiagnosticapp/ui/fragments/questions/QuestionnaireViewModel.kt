package com.example.coronadiagnosticapp.ui.fragments.questions

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.Question
import com.example.coronadiagnosticapp.data.db.QuestionType.*
import com.example.coronadiagnosticapp.data.db.UserAnswers
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class QuestionnaireViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    private var selectorsQuestions: MutableList<Question> = mutableListOf()
    private var simpleQuestions: MutableList<Question> = mutableListOf()
    private var answers = mutableListOf<UserAnswers>()
    private var currentQIndex = -1

    suspend fun getQuestions(): MutableList<Question> {
        if (simpleQuestions.isNotEmpty())
            return simpleQuestions


        val questions = repository.getQuestions()

        selectorsQuestions = mutableListOf()
        simpleQuestions = mutableListOf()

        questions.forEach {
            when (it.type) {
                MULTI_SELECT, SELECT ->
                    selectorsQuestions.add(it)

                CHECKBOX, TEXT ->
                    simpleQuestions.add(it)
            }
        }

        return simpleQuestions
    }

    fun updateData(userAnswers: UserAnswers) {
        answers.add(userAnswers)
    }

    suspend fun sendData() {
        repository.updateUserAnswers(answers)
    }

    val nextQuestion: Question?
        get() =
            if (currentQIndex < selectorsQuestions.size - 1) {
                currentQIndex++
                selectorsQuestions[currentQIndex]
            } else null


}
