package com.example.coronadiagnosticapp.ui.fragments.register

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class RegisterViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    val error = repository.error
    suspend fun registerUser(phoneNumberHash: String) {
        //val deviceId = UUID.randomUUID().toString()
        repository.registerUser(UserRegister(phoneNumberHash))
    }

    fun isLoggedIn(): Boolean = repository.isLoggedIn()


}
