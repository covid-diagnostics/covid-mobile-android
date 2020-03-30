package com.example.coronadiagnosticapp.data.providers

interface TokenProvider {
     fun getToken(): String?
     fun setToken(token: String?)
}