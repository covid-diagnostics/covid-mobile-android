package com.example.coronadiagnosticapp.utils

import android.os.Build
import android.text.Html
import android.text.Spanned

//Use to convert raw html to displayable text
val String.asHTML: Spanned
    get() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(this)
        }
