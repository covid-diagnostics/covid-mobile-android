package com.example.coronadiagnosticapp.ui.fragments.onboarding

import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class ConsentFormViewModel @Inject constructor(repository: Repository){
    val hasConsent = repository.hasConsent
}
