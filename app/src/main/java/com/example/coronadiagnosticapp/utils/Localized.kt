package com.example.coronadiagnosticapp.utils

import android.content.Context
import androidx.annotation.StringRes

interface Localizable {
    @get:StringRes
    val stringRes:Int

    fun getString(context: Context){
        context.getString(stringRes)
    }
}

//Example of usage
//enum class SwitchState:Localizable {
//    ON{
//        override val stringRes: Int = R.string.on
//    },
//    OFF{
//        override val stringRes: Int = R.string.off
//    }
//}