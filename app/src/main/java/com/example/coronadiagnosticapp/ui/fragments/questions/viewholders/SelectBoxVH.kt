package com.example.coronadiagnosticapp.ui.fragments.questions.viewholders

import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import kotlinx.android.synthetic.main.question_select_box.view.*

abstract class SelectBoxVH(viewGroup: ViewGroup) :
    BaseViewHolder(viewGroup, R.layout.question_select_box) {

    private val nameTv by lazy { itemView.name_tv }
    private val iconView by lazy { itemView.icon }

    @ColorInt
    private val selectedColor: Int = itemView.context.resources
        .getColor(R.color.colorPrimary)

    open fun fill(data: SelectQuestion.ExtraData) {
        nameTv.text = data.optionName

        Glide.with(iconView)
            .load(data.optionImage)
            .placeholder(R.drawable.ic_broken_image)
            .circleCrop()
            .into(iconView)

        setSelectedColor()

        setOnClickListener()
    }

    abstract fun setOnClickListener()

    abstract val isSelected: Boolean

    protected fun setSelectedColor() {
        if (isSelected) {
            (itemView as CardView).setCardBackgroundColor(selectedColor)
            nameTv.setTextColor(Color.WHITE)
        } else {
            (itemView as CardView).setCardBackgroundColor(Color.WHITE)
            nameTv.setTextColor(Color.BLACK)
        }
    }
}