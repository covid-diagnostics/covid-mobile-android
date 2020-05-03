package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.Camera
import java.util.*

class OxymeterData(var oxSaturation: Int, var heartRate: Int, var breathRate: Int) {}
class OxymeterAverages(var red: Array<Double>, var green: Array<Double>, var blue: Array<Double>, var timepoint: Array<Long>)

interface Oxymeter {
    fun updateWithFrame(data: ByteArray, cam: Camera)
    fun finish(samplingFreq: Double): OxymeterData? // returns null in case of invalid measurement
    fun setOnInvalidData(callback: () -> Unit)
    fun setUpdateView(callback: (heartRate: Int) -> Unit)
    fun setUpdateGraphView(callback: (frame: Int, point: Double) -> Unit)
    fun getAverages(): OxymeterAverages
}
