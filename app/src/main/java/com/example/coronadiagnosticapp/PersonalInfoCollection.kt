package com.example.coronadiagnosticapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.vvalidator.form
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_personal_info_collection.*
import org.json.JSONObject

const val FILL_DETAILS_URL = "/api/me/fill-personal-info/"

class PersonalInfoCollection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesHelper = SharedPreferencesHelper(this)

        if (preferencesHelper.getFirstName() != "") {

            Fuel.get("/api/me/").responseJson() { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        preferencesHelper.putIsLoggedIn(false)
                        preferencesHelper.putToken("")
                        FuelManager.instance.baseHeaders = mapOf()
                        toSignupScreen()

                    }
                    is Result.Success -> {
                        toDailyMetricsScreen()
                    }
                }
            }


        }
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
                submitPersonalInfoForm(
                    res.get("activity_personal_inp_first_name")?.value.toString(),
                    res.get("activity_personal_inp_last_name")?.value.toString(),
                    res.get("activity_personal_inp_age")?.value.toString()
                )

            }
        }

    }

    fun submitPersonalInfoForm(first_name: String, last_name: String, age: String) {
        val requestBody = JSONObject()
        requestBody.put("firstName", first_name)
        requestBody.put("lastName", last_name)
        requestBody.put("age", age)

        Fuel.put(
            FILL_DETAILS_URL
        ).jsonBody(requestBody.toString()).responseJson() { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val errData = jsonDeserializer().deserialize(response).obj()
                    println(errData)

                }
                is Result.Success -> {
                    val data = result.get()
                    val preferencesHelper = SharedPreferencesHelper(this)

                    preferencesHelper.putFirstName(data.obj()["firstName"].toString())

                    toDailyMetricsScreen()
                }

            }


        }
    }

    fun toSignupScreen() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity((intent))
    }

    fun toDailyMetricsScreen() {
        val intent = Intent(this, DailyMetricCollection::class.java)
        startActivity((intent))
    }

}

