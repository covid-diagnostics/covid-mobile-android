package com.example.coronadiagnosticapp.utils

import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

fun TextView.setLinearGradientColors(@ColorInt vararg colors: Int) {
    val continueTxt = text.toString()
    val x1 = paint.measureText(continueTxt)
    paint.shader = LinearGradient(
        0F, 0F, x1, 0F, colors,
        floatArrayOf(0F, 1F),
        Shader.TileMode.CLAMP
    )
}

fun TextView.setDrawableStart(@DrawableRes res:Int) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(res,0, 0,0)
}