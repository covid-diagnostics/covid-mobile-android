package com.example.coronadiagnosticapp.ui.fragments.notification

import androidx.lifecycle.ViewModel
import com.example.coronadiagnosticapp.data.repository.Repository
import javax.inject.Inject

class NotificationViewModel @Inject constructor(val repository: Repository) : ViewModel() {

    fun setNotification(didSave: Boolean) {
        repository.didSetNotificationTime = didSave
    }
}
