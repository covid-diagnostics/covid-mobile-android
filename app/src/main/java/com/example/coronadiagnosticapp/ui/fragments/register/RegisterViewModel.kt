package com.example.coronadiagnosticapp.ui.fragments.register

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.example.coronadiagnosticapp.data.repository.Repository
import java.security.MessageDigest
import javax.inject.Inject

class RegisterViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    val error = repository.error
    suspend fun registerUser(phone: String) {
        repository.registerUser(UserRegister(hash(phone)))
    }

    fun isLoggedIn() = repository.isLoggedIn()


    private fun hash(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(text.toByteArray())
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    suspend fun setCountry(country: String) {
        repository.setCountry(country)
    }

}
