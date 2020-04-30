package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


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
    fun stringToLongArray(string: String): Array<Long> =
        gson.fromJson(string, object : TypeToken<Array<Long>>() {}.type)
}