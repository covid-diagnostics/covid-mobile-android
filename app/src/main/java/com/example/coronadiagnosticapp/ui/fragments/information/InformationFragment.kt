package com.example.coronadiagnosticapp.ui.fragments.information

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.afollestad.vvalidator.form
import com.afollestad.vvalidator.form.FormResult
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.Sex
import com.example.coronadiagnosticapp.ui.fragments.ScopedFragment
import com.example.coronadiagnosticapp.ui.views.YesNoQuestionView.YesNo
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.information_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InformationFragment : ScopedFragment() {

    @Inject
    lateinit var viewModel: InformationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.information_fragment, container, false)

    override fun onAttach(context: Context) {
        context.getAppComponent().inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initForm()

        viewModel.error.observe(viewLifecycleOwner, Observer { msg ->
            toast(msg)
        })
    }

    private fun initForm() {

        form {
            input(et_age,"age") {
//                isNotEmpty()
                isNumber()
                isNumber().greaterThan(0).description("must be a positive number")
            }
            input(et_height,"height") {
//                isNotEmpty()
                isNumber()
                isNumber().greaterThan(0).description("must be a positive number")
            }
            input(et_weight,"weight") {
//                isNotEmpty()
                isNumber()
                isNumber().greaterThan(0).description("must be a positive number")
            }
//            (sex_question)
            submitWith(info_continue_btn, this@InformationFragment::tryToSubmit)
        }
    }

    private fun tryToSubmit(res: FormResult) {

        if (res.hasErrors()) {
            toast("Make sure you filled the right details")
            return
        }

        val sexSelection = sex_question.currentSelection
            ?: run {
                toast("please select sex")
                return
            }
        val sex =  Sex.values()[sexSelection.ordinal]

        val age = res["age"]!!.asInt()!!
        val height = res["height"]!!.asInt()!!
        val weight = res["weight"]!!.asInt()!!
        submitPersonalInfoForm(sex, age, height, weight)
    }

    private fun submitPersonalInfoForm(sex: Sex, age: Int, height: Int, weight: Int) {

        showLoading(progressBar_informationFragment, true)

        launch(IO) {
            viewModel.updateUserPersonalInformation(sex, age, height, weight)

            withContext(Main) {
                showLoading(progressBar_informationFragment, false)

                findNavController()
                    .navigate(R.id.action_informationFragment_to_smokingFragment)
            }
        }

    }

}
