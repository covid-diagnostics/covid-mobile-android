package com.example.coronadiagnosticapp.ui.fragments.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class WelcomeFragment : Fragment() {
    @Inject
    lateinit var viewModel: WelcomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // TODO: Delete this:
        findNavController().navigate(R.id.action_welcomFragment_to_informationFragment)

        button_start.setOnClickListener {
            when (viewModel.isLoggedIn()) {
                true -> findNavController().navigate(R.id.action_welcomeFragment_to_instructionsFragment)
                false -> findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
            }
        }
    }

}
