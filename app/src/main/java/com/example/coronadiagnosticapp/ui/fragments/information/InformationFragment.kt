package com.example.coronadiagnosticapp.ui.fragments.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.information_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InformationFragment : ScopedFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
    }

    @Inject
    lateinit var viewModel: InformationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.information_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        button_informationNext.setOnClickListener {
            submit()
        }

        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        })
    }

    private fun initForm() {

        /*form {
            //diabetes.setOnClickListener {

            submitWith(button_informationNext) { res ->
                /*submitPersonalInfoForm(
                    res.get("activity_personal_inp_first_name")?.value.toString(),
                    res.get("activity_personal_inp_last_name")?.value.toString(),
                    res.get("activity_personal_inp_age")?.value.toString().toInt()
                )*/
                showLoading(show = true)
                launch(Dispatchers.IO) {
                    viewModel.updateBackgroundDiseases(getBackgroundDiseases(res))
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        findNavController().navigate(R.id.action_informationFragment_to_instructionsFragment)
                    }
                }

            }
        }*/
    }

    private fun submit(){
        showLoading(show = true)
        launch(Dispatchers.IO) {
            viewModel.updateBackgroundDiseases(getBackgroundDiseases())
            withContext(Dispatchers.Main) {
                showLoading(false)
                findNavController().navigate(R.id.action_informationFragment_to_instructionsFragment)
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
        if (chronic_kidney_disease.isChecked)
            diseases.add("chronic kidney disease")
        if (hypertension.isChecked)
            diseases.add("hypertension")
        if (cancer.isChecked)
            diseases.add("cancer")
        return diseases

    }


    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_informationFragment.visibility = View.VISIBLE
            false -> progressBar_informationFragment.visibility = View.GONE
        }
    }

}
