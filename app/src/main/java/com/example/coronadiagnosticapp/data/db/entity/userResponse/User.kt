package com.example.coronadiagnosticapp.data.db.entity.userResponse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    var age: Int?,
    @PrimaryKey val deviceId: String,
    val email: String?,
    var firstName: String?,
    var id: Int?,
    var isActive: Boolean,
    var lastName: String?
)