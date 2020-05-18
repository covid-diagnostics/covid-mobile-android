package com.example.coronadiagnosticapp.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.util.hide
import com.afollestad.vvalidator.util.show
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        start_btn.setOnClickListener {
            findNavController().navigate( R.id.action_homeFragment_to_dailyMetricFragment)
        }
        start_btn.isEnabled = false

            progressBar.show()
        num_checks_tv.hide()
        GlobalScope.launch(Dispatchers.IO) {
            val count = viewModel.getNumChecks()

            withContext(Dispatchers.Main) {
                progressBar.hide()
                val text = getString(R.string.checks_have_been_taken_so_far, count)
                num_checks_tv.setSpanText(text)
                num_checks_tv.show()
                start_btn.isEnabled = true
            }
        }

    }
}
