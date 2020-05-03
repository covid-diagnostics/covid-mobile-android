package com.example.coronadiagnosticapp.ui.fragments.questions.viewholders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class BaseViewHolder(viewGroup: ViewGroup, @LayoutRes layout: Int) :
    RecyclerView.ViewHolder(inflate(viewGroup, layout)) {

    companion object {
        private fun inflate(viewGroup: ViewGroup, layout: Int): View {
            val inflater = LayoutInflater.from(viewGroup.context)
            return inflater.inflate(layout, viewGroup, false)
        }
    }
}