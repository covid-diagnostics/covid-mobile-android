package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "metric_table")
data class ResponseMetric(
    val coughStrength: Int,
    val filledOn: String?,
    @PrimaryKey val id: Int,
    val isCoughDry: Boolean,
    val temperature: String
)

data class SendMetric(
    val coughStrength: Int, val isCoughDry: Boolean,
    val temperature: String
)