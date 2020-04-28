package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.Question
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.CHECKBOX
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.TEXT
import com.example.coronadiagnosticapp.ui.views.QuestionCheckBox
import com.example.coronadiagnosticapp.ui.views.QuestionPresenter
import com.example.coronadiagnosticapp.ui.views.QuestionView
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import kotlinx.android.synthetic.main.fragment_questionnaire.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class QuestionnaireFragment : Fragment() {

    @Inject
    lateinit var viewModel: QuestionnaireViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_questionnaire, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.getAppComponent()?.inject(this)

//        TODO only enable next when answered

        showLoading(progressBar, true)
        GlobalScope.launch(IO) {

            val questions = viewModel.getQuestions()

            withContext(Main) {
                fill(questions)
                next_btn.isEnabled = true
                showLoading(progressBar, false)
            }
        }

        next_btn.setOnClickListener {
            findNavController()
                .navigate(R.id.action_questioneerFragment_to_questionFragment)
        }
    }

    private fun fill(questions: List<Question>) = questions.forEach {
        when (it.type) {
            CHECKBOX -> QuestionCheckBox(context)
            TEXT -> QuestionView(context)
            else -> null
        }?.let { view ->
            questions_group.addView(view as View)
            (view as QuestionPresenter).question = it
        }

    }

}
