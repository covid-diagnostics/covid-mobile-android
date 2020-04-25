package com.example.coronadiagnosticapp.ui.fragments.register

import android.os.Bundle
import android.util.Log
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
import kotlinx.android.synthetic.main.register_fragment.*
import kotlinx.coroutines.Dispatchers
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        initErrors()
    }

    private fun initForm() = form {
        inputLayout(textInputLayout_email) {
            isNotEmpty().description(getString(R.string.required))
            isEmail().description(getString(R.string.must_valid_email))
        }
        inputLayout(textInputLayout_password) {
            isNotEmpty()
        }
        inputLayout(TextInputLayout_passwordRepeat) {
            isNotEmpty()
            assert(getString(R.string.passwords_match)) { view ->
                val repeatPass = view.editText?.text.toString()
                val password = textInputLayout_password.editText?.text.toString()
                password == repeatPass
            }
        }
        submitWith(button_register) { res ->
            val progress = progressBar_register
            showLoading(progress, true)
            launch(Dispatchers.IO) {
                viewModel.registerUser(
                    res["textInputLayout_email"]?.value.toString(),
                    res["textInputLayout_password"]?.value.toString()
                )
                withContext(Dispatchers.Main) {
                    showLoading(progress, false)
                    if (viewModel.isLoggedIn()) {
                        findNavController().navigate(R.id.action_registerFragment_to_informationFragment)
                    } else {
                        toast("Please try again")
                    }
                }

            }
        }
    }

    private fun initErrors() = viewModel.error.observe(
        viewLifecycleOwner,
        Observer { msg ->
            toast(msg)
            Log.e("RegisterFragment", msg)
        })

}
