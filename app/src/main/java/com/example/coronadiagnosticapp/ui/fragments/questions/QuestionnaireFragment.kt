package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.question.Question
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType.CHECKBOX
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType.TEXT
import com.example.coronadiagnosticapp.ui.fragments.questions.viewmodels.QuestionnaireViewModel
import com.example.coronadiagnosticapp.ui.views.QuestionCheckBoxView
import com.example.coronadiagnosticapp.ui.views.QuestionPresenter
import com.example.coronadiagnosticapp.ui.views.QuestionView
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.showLoading
import com.example.coronadiagnosticapp.utils.toDP
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

        showLoading(progressBar, true)
        GlobalScope.launch(IO) {

            val questions = viewModel.getQuestions()

            withContext(Main) {
                if (questions.isEmpty()) {
                    moveToNextScreen()
                    return@withContext
                }
                fill(questions)
                next_btn.isEnabled = true
                showLoading(progressBar, false)
            }
        }

        next_btn.setOnClickListener {
            it.isEnabled = false
            saveAnswers()
        }
    }

    private fun moveToNextScreen() {
        findNavController()
            .navigate(R.id.action_questioneerFragment_to_questionFragment)
    }

    private fun saveAnswers() {
        showLoading(progressBar, true)
        val answers = mutableListOf<AnswersResponse>()
        for (view in questions_group.children) {
            val question = (view as QuestionPresenter).question!!.id

            val ans = when (view) {
                is QuestionCheckBoxView -> view.isChecked.toString()
                is QuestionView -> view.getAnswer()
                else -> ""
            }

            val answer = AnswersResponse(question, ans, question)
            answers.add(answer)
        }
        GlobalScope.launch(IO) {
            viewModel.addAnswers(answers)

            withContext(Main) {
                showLoading(progressBar, false)
                //toast("saved")
                moveToNextScreen()
            }
        }
    }

    private fun fill(questions: List<Question>) {
        val context = context ?: return

        loop@ for (question in questions) {
            val view = when (question.type) {
                CHECKBOX -> QuestionCheckBoxView(context)
                TEXT -> QuestionView(context)
                else -> continue@loop
            }
            questions_group.addView(view as View)
            (view as QuestionPresenter).question = question

            val pad = 8.toDP(context)
            (view.layoutParams as? LinearLayout.LayoutParams)
                ?.setMargins(0,pad,0, pad)
        }
    }

}
