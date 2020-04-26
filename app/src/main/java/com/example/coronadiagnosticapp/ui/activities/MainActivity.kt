package com.example.coronadiagnosticapp.ui.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.camera.CameraFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stepperLayout.visibility = View.INVISIBLE
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(CameraFragment.TAG, "LELZ!")
        if (requestCode == 200) {
            Log.i(CameraFragment.TAG, "got permissions!")
            for (i in permissions.indices) {
                // If we got permissions to use the camera, send camera info to the DB.
                if (permissions[i] == Manifest.permission.CAMERA && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(CameraFragment.TAG, "COOOOOOOOOOOOOLLLLLLLLLL!")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        GlobalScope.launch {
                            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                            val cameraCharacteristics : CameraCharacteristics = cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[0])
                            repository.updateUserCameraCharacteristics(cameraCharacteristics)
                        }
                    } else {
                        Log.w(CameraFragment.TAG, "Android API doesn't support camera2, skipping sending camera characteristics.")
                    }
                }
            }
        }
    }
}
