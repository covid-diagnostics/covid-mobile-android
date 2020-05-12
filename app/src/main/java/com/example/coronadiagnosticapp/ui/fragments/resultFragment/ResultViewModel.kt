package com.example.coronadiagnosticapp.ui.fragments.resultFragment

import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class ResultViewModel @Inject constructor(val repository: Repository) {
    suspend fun getMeasurementCount() = repository.getMeasurementCount()
    suspend fun getMeasurementId() = repository.getLastMeasurementId()

}