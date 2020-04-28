package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.Measurement
import com.example.coronadiagnosticapp.data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class OxymeterViewModel @Inject
constructor(val repository: Repository) : ViewModel() {

    suspend fun submitMeasurement(measurement: Measurement): Measurement {
        return repository.submitMeasurement(measurement)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun submitPpgMeasurement(
        red: Array<Int>?,
        green: Array<Int>?,
        blue: Array<Int>?,
        timepoint: Array<Float>?,
        cc: CameraCharacteristics,
        measurement: Int
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val m = submitMeasurement(Measurement())
            repository.submitPpgMeasurement(red, green, blue, timepoint, cc, m.id!!)
        }
    }
}
