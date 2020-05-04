package com.example.coronadiagnosticapp.ui.fragments.register

import android.R.layout.simple_spinner_dropdown_item
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.FormResult
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.*
import kotlinx.android.synthetic.main.register_fragment.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegisterFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.register_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCountriesOptions()

        initForm()
        initErrors()
    }

    private fun setUpCountriesOptions() {
        spinner_country.adapter = ArrayAdapter(
            context!!,
            simple_spinner_dropdown_item,
            CountryHelper.getCountriesList()
        )

        spinner_country.setSelection(0)
    }

    private fun initForm() = form {
        input(et_phone, "phone") {
            isNotEmpty().description(getString(R.string.required))
        }
        spinner(spinner_country, "country") {
        }
        submitWith(register_continue_btn, this@RegisterFragment::tryToSubmit)
    }


    private fun tryToSubmit(res: FormResult) {
        showLoading(progressBar_register, true)
        launch(IO) {
            val phone = res["phone"]!!.asString()
            val countrySelectIndex = res["country"]!!.asInt()!!
            val country = spinner_country.adapter.getItem(countrySelectIndex) as Country

            viewModel.registerUser(phone, country.iso)

            withContext(Main) {
                showLoading(progressBar_register, false)
                if (viewModel.isLoggedIn()) {
                    findNavController()
                        .navigate(R.id.action_registerFragment_to_informationFragment)
                } else {
                    toast("Please try again")
                }
            }

        }
    }

    private fun initErrors() {
        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            toast(msg)
            Log.e("RegisterFragment", msg)
        })
    }

}
