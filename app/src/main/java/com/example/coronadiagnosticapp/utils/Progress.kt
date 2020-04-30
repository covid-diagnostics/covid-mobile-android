package com.example.coronadiagnosticapp.utils

import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ProgressBar

fun showLoading(progress: ProgressBar, show: Boolean) {
    progress.visibility = if (show) VISIBLE else GONE
}
//TODO use a progress dialog object or some lib
