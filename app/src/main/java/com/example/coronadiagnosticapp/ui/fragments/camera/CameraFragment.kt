package com.example.coronadiagnosticapp.ui.fragments.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterActivity
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 215

private const val REQUEST_CODE_VIDEO = 315


class CameraFragment : ScopedFragment() {
    companion object {
        const val TAG = "CameraFragment"
        fun beatsPerMinuteKey() = "BEATS_PER_MINUTE"
        fun breathsPerMinute() = "BREATHS_PER_MINUTE"
        fun oxygenSaturation() = "OXYGEN_SATURATION"
    }

    @Inject
    lateinit var viewModel: CameraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 1
        if (!this.viewModel.getIsFirstTime()) {
            openOximeter()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    private fun openOximeter() {
        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
                200)
        } else {
            val intent = Intent(context, OxymeterActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_VIDEO)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 1
        button_startCamera.setOnClickListener { openOximeter() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                activity?.let {
                    val intent = Intent(context, OxymeterActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_VIDEO)
                }
            } else {
                Toast.makeText(context, "cannot continue without permissions", Toast.LENGTH_LONG)
                    .show()
                activity?.finish()//FIXME not sure if correct
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode == REQUEST_CODE_VIDEO) && (resultCode == Activity.RESULT_OK) && (data != null)) {
            showLoading(true)
            // get data from OxymeterActivity
            val beatsPerMinute = data.getStringExtra(beatsPerMinuteKey())?.toInt()
            val breathsPerMinute = data.getStringExtra(breathsPerMinute())?.toInt()
            val oxygenSaturation = data.getStringExtra(oxygenSaturation())?.toInt()
            if (beatsPerMinute != null && breathsPerMinute != null && oxygenSaturation != null) {
                launch(Dispatchers.IO) {
                    viewModel.saveResult(
                        HealthResult(
                            beatsPerMinute,
                            breathsPerMinute,
                            oxygenSaturation
                        )
                    )
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        if (viewModel.getIsFirstTime()) {
                            findNavController().navigate(R.id.action_cameraFragment_to_recorderExplanation)
                        } else {
                            findNavController().navigate(R.id.action_cameraFragment_to_recorderFragment)
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showLoading(show: Boolean) {
        when (show) {
            true -> progressBar_cameraFragment.visibility = View.VISIBLE
            false -> progressBar_cameraFragment.visibility = View.GONE
        }
    }

}
