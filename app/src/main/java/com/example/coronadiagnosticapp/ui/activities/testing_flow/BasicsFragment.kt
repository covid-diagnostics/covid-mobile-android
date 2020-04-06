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
        return inflater.inflate(R.layout.fragment_basics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner_basics_lighting.setItems("Dark room", "Sunlight", "Yellow lamp", "White lamp")
        spinner_basics_position.setItems("Standing", "Laying down", "Sitting")
        button_basics_next.setOnClickListener {
            progressBar_basicsFragment.visibility = View.VISIBLE
            launch(Dispatchers.IO) {
                viewModel.insertBasicsInformation(
                        BasicsInformation(
                                input_basics_medical.editText?.text.toString(),
                                input_basics_age.editText?.text.toString().toInt(),
                                spinner_basics_lighting.getItems<String>()[spinner_basics_lighting.selectedIndex],
                                spinner_basics_position.getItems<String>()[spinner_basics_position.selectedIndex]
                        ))
                withContext(Dispatchers.Main) {
                    progressBar_basicsFragment.visibility = View.GONE
                    findNavController().navigate(R.id.action_basicsFragment_to_secondFragment)
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

                val lightingItems = spinner_basics_lighting.getItems<String>()
                var index = lightingItems.indexOf(basicsInformation.lighting)
                var temp = lightingItems.first()
                lightingItems[index] = temp
                lightingItems[0] = basicsInformation.lighting
                spinner_basics_lighting.setItems(lightingItems)

                val positionItems = spinner_basics_position.getItems<String>()
                index = positionItems.indexOf(basicsInformation.position)
                temp = positionItems.first()
                positionItems[index] = temp
                positionItems[0] = basicsInformation.position
                spinner_basics_position.setItems(positionItems)
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
        val position: String
) {
    constructor(medicalHistory: String, age: Int, lighting: String, position: String) : this(0, medicalHistory, age, lighting, position)
}