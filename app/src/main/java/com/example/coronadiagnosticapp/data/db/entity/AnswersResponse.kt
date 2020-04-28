package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose

@Entity(tableName = "answers")
class AnswersResponse(
    @PrimaryKey
    @Expose(serialize = false, deserialize = false)
    val id: Long,
    val value: String?,
    val question: Long, val measurement: Int
)