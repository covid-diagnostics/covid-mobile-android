package com.example.coronadiagnosticapp.ui.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_welcome.*
import javax.inject.Inject

class WelcomeFragment : Fragment() {
    @Inject
    lateinit var viewModel: WelcomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_welcome, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textView_welcome_title.text = getText(R.string.your_voice_can_win_corona);

        button_start.setOnClickListener {

            val isLoggedIn = viewModel.isLoggedIn()
            val id = if (isLoggedIn) {
                R.id.action_welcomeFragment_to_homeFragment
            } else {
                R.id.action_welcomeFragment_to_registerFragment
            }
            viewModel.setIsFirstTime( !isLoggedIn)
            findNavController().navigate(id)
        }
        AutostartUtils.requestAutostartPermissions(context)
    }

}
