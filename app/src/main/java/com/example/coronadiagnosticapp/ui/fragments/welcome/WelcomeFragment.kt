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
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.textView_welcome_title.setText(getText(R.string.your_voice_can_win_corona));

        button_start.setOnClickListener {
            if (viewModel.isLoggedIn()) {
                this.viewModel.setIsFirstTime(false);
                findNavController().navigate(R.id.action_welcomeFragment_to_instructionsFragment)
            } else {
                this.viewModel.setIsFirstTime(true);
                findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
            }
        }
        AutostartUtils.requestAutostartPermissions(context)
    }

}
