package com.example.coronadiagnosticapp.ui.fragments.questions

import android.graphics.Color
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.ExtraData
import kotlinx.android.synthetic.main.question_select_box.view.*

class MultiQuestionAdapter(private var options: List<ExtraData>) :
    RecyclerView.Adapter<MultiQuestionAdapter.SelectBoxVH>(),
    Selectable {

    private val selectedFlags = Array(options.size) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectBoxVH(parent)

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: SelectBoxVH, position: Int) {
        holder.fill(options[position])
    }

    override fun getSelected() = options.filterIndexed { i, _ ->
        selectedFlags[i]
    }

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
                val i = adapterPosition

                selectedFlags[i] = !selectedFlags[i]

                setSelectedColor()
            }
        }

        private fun setSelectedColor() {
            val color =
                if (selectedFlags[adapterPosition]) Color.LTGRAY
                else Color.WHITE

            (itemView as? CardView)?.setCardBackgroundColor(color)
        }
    }
}