package com.example.coronadiagnosticapp.ui.fragments.questions.adapters

import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.ui.fragments.questions.viewholders.SelectBoxVH

class MultiQuestionAdapter(
    private var options: List<SelectQuestion.ExtraData>,
    val requestBuilder: RequestBuilder<PictureDrawable>
) :
    RecyclerView.Adapter<MultiQuestionAdapter.SelectVH>(),
    Selectable {


    private val selectedFlags = Array(options.size) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectVH(parent)


    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: SelectVH, position: Int) {
        holder.fill(options[position])
    }

    override fun getSelected() = options.filterIndexed { i, _ ->
        selectedFlags[i]
    }

    inner class SelectVH(viewGroup: ViewGroup) : SelectBoxVH(viewGroup,requestBuilder) {

        override val isSelected: Boolean
            get() = selectedFlags[adapterPosition]

        override fun setOnClickListener() {
            itemView.setOnClickListener {
                val i = adapterPosition

                selectedFlags[i] = !selectedFlags[i]

                setSelectedColor()
            }
        }
    }
}