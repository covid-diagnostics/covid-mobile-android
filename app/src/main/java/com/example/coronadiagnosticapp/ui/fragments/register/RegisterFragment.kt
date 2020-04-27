package com.example.coronadiagnosticapp.ui.fragments.register

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.MyApplication

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.register_fragment.*
import kotlinx.android.synthetic.main.register_fragment.textInputLayout_email
import kotlinx.android.synthetic.main.register_fragment.textInputLayout_password
import kotlinx.android.synthetic.main.register_fragment.TextInputLayout_passwordRepeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegisterFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            it.applicationContext.let { ctx ->
                (ctx as MyApplication).getAppComponent().inject(this)
            }
            it.findViewById<StepperIndicator>(R.id.stepperIndicator)?.apply {
                visibility = View.GONE
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.register_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initForm()
        initErrors()


    }

    private fun initForm() {
        form {
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
                showLoading(show = true)
                launch(Dispatchers.IO) {
                    viewModel.registerUser(
                        res["textInputLayout_email"]?.value.toString(),
                        res["textInputLayout_password"]?.value.toString()
                    )
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        if (viewModel.isLoggedIn()) {
                            findNavController().navigate(R.id.action_registerFragment_to_informationFragment)
                        } else {
                            Toast.makeText(context, "Please try again", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_register.visibility = View.VISIBLE
            false -> progressBar_register.visibility = View.GONE
        }
    }


    private fun initErrors() {
        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            Log.e("RegisterFragment", msg)
        })
    }

}
