package com.example.coronadiagnosticapp.ui.fragments.dailtyMetric

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.GeneralFeeling
import com.example.coronadiagnosticapp.data.db.entity.Measurement
import com.example.coronadiagnosticapp.data.repository.Repository
import java.util.*
import javax.inject.Inject

class DailyMetricViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    val error = repository.error

    suspend fun submitMeasurement(
        temp: Double?,
        exposureDate: Date?,
        positiveTestDate: Date?,
        negativeTestDate: Date?,
        feeling: GeneralFeeling
    ){
        val measurement = Measurement(
            tempMeasurement = temp,
            exposureDate = exposureDate,
            positiveTestDate = positiveTestDate,
            negativeTestDate = negativeTestDate,
            generalFeeling = feeling
        )

        repository.submitMeasurement(measurement)
    }
}
