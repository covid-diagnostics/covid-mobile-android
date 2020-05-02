package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType.MULTI_SELECT
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType.SELECT
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.ui.fragments.questions.adapters.MultiQuestionAdapter
import com.example.coronadiagnosticapp.ui.fragments.questions.adapters.SelectQuestionAdapter
import com.example.coronadiagnosticapp.ui.fragments.questions.adapters.Selectable
import com.example.coronadiagnosticapp.ui.fragments.questions.viewmodels.QuestionFragmentViewModel
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.toast
import kotlinx.android.synthetic.main.fragment_question.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class QuestionFragment : Fragment() {

    private val TAG = javaClass.name

    @Inject
    lateinit var viewModel: QuestionFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_question, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        options_rv.adapter =
            MultiQuestionAdapter(
                emptyList()
            )
        nextQuestionOrSendData()
    }

    private fun nextQuestionOrSendData() {
        GlobalScope.launch(IO) {
            val nextQuestion = viewModel.getNextQuestion()

            withContext(Main) {
                nextQuestion?.let {
//                    Animate
                    val animation = AnimationUtils
                        .loadAnimation(context, android.R.anim.slide_in_left)
                    view?.startAnimation(animation)
                    initStuff(it)

                } ?: sendData()
            }
        }
    }

    private suspend fun sendData() {
        viewModel.sendData()
        withContext(Main) {
            toast("Sent successfully")
            findNavController().navigate(R.id.action_questionFragment_to_cameraFragment)
        }
    }

    private fun initStuff(question: SelectQuestion) {
        title_tv.text = question.displayName

        val options = question.extraData

        val adapter: RecyclerView.Adapter<*> = when (question.type) {
            MULTI_SELECT -> {
                extra_text_tv.text = "Choose all relevant"
                MultiQuestionAdapter(options)
            }
            SELECT -> {
                extra_text_tv.text = "Choose one relevant"
                SelectQuestionAdapter(options)
            }
            else -> throw IllegalArgumentException("Not the right type of question")
        }

        options_rv.adapter = adapter

        next_btn.setOnClickListener {
            //            "Next question"
            val selected = (adapter as Selectable).getSelected()
            GlobalScope.launch(IO) {
                viewModel.saveSelected(question.id, selected)
                nextQuestionOrSendData()
            }
        }
    }

}
