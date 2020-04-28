package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ppgmeasurement_table")
data class PpgMeasurement(
    @PrimaryKey val id: Int?,
    val red: Array<Int>?,
    val green: Array<Int>?,
    val blue: Array<Int>?,
    val timepoint: Array<Float>?,
    val calibrationTransform1: String?,
    val calibrationTransform2: String?,
    val sensorColorTransform1: String?,
    val sensorColorTransform2: String?,
    val sensorForwardMatrix1: String?,
    val sensorForwardMatrix2: String?,
    val sensorLensShadingApplied: Boolean?,
    val sensorSensitivityRangeLower: Int?,
    val sensorSensitivityRangeUpper: Int?,
    val sensorWhiteLevel: Int?,
    val sensorMaxAnalogSensitivity: Int?,
    val sensorReferenceIlluminant1: Int?,
    val sensorReferenceIlluminant2: Byte?,
    val measurement: Int
)
