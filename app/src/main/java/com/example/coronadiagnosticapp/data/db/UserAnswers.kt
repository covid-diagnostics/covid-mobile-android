package com.example.coronadiagnosticapp.data.db

import java.util.*

class UserAnswers(
    val tempMeasurement: String?,
    val exposureDate: Date?,
    val positiveTestDate: Date?,
    val negativeTestDate: Date?,
    val generalFeeling: Feeling
)

enum class Feeling {
    SAME, BETTER, WORSE
}