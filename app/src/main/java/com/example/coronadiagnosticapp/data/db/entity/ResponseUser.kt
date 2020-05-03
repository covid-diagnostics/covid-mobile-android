package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


data class ResponseUser(
    val token: Token,
    val user: User
)


data class Token(
    var access: String,
    var refresh: String
)

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val phoneNumberHash: String,
    val email: String?,
    var firstName: String?,
    var id: Int?,
    var isActive: Boolean,
    var lastName: String?
)

data class UserRegister(
    var phoneNumberHash: String
)
