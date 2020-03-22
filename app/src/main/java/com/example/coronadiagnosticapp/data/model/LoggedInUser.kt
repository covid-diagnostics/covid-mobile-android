package com.example.coronadiagnosticapp.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String
)
