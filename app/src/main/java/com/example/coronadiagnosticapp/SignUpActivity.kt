package com.example.coronadiagnosticapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.afollestad.vvalidator.form
import kotlinx.android.synthetic.main.activity_main.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            submitWith(activity_signup_btn_submit) { res ->
                val token = submitSignupForm(
                    res.get("activity_signup_inp_email")?.value.toString(),
                    res.get("activity_signup_inp_password")?.value.toString()
                )
                println("Token $token")

                nextScreen()


            }
        }
    }
    fun nextScreen(){
        val intent = Intent(this, PersonalInfoCollection::class.java)
        startActivity((intent))
    }

    fun submitSignupForm(email: String, password: String): String {
        println("Email: $email, Pass: $password")
        return "TOKENSTRING"
    }


    /*fun onSignUp(view: View) {
        val email = activity_signup_inp_email.editText?.text.toString()
        val password = activity_signup_inp_password.editText?.text.toString()
        val repeat_password = activity_signup_inp_password_repeat.editText?.text.toString()

        if (email == "") {
            email.error = getString(R.string.required)
        }

    }*/
}
