package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "measurement_table")
data class Measurement(
    @PrimaryKey val id: Int? = null,
    val filledOn: Date? = null,
    val tag: String? = null,
    val tempMeasurement: String? = null,
    val exposureDate: String? = null,
    val positiveTestDate: String? = null,
    val negativeTestDate: String? = null,
    val generalFeeling: String? = null
)
