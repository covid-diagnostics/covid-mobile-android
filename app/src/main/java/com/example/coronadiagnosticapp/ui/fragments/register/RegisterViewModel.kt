package com.example.coronadiagnosticapp.ui.fragments.register

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import com.example.coronadiagnosticapp.data.repository.Repository
import com.example.coronadiagnosticapp.data.repository.RepositoryImpl
import java.util.*
import javax.inject.Inject

class RegisterViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    val error = repository.error
    suspend fun registerUser(email: String, password: String) {
        val deviceId = UUID.randomUUID().toString()
        repository.registerUser(UserRegister(email, password, deviceId))
    }

    fun isLoggedIn():Boolean = repository.isLoggedIn()



}
