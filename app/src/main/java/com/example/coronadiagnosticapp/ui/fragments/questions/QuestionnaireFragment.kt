package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.Question
import com.example.coronadiagnosticapp.data.db.QuestionType.CHECKBOX
import com.example.coronadiagnosticapp.data.db.QuestionType.TEXT
import com.example.coronadiagnosticapp.data.db.UserAnswers
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

        next_btn.setOnClickListener {
            showNextQuestionOrContinue()
        }

        showLoading(progressBar, true)
        GlobalScope.launch(IO) {

            val questions = viewModel.getQuestions()

            withContext(Main) {
                fill(questions)
                showLoading(progressBar, false)
            }

        }

    }

    private fun fill(simpleQA: List<Question>) {
        for (question in simpleQA) {
            when (question.type) {
                CHECKBOX -> QuestionCheckBox(context)
                TEXT -> QuestionView(context)
                else -> null
            }?.let {
                (it as QuestionPresenter).question = question
                questions_group.addView(it as View)
            }
        }
    }

    private val navController: NavController
        get() = findNavController()

    private fun showNextQuestionOrContinue() {
        val question = viewModel.nextQuestion
            ?: run {
                sendData()
                return
            }

        val bundle = bundleOf("q" to question)

        navController
            .navigate(R.id.action_questioneerFragment_to_questionFragment, bundle)

    }

    private fun sendData() {

        showLoading(progressBar, true)
        GlobalScope.launch(IO) {
            viewModel.sendData()

            withContext(Main) {
                showLoading(progressBar, false)
                navController
                    .navigate(R.id.action_questioneerFragment_to_cameraFragment)
            }
        }
    }

//    TODO  - on Back here from question fragment
//    get next q from viewModel

    fun onBackFromQuestionFragment(userAnswers: UserAnswers) {
        viewModel.updateData(userAnswers)
        showNextQuestionOrContinue()
    }
}
