package com.example.coronadiagnosticapp.ui.fragments.questions

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class QuestionsViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    suspend fun getQuestions() = repository.getSimpleQuestions()
}