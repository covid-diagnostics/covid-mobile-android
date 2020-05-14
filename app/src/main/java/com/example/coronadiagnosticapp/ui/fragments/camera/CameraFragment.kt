package com.example.coronadiagnosticapp.ui.fragments.camera

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.coronadiagnosticapp.ui.activities.MainActivity
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.ui.fragments.oxymeter.OxymeterData
import com.example.coronadiagnosticapp.ui.fragments.oxymeter.OxymeterFragment
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CameraFragment : ScopedFragment() {
    companion object {
        const val TAG = "CameraFragment"
    }

    @Inject
    lateinit var viewModel: CameraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context!!.getAppComponent().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.camera_fragment, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.setStepperCount(1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onOpenedFromOxymeterFragment()

        button_startCamera.setOnClickListener {

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
                startOxymeterFragment()
            }
        }
    }

    private fun startOxymeterFragment() {
        findNavController()
            .navigate(R.id.action_cameraFragment_to_oxymeterFragment)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startOxymeterFragment()
            } else {
                toast("cannot continue without permissions", Toast.LENGTH_LONG)
                findNavController().popBackStack()
            }
        }
    }

    private fun onOpenedFromOxymeterFragment(){
        // get data from OxymeterActivity
        val oxymeterData:OxymeterData = arguments
            ?.getParcelable(OxymeterFragment.EXTRA_OXY_DATA)
            ?: return

        showLoading(progressBar_cameraFragment,true)

        launch(Dispatchers.IO) {
            viewModel.saveResult(HealthResult(oxymeterData))
            withContext(Dispatchers.Main) {
                showLoading(progressBar_cameraFragment,false)
                val id = if (viewModel.isFirstTime) {
                    R.id.action_cameraFragment_to_recorderExplanation
                }else{
                    R.id.action_cameraFragment_to_recorderFragment
                }
                findNavController().navigate(id)
            }
        }
    }

}
