package com.example.coronadiagnosticapp.data.db.entity.responseMetric

data class SendMetric(
    val coughStrength: Int, val isCoughDry: Boolean,
    val temperature: String
)