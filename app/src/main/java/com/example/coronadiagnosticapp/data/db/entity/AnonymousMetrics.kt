package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anonymous_metrics_table")
data class AnonymousMetrics(
    val appHeartRate: Int,
    val deviceHeartRate: Int?,
    val appSaturation: Int,
    val deviceSaturation: Int?,
    val deviceType: String,
    var measurementMethod: String,
    var position: String,
    var lighting: String,
    val age: Int,
    val pastMedicalStatus: String,
    @PrimaryKey val id: Int,
    val filledOn: String
) {
    constructor(
        appHeartRate: Int,
        deviceHeartRate: Int?,
        appSaturation: Int,
        deviceSaturation: Int?,
        deviceType: String,
        measurementMethod: String,
        position: String,
        lighting: String,
        age: Int,
        pastMedicalStatus: String
    ) : this(
        appHeartRate,
        deviceHeartRate,
        appSaturation,
        deviceSaturation,
        deviceType,
        measurementMethod,
        position,
        lighting,
        age,
        pastMedicalStatus,
        0,
        ""
    )
}