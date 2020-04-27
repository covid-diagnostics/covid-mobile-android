package com.example.coronadiagnosticapp.ui.fragments.questions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.Question
import com.example.coronadiagnosticapp.data.db.QuestionType.MULTI_SELECT
import com.example.coronadiagnosticapp.data.db.QuestionType.SELECT
import kotlinx.android.synthetic.main.fragment_question.*


class QuestionFragment : Fragment() {

    private val TAG = javaClass.name

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_question, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<Question>("q")?.let {
            initStuff(it)
        }

    }

    private fun initStuff(question: Question) {
        title_tv.text = question.displayName

        val options = question.extraData

        val adapter: RecyclerView.Adapter<*> = when (question.type) {
            MULTI_SELECT -> MultiQuestionAdapter(options)
            SELECT -> SelectQuestionAdapter(options)
            else -> throw IllegalArgumentException("Not the right type of question")
        }

        options_rv.adapter = adapter

        next_btn.setOnClickListener {
            //            "Next question"
            val selected = (adapter as Selectable<*>).getSelected()
            Log.d(TAG, selected.toString())
            //            TODO send selected back
            findNavController().navigateUp()
        }
    }


}
