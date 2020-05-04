package com.example.coronadiagnosticapp.ui.fragments.information

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.db.entity.Sex
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class InformationViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    val error = repository.error
    suspend fun updateUserPersonalInformation(
        sex: Sex, age: Int, height: Int, weight: Int
    ) = repository.updateUserPersonalInformation(sex, age,height,weight)
}
