package com.example.coronadiagnosticapp.ui.fragments.smoking

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.SmokingStatus
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class SmokingViewModel @Inject constructor(val repository: Repository) : ViewModel() {
    suspend fun save(smokingStatus: SmokingStatus) {
        repository.saveSmokeStatus(smokingStatus)
    }

}
