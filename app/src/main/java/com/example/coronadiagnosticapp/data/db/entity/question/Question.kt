package com.example.coronadiagnosticapp.data.db.entity.question

import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

abstract class Question(
    @PrimaryKey
    val id: Long,
    var name: String,
    var displayName: String,
    @SerializedName("qtype")
    var type: QuestionType,
    val addedOn: Date,
    @SerializedName("required")
    var isRequired: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Question

        if (id != other.id) return false
        if (name != other.name) return false
        if (displayName != other.displayName) return false
        if (type != other.type) return false
        if (addedOn != other.addedOn) return false
        if (isRequired != other.isRequired) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + addedOn.hashCode()
        result = 31 * result + isRequired.hashCode()
        return result
    }
}


enum class QuestionType {
    CHECKBOX,
    TEXT,

    @SerializedName("MULTISELECT")
    MULTI_SELECT,
    SELECT;
}
