package com.example.coronadiagnosticapp.ui.fragments.smoking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.SmokingStatus
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.fragment_smoking.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SmokingFragment : Fragment() {

    @Inject
    lateinit var viewModel: SmokingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_smoking, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        continue_btn.setOnClickListener { save(view) }
    }

    private fun save(view: View) {
        val smokingStatus =
            when (smoke_group.checkedRadioButtonId) {
                R.id.non_smoker_radio -> SmokingStatus.NEVER
                R.id.smoker_radio -> SmokingStatus.CURRENT
                R.id.smoker_5y_ago_radio -> SmokingStatus.STOPPED
                else->{
                    toast("Choose one option")
                    return
                }
            }
        view.isEnabled = false
//        todo show progress
        GlobalScope.launch(Dispatchers.IO) {
            viewModel.save(smokingStatus)

            withContext(Dispatchers.Main) {
                toast("saved")
                findNavController()
                    .navigate(R.id.action_smokingFragment_to_backgroundDiseasesFragment)
            }
        }

    }

}
