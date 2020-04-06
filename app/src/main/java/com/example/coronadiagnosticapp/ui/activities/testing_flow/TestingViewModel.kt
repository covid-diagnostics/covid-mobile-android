package com.example.coronadiagnosticapp.ui.activities.testing_flow

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestingViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    suspend fun insertBasicsInformation(basicsInformation: BasicsInformation) {
        repository.insertBasicsInformation(basicsInformation)
    }

    fun getBasicsInformation() = repository.getBasicsInformationExist()

}