package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.Camera

class OxymeterData (var oxSaturation: Int, var heartRate: Int, var breathRate: Int) {}

interface Oxymeter {
    fun updateWithFrame(data: ByteArray, cam: Camera)
    fun finish(): OxymeterData? // returns null in case of invalid measurement
    fun setOnBadFinger(callback: () -> Unit)
}
