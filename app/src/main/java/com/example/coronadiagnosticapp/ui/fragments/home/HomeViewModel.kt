package com.example.coronadiagnosticapp.ui.fragments.home

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class HomeViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    fun isLoggedIn() = repository.isLoggedIn()
    var firstTime: Boolean
        get() = repository.getIsFirstTime()
        set(value) = repository.setIsFirstTime(value)

    suspend fun getNumChecks(): Int = repository.getNumberOfMeasurements()
}


