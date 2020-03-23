package com.example.coronadiagnosticapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.afollestad.vvalidator.form
import com.example.coronadiagnosticapp.data.model.User
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.jackson.responseObject
import com.github.kittinunf.fuel.json.jsonDeserializer
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*
import java.util.logging.Level.parse


import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import android.graphics.Matrix
import android.view.TextureView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

const val SIGNUP_URL =
    "/api/me/sign-up/"

// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 215

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.INTERNET,
    Manifest.permission.RECORD_AUDIO
)

class SignUpActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request camera permissions
        if (allPermissionsGranted()) {

            FuelManager.instance.basePath =
                "https://tnj0200iy8.execute-api.eu-west-1.amazonaws.com/staging/"
            val preferencesHelper = SharedPreferencesHelper(this)

            if (preferencesHelper.getIsLoggedIn()) {

                val token = preferencesHelper.getToken()
                if (token != "") {
                    FuelManager.instance.baseHeaders = mapOf("Authorization" to "JWT $token")

                    Fuel.get("/api/me/").responseJson() { _, _, result ->
                        when (result) {
                            is Result.Failure -> {
                                preferencesHelper.putIsLoggedIn(false)
                                preferencesHelper.putToken("")
                                FuelManager.instance.baseHeaders = mapOf()

                            }
                            is Result.Success -> {
                                if (preferencesHelper.getFirstName() != "") {
                                    toDailyCollectionScreen()
                                }
                                toPersonalInformationScreen()
                            }
                        }
                    }

                }
            }

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
                    submitSignupForm(
                        res.get("activity_signup_inp_email")?.value.toString(),
                        res.get("activity_signup_inp_password")?.value.toString()
                    )

                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }


    /**
     * Process result from permission request dialog box, has the request
     * been granted? If yes, start Camera. Otherwise display a toast
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Check if all permission specified in the manifest have been granted
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun toPersonalInformationScreen() {
        val intent = Intent(this, PersonalInfoCollection::class.java)
        startActivity((intent))
    }

    fun toDailyCollectionScreen() {
        val intent = Intent(this, DailyMetricCollection::class.java)
        startActivity((intent))
    }

    fun submitSignupForm(email: String, password: String): String {
        val uniqueID = UUID.randomUUID().toString()
        val requestBody = JSONObject()
        requestBody.put("email", email)
        requestBody.put("password", password)
        requestBody.put("deviceId", uniqueID)

        Fuel.post(
            SIGNUP_URL
        ).jsonBody(requestBody.toString()).responseJson() { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    val errData = jsonDeserializer().deserialize(response).obj()
                    println(errData)

                }
                is Result.Success -> {
                    val data = result.get()
                    val preferencesHelper = SharedPreferencesHelper(this)
                    val token = data.obj().getJSONObject("token")["access"].toString()
                    preferencesHelper.putIsLoggedIn(true)
                    preferencesHelper.putToken(token)

                    FuelManager.instance.baseHeaders = mapOf("Authorization" to "JWT $token")

                    toPersonalInformationScreen()
                }

            }


        }



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
