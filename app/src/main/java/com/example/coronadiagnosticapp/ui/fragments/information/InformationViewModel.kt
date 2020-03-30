package com.example.coronadiagnosticapp.ui.fragments.information

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class InformationViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    val error = repository.error
    suspend fun updateUserPersonalInformation(firstName: String, lastName: String, age: Int) {
        repository.updateUserPersonalInformation(firstName, lastName, age)
    }
}
