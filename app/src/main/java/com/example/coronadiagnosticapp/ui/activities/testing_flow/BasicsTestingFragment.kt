package com.example.coronadiagnosticapp.ui.activities.testing_flow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.coronadiagnosticapp.MyApplication
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import kotlinx.android.synthetic.main.fragment_basics.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class BasicsFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: TestingViewModel

    private lateinit var keysLighting: Array<String>
    private lateinit var localLighting: Array<String>
    private lateinit var keysPosition: Array<String>
    private lateinit var localPosition: Array<String>
    private lateinit var localMeasurement: Array<String>
    private lateinit var keysMeasurement: Array<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.applicationContext.let { ctx ->
            (ctx as MyApplication).getAppComponent().inject(this)

            localLighting =
                resources.getStringArray(R.array.string_array_testing_lighting) as Array<String>
            keysLighting =
                resources.getStringArray(R.array.string_array_testing_lightingKeys) as Array<String>

            localPosition =
                resources.getStringArray(R.array.string_array_testing_position) as Array<String>
            keysPosition =
                resources.getStringArray(R.array.string_array_testing_positionKeys) as Array<String>

            localMeasurement =
                resources.getStringArray(R.array.string_array_testing_measurement_method) as Array<String>
            keysMeasurement =
                resources.getStringArray(R.array.string_array_testing_measurement_methodKeys) as Array<String>

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_basics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init spinners
        spinner_basics_lighting.setItems(
            resources.getStringArray(R.array.string_array_testing_lighting).toMutableList()
        )
        spinner_basics_position.setItems(
            resources.getStringArray(R.array.string_array_testing_position).toMutableList()
        )
        spinner_basics_measurementMethod.setItems(
            resources.getStringArray(R.array.string_array_testing_measurement_method)
                .toMutableList()
        )

        button_basics_next.setOnClickListener {
            progressBar_basicsFragment.visibility = View.VISIBLE
            launch(Dispatchers.IO) {
                val selectedPosition = keysPosition[spinner_basics_position.selectedIndex]
                val selectedMeasurement =
                    keysMeasurement[spinner_basics_measurementMethod.selectedIndex]
                val selectedLighting = keysLighting[spinner_basics_lighting.selectedIndex]
                viewModel.insertBasicsInformation(
                    BasicsInformation(
                        input_basics_medical.editText?.text.toString(),
                        input_basics_age.editText?.text.toString().toInt(),
                        selectedLighting,
                        selectedPosition,
                        selectedMeasurement
                    )
                )
                withContext(Dispatchers.Main) {
                    progressBar_basicsFragment.visibility = View.GONE
                    findNavController().navigate(R.id.action_basicsTestingFragment_to_resultTestingFragment)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getBasicsInformation().observe(viewLifecycleOwner, Observer { basicsInformation ->
            if (basicsInformation != null) {
                input_basics_age.editText?.setText(basicsInformation.age.toString())
                input_basics_medical.editText?.setText(basicsInformation.medicalHistory)
                spinner_basics_position.selectedIndex =
                    keysPosition.indexOf(basicsInformation.position)
                spinner_basics_lighting.selectedIndex =
                    keysLighting.indexOf(basicsInformation.lighting)
                spinner_basics_measurementMethod.selectedIndex =
                    keysMeasurement.indexOf(basicsInformation.measurement)
            }
        })
    }
}

@Entity(tableName = "basics_information")
data class BasicsInformation(
    @PrimaryKey val id: Int,
    val medicalHistory: String,
    val age: Int,
    val lighting: String,
    val position: String,
    val measurement: String
) {
    constructor(
        medicalHistory: String,
        age: Int,
        lighting: String,
        position: String,
        measurement: String
    ) : this(
        0,
        medicalHistory,
        age,
        lighting,
        position,
        measurement
    )
}