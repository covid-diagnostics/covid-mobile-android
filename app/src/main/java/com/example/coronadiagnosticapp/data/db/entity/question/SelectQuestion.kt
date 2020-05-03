package com.example.coronadiagnosticapp.data.db.entity.question

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "select_questions")
class SelectQuestion(
    id: Long,
    name: String,
    displayName: String,
    type: QuestionType,
    addedOn: Date,
    isRequired: Boolean,
    @SerializedName("jsonExtraData")
    val extraData: List<ExtraData>
) : Question(id, name, displayName, type, addedOn, isRequired) {

    data class ExtraData(
        val optionName: String,
        val optionValue: String,
        val optionImage: String//url
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SelectQuestion

        if (extraData != other.extraData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + extraData.hashCode()
        return result
    }


}