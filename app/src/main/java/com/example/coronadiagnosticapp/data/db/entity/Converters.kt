package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.TypeConverter
import com.example.coronadiagnosticapp.data.db.entity.question.CheckBoxQuestion
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


class Converters {
    val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?) = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?) = date?.time

    @TypeConverter
    fun stringToSex(value: String?) = stringToEnum<Sex>(value)

    @TypeConverter
    fun sexToString(value: Sex?) = enumToString(value)

    @TypeConverter
    fun doubleArrayToString(array: Array<Double>): String = gson.toJson(array)

    @TypeConverter
    fun stringToDoubleArray(string: String): Array<Double> =
        gson.fromJson(string, object : TypeToken<Array<Double>>() {}.type)

    @TypeConverter
    fun longArrayToString(array: Array<Long>): String = gson.toJson(array)

    @TypeConverter
    fun stringToLongArray(string: String): Array<Long> =
        gson.fromJson(string, object : TypeToken<Array<Long>>() {}.type)

    @TypeConverter
    fun fromQuestionType(value: String?) = value?.let { QuestionType.valueOf(it) }

    @TypeConverter
    fun questionTypeToString(value: QuestionType?) = value?.name

    @TypeConverter
    fun fromSelectList(list: List<SelectQuestion.ExtraData>): String? = gson.toJson(list)

    @TypeConverter
    fun stringListToString(array: List<String>): String = gson.toJson(array)

    @TypeConverter
    fun stringToStringList(string: String): List<String> =
        gson.fromJson(string, object : TypeToken<List<String>>() {}.type)

    @TypeConverter
    fun toSelectExtraDataList(json: String?): List<SelectQuestion.ExtraData> {
        json ?: return emptyList()
        val type = object : TypeToken<ArrayList<SelectQuestion.ExtraData>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromCheckBoxList(list: List<CheckBoxQuestion.ExtraData>) = gson.toJson(list)

    @TypeConverter
    fun toCheckBoxExtraDataList(json: String?): List<CheckBoxQuestion.ExtraData> {
        json ?: return emptyList()
        val type = object : TypeToken<ArrayList<CheckBoxQuestion.ExtraData>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromCheckBoxExtra(extraData: CheckBoxQuestion.ExtraData?) = extraData?.img

    @TypeConverter
    fun toCheckBoxExtra(value: String?) = value?.let { CheckBoxQuestion.ExtraData(it) }

    @TypeConverter
    fun fromSelectExtraData(extraData: SelectQuestion.ExtraData?): String? {
        return extraData?.let { gson.toJson(extraData) }
    }

    @TypeConverter
    fun toSelectExtraData(value: String?) = value?.let {
        gson.fromJson(value, SelectQuestion.ExtraData::class.java)
    }

    @TypeConverter
    fun toSmokeStatus(value: String?) = stringToEnum<SmokingStatus>(value)

    @TypeConverter
    fun smokeStatusToString(value: SmokingStatus?) = enumToString(value)

    @TypeConverter
    fun toGeneralFeeling(value: String?) = stringToEnum<GeneralFeeling>(value)

    @TypeConverter
    fun generalFeelingToString(value: GeneralFeeling?) = enumToString(value)

    @TypeConverter
    fun toDiseaseList(value: String?): List<BackDiseases> {
        val type = object : TypeToken<ArrayList<BackDiseases>>() {}.type
        return gson.fromJson<ArrayList<BackDiseases>>(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromDiseaseList(list: List<BackDiseases>): String? = gson.toJson(list)

    private fun enumToString(enum: Enum<*>?) = enum?.toString()

    private inline fun <reified T : Enum<T>> stringToEnum(value: String?) =
        value?.let { enumValueOf<T>(it) }
}