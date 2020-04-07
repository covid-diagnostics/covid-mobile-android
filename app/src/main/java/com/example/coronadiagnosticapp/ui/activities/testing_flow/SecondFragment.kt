package com.example.coronadiagnosticapp.ui.activities.testing_flow

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.activities.OxymeterActivity
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.ui.fragments.camera.CameraFragment
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.android.synthetic.main.fragment_welcome.*
import kotlinx.android.synthetic.main.result_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
private const val REQUEST_CODE_PERMISSIONS = 215

class SecondFragment : ScopedFragment() {

    val REQUEST_CODE_VIDEO = 315

    @Inject
    lateinit var viewModel: TestingViewModel

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        textView_test_header.text = "Please click the button START in order to start the test"
        button_test_start.visibility = View.VISIBLE
        textView_test_results.visibility = View.GONE
        group_submit.visibility = View.GONE

        button_test_start.setOnClickListener {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    // get data from OxymeterActivity
                    val beatsPerMinute = data.getStringExtra(CameraFragment.beatsPerMinuteKey())?.toInt()
                    val breathsPerMinute = data.getStringExtra(CameraFragment.breathsPerMinute())?.toInt()
                    val oxygenSaturation = data.getStringExtra(CameraFragment.oxygenSaturation())?.toInt()
                    if (beatsPerMinute != null && breathsPerMinute != null && oxygenSaturation != null) {
                        updateUiAfterTest(beatsPerMinute, oxygenSaturation)
                        launch(Dispatchers.IO) {
                            withContext(Dispatchers.Main) {
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(context, "please try again", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateUiAfterTest(beatsPerMinute: Int, oxygenSaturation: Int) {
        textView_test_header.text = "Thanks for taking the test!\n The results are:"
        button_test_start.visibility = View.GONE
        textView_test_results.text = "Saturation: $oxygenSaturation, Heartbeats: $beatsPerMinute"
        textView_test_results.visibility = View.VISIBLE
        group_submit.visibility = View.VISIBLE

        button_test_finish.setOnClickListener {
            // send to db
            launch(Dispatchers.IO) {
                delay(3000)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Thanks!", Toast.LENGTH_SHORT).show()
                }
            }

        }


    }
}
