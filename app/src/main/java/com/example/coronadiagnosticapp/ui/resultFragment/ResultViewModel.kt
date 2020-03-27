package com.example.coronadiagnosticapp.ui.resultFragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject
import javax.inject.Singleton

class ResultViewModel @Inject constructor(val repository: Repository) : ViewModel() {

     fun getLastResult():LiveData<HealthResult> = repository.getLastResult()

}
