package com.example.coronadiagnosticapp.utils

import android.widget.EditText

fun EditText.textAsInt() =
    text.toString().toIntOrNull()

fun EditText.textAsDouble() =
    text.toString().toDoubleOrNull()

fun EditText.textAsFloat() =
    text.toString().toFloatOrNull()

