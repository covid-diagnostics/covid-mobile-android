package com.example.coronadiagnosticapp.data.db.entity.userResponse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    var id: Int?,
    @PrimaryKey
    val phoneNumberHash: String,
    var isActive: Boolean
)