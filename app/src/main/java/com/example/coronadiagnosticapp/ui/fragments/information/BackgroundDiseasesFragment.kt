package com.example.coronadiagnosticapp.ui.fragments.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import kotlinx.android.synthetic.main.background_diseases_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackgroundDiseasesFragment : ScopedFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    @Inject
    lateinit var viewModel: InformationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.background_diseases_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        chronic_kidney_disease.setOnCheckedChangeListener { compoundButton: CompoundButton, isChecked: Boolean ->
            if(isChecked)
                chronic_kidney_disease_yes_no.visibility = VISIBLE
            else
                chronic_kidney_disease_yes_no.visibility = GONE
        }


        button_background_diseases_next.setOnClickListener {
            submit()
        }

        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun submit(){
        showLoading(show = true)
        launch(Dispatchers.IO) {
            viewModel.updateBackgroundDiseases(getBackgroundDiseases())
            withContext(Dispatchers.Main) {
                showLoading(false)
                findNavController().navigate(R.id.action_backgroundDiseases_to_instructionsFragment)
            }
        }
    }

    private fun getBackgroundDiseases(): ArrayList<String> {
        val diseases = ArrayList<String>()
        if (diabetes.isChecked)
            diseases.add("diabetes")
        if (autoimmune_disease.isChecked)
            diseases.add("autoimmune disease")
        if (asthma.isChecked)
            diseases.add("asthma")
        if (pulmonary_disease.isChecked)
            diseases.add("pulmonary disease")
        if (chronic_kidney_disease.isChecked) {
            if(yes_radio_btn.isChecked)
                diseases.add("CKD receives dialysis treatment")
            else
                diseases.add("CKD doesn't receives dialysis treatment")
        }
        if (hypertension.isChecked)
            diseases.add("hypertension")
        if (cancer.isChecked)
            diseases.add("cancer")
        return diseases

    }


    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_background_diseases_Fragment.visibility = View.VISIBLE
            false -> progressBar_background_diseases_Fragment.visibility = View.GONE
        }
    }

}
