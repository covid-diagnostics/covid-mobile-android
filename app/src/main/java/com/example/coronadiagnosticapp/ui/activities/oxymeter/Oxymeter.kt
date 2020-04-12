package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import java.nio.ByteBuffer

class OxymeterData (var oxSaturation: Int, var heartRate: Int, var breathRate: Int) {}

interface Oxymeter {
    fun updateWithFrame(data: Array<Double>)
    fun finish(totalTimeInSecs: Double): OxymeterData? // returns null in case of invalid measurement
    fun setOnBadFinger(callback: () -> Unit)
}
