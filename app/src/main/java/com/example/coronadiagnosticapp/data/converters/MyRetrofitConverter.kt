package com.example.coronadiagnosticapp.data.converters

import com.example.coronadiagnosticapp.data.db.entity.question.*
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

class MyRetrofitConverter {

    fun deserializeToQuestion(json: JsonObject): Question? {
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()

        val type = gson.fromJson(json["qtype"], QuestionType::class.java)
            ?: return null

        return when (type) {
            QuestionType.CHECKBOX ->
                gson.fromJson(json, CheckBoxQuestion::class.java)
            QuestionType.TEXT ->
                gson.fromJson(json, TextQuestion::class.java)
            QuestionType.MULTI_SELECT,
            QuestionType.SELECT ->
                gson.fromJson(json, SelectQuestion::class.java)
        }
    }

    fun convertJsonToQuestionList(json: List<JsonObject>) =
        json.mapNotNull { deserializeToQuestion(it) }
}