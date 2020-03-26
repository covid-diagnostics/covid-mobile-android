package com.example.coronadiagnosticapp.ui.fragments.camera

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.coronadiagnosticapp.data.di.DaggerAppComponent
import com.example.coronadiagnosticapp.ui.activities.OxymeterActivity
import com.example.coronadiagnosticapp.ui.activities.VideoRecordActivity
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.camera_fragment.*
import kotlinx.android.synthetic.main.recorder_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import java.io.File
import javax.inject.Inject


// This is an arbitrary number we are using to keep track of the permission
// request. Where an app has multiple context for requesting permission,
// this can help differentiate the different contexts.
private const val REQUEST_CODE_PERMISSIONS = 215

private const val REQUEST_CODE_VIDEO = 315


// This is an array of all the permission specified in the manifest.
private val REQUIRED_PERMISSIONS = arrayOf(
    Manifest.permission.CAMERA,
    Manifest.permission.INTERNET,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.WRITE_EXTERNAL_STORAGE
)

class CameraFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: CameraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.camera_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        startCamera.setOnClickListener {
            // TODO: Ask permission to use the camera
            if (context?.let { it1 ->
                    ActivityCompat.checkSelfPermission(
                        it1,
                        Manifest.permission.CAMERA
                    )
                } != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                activity?.let { it1 ->
                    ActivityCompat.requestPermissions(
                        it1, arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), 200
                    )
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
        if (requestCode == REQUEST_CODE_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
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
                    Toast.makeText(context, "Result: "+ data.getStringExtra("result"), Toast.LENGTH_SHORT).show()

                }
            } else {
                Toast.makeText(context, "please try again", Toast.LENGTH_SHORT).show()
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
