package com.example.coronadiagnosticapp.ui.fragments.register

import android.opengl.Visibility
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.register_fragment.*
import kotlinx.android.synthetic.main.register_fragment.activity_signup_inp_email
import kotlinx.android.synthetic.main.register_fragment.activity_signup_inp_password
import kotlinx.android.synthetic.main.register_fragment.activity_signup_inp_password_repeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RegisterFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
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
            inputLayout(activity_signup_inp_email) {
                isNotEmpty().description(getString(R.string.required))
                isEmail().description(getString(R.string.must_valid_email))
            }
            inputLayout(activity_signup_inp_password) {
                isNotEmpty()
            }
            inputLayout(activity_signup_inp_password_repeat) {
                isNotEmpty()
                assert(getString(R.string.passwords_match)) { view ->
                    val repeatPass = view.editText?.text.toString()
                    val password = activity_signup_inp_password.editText?.text.toString()
                    password == repeatPass
                }
            }
            submitWith(button_register) { res ->
                showLoading(show = true)
                launch(Dispatchers.IO) {
                    viewModel.registerUser(
                        res.get("activity_signup_inp_email")?.value.toString(),
                        res.get("activity_signup_inp_password")?.value.toString()
                    )
                    withContext(Dispatchers.Main) {
                        //update views
                        showLoading(false)
                        if (viewModel.isLoggedIn()) {
                            findNavController().navigate(R.id.action_registerFragment_to_informationFragment)
                        } else {
                            Toast.makeText(context, "Too bad :/", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar.visibility = View.VISIBLE
            false -> progressBar.visibility = View.GONE
        }
    }


    private fun initErrors() {
        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            Log.e("RegisterFragment", msg)
        })
    }

}
