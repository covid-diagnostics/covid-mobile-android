package com.example.coronadiagnosticapp.data.db.entity.question

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "check_box_questions")
class CheckBoxQuestion(
    id: Long,
    name: String,
    displayName: String,
    addedOn: Date,
    isRequired: Boolean,
    @SerializedName("jsonExtraData")
    val extraData: ExtraData
) : Question(id, name, displayName,
    QuestionType.CHECKBOX, addedOn, isRequired) {

    data class ExtraData(val img: String)
}