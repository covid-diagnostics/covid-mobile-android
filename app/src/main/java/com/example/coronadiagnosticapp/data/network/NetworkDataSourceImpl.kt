package com.example.coronadiagnosticapp.data.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.User
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.util.*
import javax.inject.Inject


class NetworkDataSourceImpl @Inject constructor(private val api: ApiServer) : NetworkDataSource {

    private val _responseUser = MutableLiveData<ResponseUser>()
    private val _error = MutableLiveData<String>()
    override val responseUser: LiveData<ResponseUser> = _responseUser
    override val error: LiveData<String> = _error

    override suspend fun registerUser(userRegister: UserRegister): ResponseUser {

        try {
            return api.registerUser(userRegister).await()
        } catch (e: HttpException) {
            Log.e("HTTP", e.response().toString())
        }
        return api.registerUser(userRegister).await()
    }

    override suspend fun updateUserPersonalInformation(
        user: User
    ): User? {
        try {
            return api.updateUserInformation(user).await()

        } catch (e: HttpException) {
            Log.e("HTTP", e.response().toString())
        }
        return null
    }

    override suspend fun submitMeasurement(measurement: Measurement): Measurement {
        return api.submitMeasurement(measurement).await()
    }

    override suspend fun submitPpgMeasurement(measurement: PpgMeasurement): PpgMeasurement {
        return api.submitPpgMeasurement(measurement).await()
    }


    override suspend fun uploadAudioRecording(file: File, id: Int) {

        val body = file.asRequestBody("audio/*".toMediaType())

        val filePart = MultipartBody.Part.createFormData(
            "chestRecording", file.name, body
        )

        val idPart = MultipartBody.Part.createFormData("id", id.toString())

        return api.uploadAudioRecording(filePart, idPart).await()
    }

    override suspend fun getQuestions(): List<JsonObject> {
        val language = Locale.getDefault().language
        return api.getQuestions(language).await()
//        return dummyQuestions()

    }

    private fun dummyQuestions(): List<Question> {
        return listOf(
            Question(
                1, "sup", "What's up?",
                QuestionType.TEXT, emptyList()
            ),

            Question(
                2, "you_sick", "Are you sick?",
                QuestionType.CHECKBOX, emptyList()
            ),

            Question(
                3, "is_flat", "Is the world flat?",
                QuestionType.CHECKBOX, emptyList()
            ),

            Question(
                4, "name", "What's you're name?",
                QuestionType.CHECKBOX, emptyList()
            ),

            Question(
                5, "single_select_test", "Select one answer only",
                QuestionType.MULTI_SELECT, listOf(
                    ExtraData(
                        "cough",
                        "cough",
                        "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%3Fid%3DOIP.Em6Gv1fRpMRAbPTtNmAvPwAAAA%26pid%3DApi&f=1"
                    ),
                    ExtraData("Confusion", "Confusion", "URL")
                )
            ),

            Question(
                6, "symptoms", "What symptoms do you have?",
                QuestionType.SELECT, listOf(
                    ExtraData(
                        "cough",
                        "cough",
                        "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ftse2.mm.bing.net%2Fth%3Fid%3DOIP.Em6Gv1fRpMRAbPTtNmAvPwAAAA%26pid%3DApi&f=1"
                    ),
                    ExtraData("Confusion", "Confusion", "URL")
                )
            )


        )
    }

    override suspend fun sendAnswers(answers: List<AnswersResponse>) {
        for (answer in answers) {
            api.sendUserAnswer(answer).await()
        }
    }

}