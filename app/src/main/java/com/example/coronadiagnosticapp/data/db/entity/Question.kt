package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "questions")
data class
Question(
    @PrimaryKey
    val id: Long,
    var name: String,
    var displayName: String,
    @SerializedName("qtype")
    var type: QuestionType,
    @Expose(serialize = false, deserialize = false)
    var extraData: List<ExtraData>,
    val addedOn: Date,
    @SerializedName("required")
    var isRequired: Boolean = true
) {

    constructor(
        id: Long,
        name: String,
        displayName: String,
        type: QuestionType,
        extraData: List<ExtraData>
    ) : this(id, name, displayName, type, extraData, Date())
}

data class ExtraData(
    val optionName: String,
    val optionValue: String,
    val optionImage: String//url
) {
    constructor(optionImage: String) :
            this("", "", optionImage)
}

enum class QuestionType {
    CHECKBOX,
    TEXT,

    @SerializedName("MULTISELECT")
    MULTI_SELECT,
    SELECT;
}

data class CheckBoxExtraData(val img: String)
