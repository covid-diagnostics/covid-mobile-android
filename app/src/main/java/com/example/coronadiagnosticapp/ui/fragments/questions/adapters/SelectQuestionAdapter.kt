package com.example.coronadiagnosticapp.ui.fragments.questions.adapters

import android.graphics.drawable.PictureDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.ui.fragments.questions.viewholders.SelectBoxVH


class SelectQuestionAdapter(
    private var options: List<SelectQuestion.ExtraData>,
    val requestBuilder: RequestBuilder<PictureDrawable>
) :
    RecyclerView.Adapter<SelectQuestionAdapter.SelectVH>(),
    Selectable {

    private var currentSelected = -1
    private var lastSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectVH(parent)

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: SelectVH, position: Int) {
        holder.fill(options[position])
    }

    override fun getSelected() =
        options.getOrNull(currentSelected)?.let {
            listOf(it)
        } ?: listOf()


    inner class SelectVH(viewGroup: ViewGroup) : SelectBoxVH(viewGroup,requestBuilder) {

        override val isSelected: Boolean
            get() = currentSelected == adapterPosition

        override fun setOnClickListener() = itemView.setOnClickListener {

            lastSelected = currentSelected
            if (lastSelected != -1)
                notifyItemChanged(lastSelected)

            currentSelected = adapterPosition

            setSelectedColor()
        }
    }
}