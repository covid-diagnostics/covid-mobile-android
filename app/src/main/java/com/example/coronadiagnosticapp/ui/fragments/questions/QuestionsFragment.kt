package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.Question
import com.example.coronadiagnosticapp.data.db.QuestionType
import com.example.coronadiagnosticapp.ui.views.QuestionCheckBox
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.fragment_questions.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class QuestionsFragment : Fragment() {

    @Inject
    lateinit var viewModel: QuestionsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_questions, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        TODO add progress
//        showLoading(,true)
        GlobalScope.launch(IO) {
            val questions = viewModel.getQuestions()
            withContext(Main) {
//            TODO("Init data with questions")
                initQuestions(questions)
                //        TODO add progress
                //            showLoading(,false)

            }
        }
    }

    private fun initQuestions(questions: List<Question>) = questions.forEach {
        when (it.qtype) {
            QuestionType.CHECKBOX -> addCheckBox(it)
            QuestionType.TEXT -> addEditText(it)
            QuestionType.MULTI_SELECT -> addMultiSelect(it)
            QuestionType.SELECT -> addSelect(it)
        }
    }

    private fun addSelect(question: Question) {
        toast(question.name)
//        TODO add one option select checkboxes
    }

    private fun addMultiSelect(question: Question) {
        toast(question.name)
//        TODO add mutliSelect checkboxes
    }

    private fun addEditText(question: Question) {
        toast(question.displayName)
//        TODO add edit texts
    }

    private fun addCheckBox(question: Question) {

        val view: QuestionCheckBox = layoutInflater.inflate(
            R.layout.question_checkbox,
            questions_form,
            false
        ) as QuestionCheckBox

        view.setQuestion(question)

        questions_form.addView(view)
    }

}
