package com.example.coronadiagnosticapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.afollestad.vvalidator.form
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_daily_metric_collection.*
import org.json.JSONObject

const val DAILY_METRICS_URL = "/api/me/fill-daily-metrics/"

class DailyMetricCollection : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesHelper = SharedPreferencesHelper(this)
        if (preferencesHelper.getSubmissionId() != 0){
            toCameraScreen()
        }
        setContentView(R.layout.activity_daily_metric_collection)

        form {
            inputLayout(activity_metrics_inp_temp) {
                isNotEmpty().description(getString(R.string.required))
            }
            inputLayout(activity_metrics_inp_cough_strength) {
                isNotEmpty()


            }
            checkable(activity_metrics_chk_cough_wet) {
            }
            submitWith(activity_metrics_btn_submit) { res ->
                submitDailyMetrics(
                    res.get("activity_metrics_inp_temp")?.value.toString(),
                    res.get("activity_metrics_inp_cough_strength")?.value.toString(),
                    res.get("activity_metrics_chk_cough_wet")?.value as Boolean
                )

            }
        }

    }

    fun submitDailyMetrics(temp: String, cough_strength: String, is_wet: Boolean) {
        println("$temp $cough_strength $is_wet")
        val requestBody = JSONObject()
        requestBody.put("temperature", temp)
        requestBody.put("coughStrength", cough_strength)
        requestBody.put("isCoughDry", !is_wet)

        Fuel.post(
            DAILY_METRICS_URL
        ).jsonBody(requestBody.toString()).responseJson() { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val errData = jsonDeserializer().deserialize(response).obj()
                    println(errData)

                }
                is Result.Success -> {
                    val data = result.get()
                    val preferencesHelper = SharedPreferencesHelper(this)

                    preferencesHelper.putSubmissionId(data.obj()["id"] as Int)

                    toCameraScreen()
                }

            }


        }
    }

    fun toCameraScreen() {
        val intent = Intent(this, StartRecorder::class.java)
        startActivity(intent)
    }
}
