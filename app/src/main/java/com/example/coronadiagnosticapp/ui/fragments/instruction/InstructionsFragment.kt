package com.example.coronadiagnosticapp.ui.fragments.instruction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.instructions_fragment.*
import javax.inject.Inject

class InstructionsFragment : Fragment() {

    @Inject
    lateinit var viewModel: InstructionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            it.applicationContext.let { ctx ->
                (ctx as MyApplication).getAppComponent().inject(this)
            }
            it.findViewById<StepperIndicator>(R.id.stepperIndicator)?.apply {
                visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.instructions_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textView_header_instructions.apply {
            text = "$text ${viewModel.getUserName()}"
        }
        button_instructions.setOnClickListener {
            findNavController().navigate(R.id.action_instructionsFragment_to_dailyMetricFragment)
        }
    }

}
