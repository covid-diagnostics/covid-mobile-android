package com.example.coronadiagnosticapp.data.db.entity

import com.google.gson.annotations.SerializedName

enum class SmokingStatus {
    @SerializedName("NEVER")
    NON,

    @SerializedName("STOPPED")
    SMOKER,

    @SerializedName("CURRENT")
    SMOKED5_Y_AGO
}