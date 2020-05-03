package com.example.coronadiagnosticapp.ui.activities.oxymeter

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.Measurement
import com.example.coronadiagnosticapp.data.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class OxymeterViewModel @Inject
constructor(val repository: Repository) : ViewModel() {

    fun submitPpgMeasurement(
        oxymeterAverages: OxymeterAverages,
        cc: CameraCharacteristics
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Also submit PPG measurement
                repository.submitPpgMeasurement(oxymeterAverages, cc)
            }
        }
    }
}
