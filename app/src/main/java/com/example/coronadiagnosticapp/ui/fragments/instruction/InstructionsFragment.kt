package com.example.coronadiagnosticapp.ui.fragments.instruction

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

import com.example.coronadiagnosticapp.R
import kotlinx.android.synthetic.main.instructions_fragment.*
import javax.inject.Inject

class InstructionsFragment : Fragment() {



    @Inject lateinit var viewModel: InsturcationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.instructions_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        button_instructions.setOnClickListener {
            findNavController().navigate(R.id.action_instructionsFragment_to_dailyMetricFragment)
        }
    }

}
