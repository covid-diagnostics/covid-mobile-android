package com.example.coronadiagnosticapp.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey
    val id: Int,
    val name: String,
    val displayName: String,
    val displayNameEn: String?,
    val displayNameHe: String?,
    val qtype: QuestionType,
    val order: Int,
    @SerializedName("active")
    val isActive: Boolean,
    val extraData: String?,
    val extraDataEn: String?,
    val extraDataHe: String?,
    @SerializedName("required")
    val isRequired: Boolean,
    val addedOn: Date,
    val parent: Int?
)

enum class QuestionType {
    CHECKBOX,
    TEXT,

    @SerializedName("MULTISELECT")
    MULTI_SELECT,
    SELECT
}