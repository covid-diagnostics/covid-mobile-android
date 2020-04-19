package com.example.coronadiagnosticapp.ui.fragments.resultFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.example.coronadiagnosticapp.MyApplication

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.result_fragment.*
import javax.inject.Inject

class ResultFragment : ScopedFragment() {
    @Inject
    lateinit var viewModel: ResultViewModel

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
        return inflater.inflate(R.layout.result_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getLastHealth().observe(viewLifecycleOwner, Observer { healthResult ->

            textView_Oxygen.text = "${textView_Oxygen.text} ${healthResult.oxygenSaturation}"
            textView_heartRate.text = "${textView_heartRate.text} ${healthResult.beatsPerMinute}"
            textView_respination.text =
                "${textView_respination.text} ${healthResult.breathsPerMinute}"

        })


    }

}