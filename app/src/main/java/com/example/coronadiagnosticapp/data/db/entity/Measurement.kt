package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "measurement_table")
data class Measurement(
    @PrimaryKey
    val id: Int? = null,
    val filledOn: Date? = null,
    val tag: String? = null,
    val tempMeasurement: Double? = null,
    val exposureDate: Date? = null,
    val positiveTestDate: Date? = null,
    val negativeTestDate: Date? = null,
    val generalFeeling: GeneralFeeling = GeneralFeeling.SAME
)

enum class GeneralFeeling{
    SAME, BETTER, WORSE
}