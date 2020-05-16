package com.example.coronadiagnosticapp.ui.views

import android.content.Context
import android.graphics.drawable.Animatable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.example.coronadiagnosticapp.R

class MorphButton : ImageView {

    private var isFirst = true

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    init {
        setImageResource(R.drawable.play_to_pause)
    }

    fun toggleAnimation() = if (isFirst) playToPause() else pauseToPlay()

    fun playToPause() {
        animateToImage(R.drawable.play_to_pause)
        isFirst = false
    }

    fun pauseToPlay() {
        animateToImage(R.drawable.pause_to_play)
        isFirst = true
    }

    private fun animateToImage(@DrawableRes drawableId: Int) {
        setImageResource(drawableId)
        (drawable as? Animatable)?.start()
    }
}