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
import com.example.coronadiagnosticapp.data.db.entity.BackDiseases
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
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
        showLoading(progressBar_background_diseases_Fragment, true)
        launch(Dispatchers.IO) {
            viewModel.updateBackgroundDiseases(getBackgroundDiseases())
            withContext(Dispatchers.Main) {
                showLoading(progressBar_background_diseases_Fragment,false)
                findNavController().navigate(R.id.action_backgroundDiseases_to_instructionsFragment)
            }
        }
    }

    private fun getBackgroundDiseases(): List<BackDiseases> {
        val diseases = mutableListOf<BackDiseases>()
        if (diabetes.isChecked)
            diseases.add(BackDiseases.DIABETES)
        if (autoimmune_disease.isChecked)
            diseases.add(BackDiseases.AUTOIMMUNE)
        if (asthma.isChecked)
            diseases.add(BackDiseases.ASTHMA)
        if (pulmonary_disease.isChecked)
            diseases.add(BackDiseases.PULMONARY)
        if (chronic_kidney_disease.isChecked) {
            val disease = if (yes_radio_btn.isChecked)
                BackDiseases.CKD_RECEIVES_DIALYSIS
            else
                BackDiseases.CKD_NOT_RECEIVE_DIALYSIS
            diseases.add(disease)
        }
        if (hypertension.isChecked)
            diseases.add(BackDiseases.HYPERTENSION)
        if (cancer.isChecked)
            diseases.add(BackDiseases.CANCER)
        return diseases

    }
}
