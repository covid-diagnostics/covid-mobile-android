package com.example.coronadiagnosticapp.data.db.entity

import com.google.gson.annotations.SerializedName

enum class SmokingStatus {
    @SerializedName("NEVER")
    NON,

    @SerializedName("CURRENT")
    SMOKER,

    @SerializedName("STOPPED")
    SMOKED5_Y_AGO
}