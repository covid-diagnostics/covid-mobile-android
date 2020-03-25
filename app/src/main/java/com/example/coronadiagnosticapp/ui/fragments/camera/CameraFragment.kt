package com.example.coronadiagnosticapp.ui.fragments.camera

import android.Manifest
import android.content.pm.PackageManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.camera_fragment.*
import javax.inject.Inject


private const val REQUEST_CODE_PERMISSIONS = 215

// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class CameraFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: CameraViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startCamera.setOnClickListener {

            val myContext = context
            if (myContext != null) {
                val cameraPermission =
                    ContextCompat.checkSelfPermission(myContext, Manifest.permission.CAMERA)
                val audioPermission =
                    ContextCompat.checkSelfPermission(myContext, Manifest.permission.RECORD_AUDIO)
                val storagePermission = ContextCompat.checkSelfPermission(
                    myContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )

                if (audioPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
                    activity!!.finish()
                }
                // start camera do stuff



            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }


}
