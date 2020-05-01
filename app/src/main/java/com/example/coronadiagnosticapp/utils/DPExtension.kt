package com.example.coronadiagnosticapp.utils

import android.content.Context
import android.util.TypedValue

fun Float.toDP(context: Context) =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    )

fun Int.toDP(context: Context) = toFloat().toDP(context).toInt()