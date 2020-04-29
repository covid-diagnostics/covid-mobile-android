package com.example.coronadiagnosticapp.ui.fragments.information

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.information_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InformationFragment : ScopedFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    @Inject
    lateinit var viewModel: InformationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.information_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initForm()

        viewModel.error.observe(viewLifecycleOwner, Observer {
            toast(it)
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
                val firstName = res["activity_personal_inp_first_name"]?.value
                    ?.toString()
                    ?: return@submitWith

                val lastName = res["activity_personal_inp_last_name"]?.value
                    ?.toString()
                    ?: return@submitWith

                val age = res["activity_personal_inp_age"]?.value
                    ?.toString()?.toIntOrNull()
                    ?: return@submitWith

                submitPersonalInfoForm(firstName, lastName, age)
            }
        }
    }

    private fun submitPersonalInfoForm(firstName: String, lastName: String, age: Int) {
        val progress = progressBar_informationFragment
        showLoading(progress, true)
        launch(Dispatchers.IO) {
            viewModel.updateUserPersonalInformation(firstName, lastName, age)
            withContext(Dispatchers.Main) {
                showLoading(progress, false)
                findNavController().navigate(R.id.action_informationFragment_to_instructionsFragment)
            }
        }

    }

}
