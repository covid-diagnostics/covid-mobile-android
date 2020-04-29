package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.Question
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.CHECKBOX
import com.example.coronadiagnosticapp.data.db.entity.QuestionType.TEXT
import com.example.coronadiagnosticapp.ui.views.QuestionCheckBox
import com.example.coronadiagnosticapp.ui.views.QuestionPresenter
import com.example.coronadiagnosticapp.ui.views.QuestionView
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.example.coronadiagnosticapp.utils.toast
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
            saveAnswers()
            findNavController()
                .navigate(R.id.action_questioneerFragment_to_questionFragment)
        }
    }

    private fun saveAnswers() {
        val answers = mutableListOf<AnswersResponse>()
        for (view in questions_group.children) {
            val question = (view as QuestionPresenter).question!!.id

            val ans = when (view) {
                is QuestionCheckBox -> view.isChecked.toString()
                is QuestionView -> view.getAnswer()
                else -> ""
            }

            val answer = AnswersResponse(question, ans, question)
            answers.add(answer)
        }
        GlobalScope.launch(IO) {
            viewModel.addAnswers(answers)

            withContext(Main) {
                toast("saved")
            }
        }
    }

    private fun fill(questions: List<Question>) {
        val context = context ?: return

        questions.forEach {
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

}
