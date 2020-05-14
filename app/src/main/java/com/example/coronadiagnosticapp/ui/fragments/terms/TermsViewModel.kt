package com.example.coronadiagnosticapp.ui.fragments.terms

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class TermsViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    fun saveConsent() {
        repository.hasConsent = true
    }
}


