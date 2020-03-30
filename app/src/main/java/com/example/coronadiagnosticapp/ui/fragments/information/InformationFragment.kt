package com.example.coronadiagnosticapp.ui.fragments.information

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.MyApplication

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
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

        initForm()

        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

        })
    }

    private fun initForm() {

        form {
            inputLayout(activity_personal_inp_first_name) {
                isNotEmpty().description(getString(R.string.required))
            }
            inputLayout(activity_personal_inp_last_name) {
                isNotEmpty().description(getString(R.string.required))


            }
            inputLayout(activity_personal_inp_age) {
                isNotEmpty().description(getString(R.string.required))
            }

            submitWith(button_informationNext) { res ->
                submitPersonalInfoForm(
                    res.get("activity_personal_inp_first_name")?.value.toString(),
                    res.get("activity_personal_inp_last_name")?.value.toString(),
                    res.get("activity_personal_inp_age")?.value.toString().toInt()
                )

            }
        }
    }

    private fun submitPersonalInfoForm(firstName: String, lastName: String, age: Int) {
        showLoading(show = true)
        launch(Dispatchers.IO) {
            viewModel.updateUserPersonalInformation(
                firstName, lastName, age
            )
            withContext(Dispatchers.Main) {
                showLoading(false)
                findNavController().navigate(R.id.action_informationFragment_to_dailyMetricFragment)
            }
        }

    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_informationFragment.visibility = View.VISIBLE
            false -> progressBar_informationFragment.visibility = View.GONE
        }
    }

}
