package com.example.coronadiagnosticapp.ui.fragments.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import kotlinx.android.synthetic.main.fragment_fourth.*


class FourthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_fourth, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_start.setOnClickListener {

            AutostartUtils.requestAutostartPermissions(context!!)

            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, RegisterFragment())
                .commit()
        }
    }
}
