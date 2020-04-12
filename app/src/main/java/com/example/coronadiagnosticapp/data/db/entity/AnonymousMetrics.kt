package com.example.coronadiagnosticapp.data.db.entity

import java.io.File
import java.util.*

data class AnonymousMetrics(
        val id: Int,
        val filledOn: String,
        val appHeartRate: Int,
        val deviceHeartRate: Int?,
        val appSaturation: Int,
        val deviceSaturation: Int?,
        val deviceType: String,
        // position
        val measurementMethod: String,
        val lighting: String,
        val age: Int,
        val pastMedicalStatus: String,
        val file: File?
) {
    constructor(
            filledOn: Date,
            appHeartRate: Int,
            deviceHeartRate: Int?,
            appSaturation: Int,
            deviceSaturation: Int?,
            deviceType: String,
            measurementMethod: String,
            lighting: String,
            age: Int,
            pastMedicalStatus: String,
            file: File?
    ) : this(
            System.currentTimeMillis().toInt(),
            filledOn.toString(),
            appHeartRate,
            deviceHeartRate,
            appSaturation,
            deviceSaturation,
            deviceType,
            measurementMethod,
            lighting,
            age,
            pastMedicalStatus,
            file)
}