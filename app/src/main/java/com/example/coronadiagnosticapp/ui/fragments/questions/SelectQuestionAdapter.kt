package com.example.coronadiagnosticapp.ui.fragments.questions

import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.ExtraData
import kotlinx.android.synthetic.main.question_select_box.view.*


class SelectQuestionAdapter(private var options: List<ExtraData>) :
    RecyclerView.Adapter<SelectQuestionAdapter.SelectBoxVH>(),
    Selectable<ExtraData?> {

    private var currentSelected = -1
    private var lastSelected = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectBoxVH(parent)

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: SelectBoxVH, position: Int) {
        holder.fill(options[position])
    }

    override fun getSelected() = options.getOrNull(currentSelected)

    inner class SelectBoxVH(viewGroup: ViewGroup) :
        BaseViewHolder(viewGroup, R.layout.question_select_box) {

        private val nameTv by lazy { itemView.name_tv }
        private val iconView by lazy { itemView.icon }

        fun fill(data: ExtraData) {
            nameTv.text = data.optionName

            Glide.with(iconView)
                .load(data.optionImage)
                .placeholder(R.drawable.smail_emoji)
                .fallback(R.drawable.runny_nose)
                .transform(RoundedCorners(16))
                .into(iconView)

            setSelectedColor()

            itemView.setOnClickListener {

                lastSelected = currentSelected
                if (lastSelected != -1)
                    notifyItemChanged(lastSelected)

                currentSelected = adapterPosition

                setSelectedColor()
            }
        }

        private fun setSelectedColor() {
            val color = if (currentSelected == adapterPosition) {
                Color.CYAN
            } else Color.WHITE

            itemView.setBackgroundColor(color)
        }
    }
}