package com.example.coronadiagnosticapp.ui.fragments.terms

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.register.RegisterFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_terms_and_conditions.*
import javax.inject.Inject


class TermsAndConditionsFragment : Fragment() {

    @Inject
    lateinit var viewModel : TermsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_terms_and_conditions, container, false)

    override fun onAttach(context: Context) {
        context.getAppComponent().inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        start_btn.setOnClickListener {
            viewModel.saveConsent()
            findNavController()
                .navigate(R.id.action_termsAndConditionsFragment2_to_registerFragment)
        }
    }

}
