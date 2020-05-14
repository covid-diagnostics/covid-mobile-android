package com.example.coronadiagnosticapp.ui.fragments.splash

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class SplashViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    fun isLoggedIn() = repository.isLoggedIn()
    fun setIsFirstTime(isFirstTime: Boolean) {
        repository.isFirstTime = isFirstTime
    }
}


