package com.example.coronadiagnosticapp.ui.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_welcome.*
import javax.inject.Inject

class WelcomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_welcome, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        button_start.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment,RegisterFragment())
                .commit()
        }
        AutostartUtils.requestAutostartPermissions(context!!)
    }

}
