package com.example.coronadiagnosticapp.ui.fragments.dailtyMetric

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class DailyMetricViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    val error = repository.error

    suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean){
        repository.updateUserMetrics(temp, cough, isWet)
    }
}
