package com.example.coronadiagnosticapp.ui.fragments.camera

import android.Manifest.permission.CAMERA
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.ui.activities.OxymeterActivity
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterData
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.example.coronadiagnosticapp.utils.toast
import com.rakshakhegde.stepperindicator.StepperIndicator
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.


class CameraFragment : ScopedFragment() {

    companion object CameraCodes {
        private const val RC_CAM_AND_WRITE = 200
        private const val REQUEST_CODE_PERMISSIONS = 215
        private const val REQUEST_CODE_VIDEO = 315
    }

    @Inject
    lateinit var viewModel: CameraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)

        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.camera_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.findViewById<StepperIndicator>(R.id.stepperIndicator)?.currentStep = 1
        button_startCamera.setOnClickListener {
            val context = context ?: return@setOnClickListener

            val camPermission = ContextCompat.checkSelfPermission(context, CAMERA)
            val writePermission = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)

            if (camPermission != PERMISSION_GRANTED ||
                writePermission != PERMISSION_GRANTED
            ) {

                activity?.let { activity ->
                    val permissions = arrayOf(
                        CAMERA,
                        WRITE_EXTERNAL_STORAGE
                    )
                    ActivityCompat.requestPermissions(activity, permissions, RC_CAM_AND_WRITE)
                }
            } else {
                val intent = Intent(context, OxymeterActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_VIDEO)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_CAM_AND_WRITE) {
            if (grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                context?.let {
                    val intent = Intent(it, OxymeterActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_VIDEO)
                }
            } else {
                toast("cannot continue without permissions", Toast.LENGTH_LONG)
                activity?.finish()//FIXME not sure if correct
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_VIDEO -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.let { checkVideoData(it) }
                } else {
                    toast("please try again")
                }
            }
        }
    }

    private fun checkVideoData(data: Intent) {
        //                    val fileName = data.getStringExtra("result")
        //                    val file = File(fileName)
        //
        //                    Log.d("CameraFragment", file.totalSpace.toString())
        //                    showLoading(true)
        //                    launch(Dispatchers.IO) {
        //                        viewModel.uploadVideo(File("csd"))
        //                        withContext(Dispatchers.Main) {
        //                            showLoading(false)
        //                            findNavController().navigate(R.id.action_cameraFragment_to_recorderFragment)
        //                        }
        //                    }
        val progress = progressBar_cameraFragment
        showLoading(progress, true)
        // get data from OxymeterActivity
        val measures: OxymeterData = data.getParcelableExtra(OxymeterActivity.EXTRA_OXYMETER_DATA)
            ?: return

        launch(Dispatchers.IO) {
            val healthResult = HealthResult(measures)
            viewModel.saveResult(healthResult)

            withContext(Dispatchers.Main) {
                showLoading(progress, false)
                findNavController().navigate(R.id.action_cameraFragment_to_recorderFragment)
            }
        }

    }

}
