package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.TypeConverter
import com.example.coronadiagnosticapp.data.db.QuestionType
import java.util.*


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?) = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?) = date?.time

    @TypeConverter
    fun fromQuestionType(value: String?) = value?.let { QuestionType.valueOf(it) }

    @TypeConverter
    fun questionTypeToString(value: QuestionType?) = value?.name
}