package com.example.coronadiagnosticapp.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.coronadiagnosticapp.data.di.AppComponent

fun Context.toast(msg: String, duration: Int = LENGTH_SHORT) =
    Toast.makeText(this, msg, duration).show()

fun Context.toast(@StringRes msg: Int, duration: Int = LENGTH_SHORT) =
    Toast.makeText(this, msg, duration).show()

fun Fragment.toast(msg: String, duration: Int = LENGTH_SHORT) =
    context?.toast(msg, duration)

fun Fragment.toast(@StringRes msg: Int, duration: Int = LENGTH_SHORT) =
    context?.toast(msg, duration)


//App component shorter
fun Context.getAppComponent(): AppComponent =
    (applicationContext as MyApplication).getAppComponent()
