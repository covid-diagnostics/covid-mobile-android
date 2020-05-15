package com.example.coronadiagnosticapp.ui.fragments.questions

import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.vvalidator.util.hide
import com.afollestad.vvalidator.util.show
import com.bumptech.glide.Glide
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType.MULTI_SELECT
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType.SELECT
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.ui.activities.MainActivity
import com.example.coronadiagnosticapp.ui.fragments.questions.adapters.MultiQuestionAdapter
import com.example.coronadiagnosticapp.ui.fragments.questions.adapters.SelectQuestionAdapter
import com.example.coronadiagnosticapp.ui.fragments.questions.adapters.Selectable
import com.example.coronadiagnosticapp.ui.fragments.questions.viewmodels.QuestionFragmentViewModel
import com.example.coronadiagnosticapp.utils.getAppComponent
import com.example.coronadiagnosticapp.utils.svg.SvgSoftwareLayerSetter
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? MainActivity)?.hideStepperLayout()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.getAppComponent()?.inject(this)
    }

    private val requestBuilder by lazy {
        Glide.with(this)
            .`as`(PictureDrawable::class.java)
            .placeholder(R.drawable.ic_broken_image)
            .listener(SvgSoftwareLayerSetter())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        options_rv.adapter = MultiQuestionAdapter(emptyList(), requestBuilder)

        addScrollListener()

        nextQuestionOrSendData()
    }

    private fun addScrollListener() {
        options_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // Scrolling up
                if (dy > 0) onScrollUp()
                // Scrolling down
                else onScrollDown()
            }
        })
    }


    private fun onScrollDown() {
        question_box.show()
    }

    private fun onScrollUp() {
        question_box.hide()
    }

    private fun nextQuestionOrSendData() {
        GlobalScope.launch(IO) {
            viewModel.getNextQuestion()?.let {
                withContext(Main) {
//                    Animate
                    val animation = AnimationUtils
                        .loadAnimation(context, android.R.anim.slide_in_left)
                    view?.startAnimation(animation)
                    initStuff(it)

                }
            } ?: sendData()
        }
    }

    private suspend fun sendData() {
        viewModel.sendData()
        withContext(Main) {
            //toast("Sent successfully")
            findNavController().navigate(R.id.action_questionFragment_to_cameraFragment)
        }
    }

    private fun initStuff(question: SelectQuestion) {
        title_tv.text = question.displayName

        val options = question.extraData

        val adapter: RecyclerView.Adapter<*> = when (question.type) {
            MULTI_SELECT -> {
                extra_text_tv.text = getString(R.string.choose_all_relevant)
                MultiQuestionAdapter(options, requestBuilder)
            }
            SELECT -> {
                extra_text_tv.text = getString(R.string.choose_one_relevant)
                SelectQuestionAdapter(options, requestBuilder)
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
