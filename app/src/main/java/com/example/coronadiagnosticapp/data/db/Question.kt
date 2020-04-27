package com.example.coronadiagnosticapp.data.db

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.util.*

data class Question(
    val id: Int,
    val name: String,
    val displayName: String,
    @SerializedName("qtype")
    val type: QuestionType,
    @SerializedName("jsonExtraData")
    val extraData: List<ExtraData>,
    @SerializedName("required")
    val isRequired: Boolean,
    val addedOn: Date,
    val parent: Int?
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString()!!,
        source.readString()!!,
        QuestionType.values()[source.readInt()],
        source.createTypedArrayList(ExtraData.CREATOR) ?: emptyList(),
        1 == source.readInt(),
        source.readSerializable() as Date,
        source.readValue(Int::class.java.classLoader) as Int?
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(id)
        writeString(name)
        writeString(displayName)
        writeInt(type.ordinal)
        writeTypedList(extraData)
        writeInt((if (isRequired) 1 else 0))
        writeSerializable(addedOn)
        writeValue(parent)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Question> = object : Parcelable.Creator<Question> {
            override fun createFromParcel(source: Parcel): Question = Question(source)
            override fun newArray(size: Int): Array<Question?> = arrayOfNulls(size)
        }
    }
}


class ExtraData(
    val optionName: String,
    val optionValue: String,
    val optionImage: String//url
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(optionName)
        writeString(optionValue)
        writeString(optionImage)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ExtraData> = object : Parcelable.Creator<ExtraData> {
            override fun createFromParcel(source: Parcel): ExtraData = ExtraData(source)
            override fun newArray(size: Int): Array<ExtraData?> = arrayOfNulls(size)
        }
    }
}

enum class QuestionType {
    CHECKBOX,
    TEXT,

    @SerializedName("MULTISELECT")
    MULTI_SELECT,
    SELECT
}