package com.example.coronadiagnosticapp.ui.fragments.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import javax.inject.Inject

class SplashFragment : Fragment() {

    @Inject
    lateinit var viewModel: SplashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_splash, container, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isLoggedIn = viewModel.isLoggedIn()
        viewModel.setIsFirstTime(!isLoggedIn)
        val id = if (isLoggedIn) {
            R.id.action_splashFragment_to_homeFragment
        } else {
            R.id.action_splashFragment_to_welcomeFragment
        }
        findNavController().navigate(id)
    }
}
