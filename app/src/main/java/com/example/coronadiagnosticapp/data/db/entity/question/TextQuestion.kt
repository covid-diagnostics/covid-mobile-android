package com.example.coronadiagnosticapp.data.db.entity.question

import androidx.room.Entity
import com.example.coronadiagnosticapp.data.db.entity.question.Question
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType
import java.util.*

@Entity(tableName = "text_questions")
class TextQuestion(
    id: Long,
    name: String,
    displayName: String,
    addedOn: Date,
    isRequired: Boolean
) : Question(id, name, displayName,
    QuestionType.TEXT, addedOn, isRequired)