package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList


class Converters {
    val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun doubleArrayToString(array: Array<Double>): String = gson.toJson(array)

    @TypeConverter
    fun stringToDoubleArray(string: String): Array<Double> =
        gson.fromJson(string, object : TypeToken<Array<Double>>() {}.type)

    @TypeConverter
    fun longArrayToString(array: Array<Long>): String = gson.toJson(array)

    @TypeConverter
    fun fromQuestionType(value: String?) = value?.let { QuestionType.valueOf(it) }

    @TypeConverter
    fun questionTypeToString(value: QuestionType?) = value?.name

    @TypeConverter
    fun stringToLongArray(string: String): Array<Long> =
        gson.fromJson(string, object : TypeToken<Array<Long>>() {}.type)

    @TypeConverter
    fun fromList(list: List<ExtraData>) = gson.toJson(list)


    @TypeConverter
    fun toExtraDataList(json: String?): List<ExtraData> {
        json ?: return emptyList()
        val type = TypeToken
            .getParameterized(ArrayList::class.java, ExtraData::class.java).type

        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun toGeneralFeeling(value: String?) = stringToEnum<GeneralFeeling>(value)

    @TypeConverter
    fun generalFeelingToString(value: GeneralFeeling?) = enumToString(value)

    private fun enumToString(enum: Enum<*>?) = enum?.toString()

    private inline fun <reified T : Enum<T>> stringToEnum(value: String?) =
        value?.let { enumValueOf<T>(it) }

    //    maybe could be used for list stuff
    fun <T> toListOf(json: String?): List<T> {
        val type = object : TypeToken<ArrayList<T>>() {}.type
        return gson.fromJson<ArrayList<T>>(json, type) ?: emptyList()
    }
}