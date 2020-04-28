package com.example.coronadiagnosticapp.data.repository

import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.SharedProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val networkDataSource: NetworkDataSource,
    val dao: DbDao,
    val sharedProvider: SharedProvider,
    val tokenServiceInterceptor: TokenServiceInterceptor
) : Repository {
    companion object {
        private var lastHealthResult: HealthResult? = null
        private var breathingRate_: Double = -1.0
    }

    override var breathingRate: Double
        get() = breathingRate_
        set(value) {
            breathingRate_ = value
        }

    private lateinit var responseUser: ResponseUser
    override val error: MutableLiveData<String> = MutableLiveData()


    override suspend fun registerUser(userRegister: UserRegister) {
        try {
            val responseUser = networkDataSource.registerUser(userRegister)
            dao.upsertUser(responseUser.user)
            tokenServiceInterceptor.sessionToken = responseUser.token.access
            sharedProvider.setToken(responseUser.token.access)
        } catch (e: Exception) {
            error.postValue(e.message)
        }

    }

    override fun isLoggedIn(): Boolean {
        val tokenInterceptor = tokenServiceInterceptor.sessionToken
        if (!tokenInterceptor.isNullOrBlank()) return true

        val tokenFromPreference = sharedProvider.getToken()
        if (tokenFromPreference.isNullOrBlank()) return false

        tokenServiceInterceptor.sessionToken = tokenFromPreference
        return true
    }

    override suspend fun updateUserPersonalInformation(
        firstName: String,
        lastName: String,
        age: Int
    ) {
        val user = dao.getUser()
        user.apply {
            this.firstName = firstName
            this.lastName = lastName
            this.age = age
        }
        val userRes = networkDataSource.updateUserPersonalInformation(user)
        if (userRes != null) {
            dao.upsertUser(userRes)
            sharedProvider.setName(userRes.firstName)
        }
    }

    override suspend fun updateUserMetrics(temp: String, cough: Int, isWet: Boolean) {
        try {
            val responseMetric = networkDataSource.updateUserMetrics(cough, isWet, temp)
            dao.upsertMetric(responseMetric)

        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }


    override suspend fun saveResult(healthResult: HealthResult) {
        lastHealthResult = healthResult
        dao.insertHealth(healthResult)
    }

    override fun getLastResult() = lastHealthResult
    override fun getUserName() = sharedProvider.getName()

    override suspend fun uploadAudioRecording(file: File) {
        try {
            val id = dao.getMetric().id
            networkDataSource.uploadAudioRecording(file, id)

        } catch (e: Exception) {
            error.postValue(e.message)
        }
    }

    override suspend fun getQuestions(): List<Question> {
        val questionsJson = networkDataSource.getQuestions()
        val questions = mutableListOf<Question>()
        val gson = Gson()
        for (jsonObject in questionsJson) {

            val id = jsonObject["id"].asLong
            val name = jsonObject["name"].asString
            val displayName = jsonObject["displayName"].asString
            val required = jsonObject["required"].asBoolean

            val type = gson.fromJson(jsonObject["qtype"], QuestionType::class.java)

            val extraData = convertExtraData(jsonObject, type, gson)

            val dateString = jsonObject["addedOn"].asString
            val addedOn = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()
            ).parse(dateString)!!


            val question = Question(
                id, name, displayName,
                type, extraData, addedOn, required
            )

            questions.add(question)
        }

        dao.insert(questions)

        return questions
    }

    private fun convertExtraData(
        jsonObject: JsonObject,
        type: QuestionType?,
        gson: Gson
    ): List<ExtraData> {
        val jsonElementExtra = jsonObject["extraData"]

        val extraData = when (type) {
            QuestionType.CHECKBOX -> {
                val value = gson.fromJson(
                    jsonElementExtra.asString,
                    CheckBoxExtraData::class.java
                )

                listOf(ExtraData(value.img))
            }

            QuestionType.MULTI_SELECT,
            QuestionType.SELECT -> {
                Converters().toExtraDataList(jsonElementExtra.asString)
            }

            QuestionType.TEXT, null -> emptyList()

        }

        return extraData
    }

    override suspend fun getNextSelectableQuestion(currentQuestion: Question?): Question? {
        val questions = dao.getQuestions(QuestionType.SELECT, QuestionType.MULTI_SELECT)
        if (currentQuestion == null)
            return questions.firstOrNull()

        val index = questions.indexOf(currentQuestion)

        return if (index != -1) questions.getOrNull(index + 1)
        else null
    }

    override suspend fun addAnswer(answer: AnswersResponse) {
        val id = dao.getMetric().id
        answer.measurement = id
        dao.insert(answer)
    }

    override suspend fun sendUserAnswers() {
        val answers = dao.getAnswers()
        networkDataSource.sendAnswers(answers)
    }
}