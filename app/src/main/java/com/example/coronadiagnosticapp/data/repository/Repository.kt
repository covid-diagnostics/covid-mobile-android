package com.example.coronadiagnosticapp.data.repository

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.question.Question
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.example.coronadiagnosticapp.ui.fragments.oxymeter.OxymeterAverages
import java.io.File

interface Repository {
    val error: MutableLiveData<String>
    var breathingRate: Double

    suspend fun registerUser(userRegister: UserRegister)
    suspend fun setCountry(country: String)
    fun isLoggedIn(): Boolean
    suspend fun updateUserPersonalInformation(
        sex: Sex,
        age: Int,
        height: Int,
        weight: Int
    )

    suspend fun updateBackgroundDiseases(diseases: List<BackDiseases>)
    suspend fun saveResult(healthResult: HealthResult)
    fun getLastResult(): HealthResult?
    suspend fun uploadAudioRecording(file: File)
    fun getIsFirstTime(): Boolean
    fun setIsFirstTime(isFirstTime: Boolean)
    suspend fun submitMeasurement(measurement: Measurement): Measurement

    @RequiresApi(value = 23)
    suspend fun submitPpgMeasurement(
        oxymeterAverages: OxymeterAverages,
        cc: CameraCharacteristics
    )

    suspend fun getQuestions(type: QuestionType): List<Question>

    suspend fun getNextSelectableQuestion(currentQuestion: SelectQuestion?): SelectQuestion?
    suspend fun addAnswer(answer: AnswersResponse)
    suspend fun sendUserAnswers()
    suspend fun addAnswers(answers: List<AnswersResponse>)
    suspend fun loadQuestionsToDB(): List<Question>
    suspend fun getMeasurementCount(): Int
    suspend fun getLastMeasurementId(): Int
    suspend fun getSimpleQuestions(): List<Question>
    suspend fun saveSmokeStatus(smokingStatus: SmokingStatus)
    var didSetNotificationTime: Boolean
}