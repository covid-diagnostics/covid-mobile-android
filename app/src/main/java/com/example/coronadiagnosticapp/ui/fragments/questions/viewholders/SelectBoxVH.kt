package com.example.coronadiagnosticapp.ui.fragments.questions.viewholders

import android.graphics.Color
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.coronadiagnosticapp.R
import com.example.coronadiagnosticapp.data.db.entity.ExtraData
import kotlinx.android.synthetic.main.question_select_box.view.*
abstract class SelectBoxVH(viewGroup: ViewGroup) :
        BaseViewHolder(viewGroup, R.layout.question_select_box) {

        private val nameTv by lazy { itemView.name_tv }
        private val iconView by lazy { itemView.icon }

        open fun fill(data: ExtraData) {
            nameTv.text = data.optionName

            Glide.with(iconView)
                .load(data.optionImage)
                .placeholder(R.drawable.smail_emoji)
                .fallback(R.drawable.runny_nose)
                .transform(RoundedCorners(16))
                .into(iconView)

            setSelectedColor()

            setOnClickListener()
        }

        abstract fun setOnClickListener()

        abstract val isSelected:Boolean

        protected fun setSelectedColor() {
            val color = if (isSelected) Color.LTGRAY else Color.WHITE
            (itemView as CardView).setCardBackgroundColor(color)
        }
    }