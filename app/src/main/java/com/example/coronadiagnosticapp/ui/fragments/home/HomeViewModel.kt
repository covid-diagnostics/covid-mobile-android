package com.example.coronadiagnosticapp.ui.fragments.home

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class HomeViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    suspend fun getNumChecks(): Int = repository.getMeasurementCount()
}


