package com.example.coronadiagnosticapp.ui.fragments.recorder

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class RecorderViewModel @Inject constructor() : ViewModel() {

    suspend fun uploadFile(file: File){
        delay((3000))
    }
}
