package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.Camera
import org.apache.commons.beanutils.converters.IntegerConverter

class OxymeterData (var oxSaturation: Int, var heartRate: Int, var breathRate: Int) {}

interface Oxymeter {
    fun updateWithFrame(data: ByteArray, cam: Camera)
    fun finish(totalTimeInSecs: Double, samplingFreq: Double): OxymeterData? // returns null in case of invalid measurement
    fun setOnBadFinger(callback: () -> Unit)
    fun setUpdateView(callback: (heartRate: Int) -> Unit)
}
