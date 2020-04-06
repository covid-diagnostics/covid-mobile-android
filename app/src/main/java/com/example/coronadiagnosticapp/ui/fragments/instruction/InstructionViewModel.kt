package com.example.coronadiagnosticapp.ui.fragments.instruction

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class InstructionViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    fun getUserName() = repository.getUserName()
}
