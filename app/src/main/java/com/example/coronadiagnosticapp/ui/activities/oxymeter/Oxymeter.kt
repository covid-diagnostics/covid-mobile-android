package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.Camera
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class OxymeterData(var oxSaturation: Int, var heartRate: Int, var breathRate: Int) : Parcelable

interface Oxymeter {
    fun updateWithFrame(data: ByteArray, cam: Camera)
    fun finish(samplingFreq: Double): OxymeterData? // returns null in case of invalid measurement
    fun setOnInvalidData(callback: () -> Unit)
    fun setUpdateView(callback: (heartRate: Int) -> Unit)
    fun setUpdateGraphView(callback: (frame: Int, point: Double) -> Unit)
}
