package com.example.coronadiagnosticapp.ui.fragments.instruction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.instructions_fragment.*
import javax.inject.Inject

class InstructionsFragment : Fragment() {

    @Inject
    lateinit var viewModel: InstructionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.instructions_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textView_header_instructions.apply {
            text = "$text ${viewModel.getUserName()}"
//            TODO use string rss for with %s
        }
        button_instructions.setOnClickListener {

            findNavController().navigate(R.id.action_instructionsFragment_to_questioneerFragment)
//            findNavController().navigate(R.id.action_instructionsFragment_to_dailyMetricFragment)
        }
    }

}
