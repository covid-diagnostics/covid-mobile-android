package com.example.coronadiagnosticapp.ui.fragments.notification

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.Reminder.RegisterNotificationService
import com.example.coronadiagnosticapp.ui.fragments.onboarding.AutostartUtils
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_notification.*
import javax.inject.Inject

class NotificationFragment : Fragment() {

    @Inject
    lateinit var viewModel: NotificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_notification, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        done_btn.setOnClickListener {
            AutostartUtils.requestAutostartPermissions(context!!)
            saveTime()
            goHome()
        }

        set_it_later_btn.setOnClickListener {
            viewModel.setNotification(false)
            goHome()
        }
    }

    private fun saveTime() {
        val hour: Int
        val minute: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hour = time_picker.hour
            minute = time_picker.minute
        } else {
            hour = time_picker.currentHour
            minute = time_picker.currentMinute
        }
        val intent = Intent(context!!.applicationContext,
            RegisterNotificationService::class.java)
            .putExtra("hour",hour)
            .putExtra("minute",minute)

        context!!.startService(intent)

        viewModel.setNotification(true)
    }

    private fun goHome() =
        findNavController()
            .navigate(R.id.action_notificationFragment_to_homeFragment)
}
