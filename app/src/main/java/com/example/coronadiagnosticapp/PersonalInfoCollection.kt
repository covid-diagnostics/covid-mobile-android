package com.example.coronadiagnosticapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.vvalidator.form
import kotlinx.android.synthetic.main.activity_personal_info_collection.*

class PersonalInfoCollection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info_collection)

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

            submitWith(activity_personal_btn_submit) { res ->
                val success = submitPersonalInfoForm(
                    res.get("activity_personal_inp_first_name")?.value.toString(),
                    res.get("activity_personal_inp_last_name")?.value.toString(),
                    res.get("activity_personal_inp_age")?.value.toString()
                )
                println("Submitted info $success")

                nextScreen()


            }
        }

    }

    fun submitPersonalInfoForm(first_name: String, last_name: String, age: String): Boolean {
        return true
    }

    fun nextScreen(){
        val intent = Intent(this, DailyMetricCollection::class.java)
        startActivity((intent))
    }

}

