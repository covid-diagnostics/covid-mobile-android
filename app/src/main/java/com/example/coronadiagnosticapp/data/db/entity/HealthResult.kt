package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "health_table")
data class HealthResult(
    @PrimaryKey val date: Date,
    val beatsPerMinute: Int,
    val breathsPerMinute: Int,
    val oxygenSaturation: Int
) {
    constructor(beatsPerMinute: Int, breathsPerMinute: Int, oxygenSaturation: Int) : this(
        Date(),
        beatsPerMinute,
        breathsPerMinute,
        oxygenSaturation
    )
}