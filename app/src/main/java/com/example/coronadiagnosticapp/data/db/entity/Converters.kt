package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList


class Converters {
    val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?) = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?) = date?.time

    @TypeConverter
    fun fromQuestionType(value: String?) = value?.let { QuestionType.valueOf(it) }

    @TypeConverter
    fun questionTypeToString(value: QuestionType?) = value?.name

    @TypeConverter
    fun fromExtraData(extraData: ExtraData?): String? {
        extraData ?: return null
        return Gson().toJson(extraData)
    }

    @TypeConverter
    fun jsonToExtraData(json: String?): ExtraData? {
        json ?: return null
        return Gson().fromJson(json, ExtraData::class.java)
    }

    @TypeConverter
    fun fromList(list: List<ExtraData>) = Gson().toJson(list)


    @TypeConverter
    fun toExtraDataList(json: String?): List<ExtraData> {
        json ?: return emptyList()
        val type = TypeToken
            .getParameterized(ArrayList::class.java, ExtraData::class.java).type

        return Gson().fromJson(json, type)
    }

    fun toStringList(json: String): List<String> {
        val type = TypeToken
            .getParameterized(ArrayList::class.java, String::class.java).type
        return Gson().fromJson(json, type)
    }

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
}