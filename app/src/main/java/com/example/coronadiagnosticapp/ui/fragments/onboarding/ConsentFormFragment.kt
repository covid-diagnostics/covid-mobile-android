package com.example.coronadiagnosticapp.ui.fragments.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.terms.TermsAndConditionsFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_fourth.*
import javax.inject.Inject


class ConsentFormFragment : Fragment() {

    @Inject
    lateinit var viewModel:ConsentFormViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_fourth, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_start.setOnClickListener {

            val id = if (viewModel.hasConsent) {
                R.id.action_onBoardingMainFragment_to_registerFragment
            } else {
                R.id.action_onBoardingMainFragment_to_termsAndConditionsFragment
            }
            findNavController().navigate(id)

        }
    }
}
