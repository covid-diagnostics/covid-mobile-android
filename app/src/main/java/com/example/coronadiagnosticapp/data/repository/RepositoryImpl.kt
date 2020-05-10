package com.example.coronadiagnosticapp.data.repository

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.converters.MyRetrofitConverter
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.question.Question
import com.example.coronadiagnosticapp.data.db.entity.question.QuestionType
import com.example.coronadiagnosticapp.data.db.entity.question.SelectQuestion
import com.example.coronadiagnosticapp.data.db.entity.userResponse.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.userResponse.UserRegister
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.SharedProvider
import com.example.coronadiagnosticapp.ui.activities.oxymeter.OxymeterAverages
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val networkDataSource: NetworkDataSource,
    val dao: DbDao,
    val sharedProvider: SharedProvider,
    val tokenServiceInterceptor: TokenServiceInterceptor
) : Repository {
    companion object {
        const val TAG = "Repository"
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
        val tokenFromPreference = sharedProvider.getToken()
        if (!tokenInterceptor.isNullOrBlank()) return true
        return if (!tokenFromPreference.isNullOrBlank()) {
            tokenServiceInterceptor.sessionToken = tokenFromPreference
            true
        } else {
            false
        }
    }


    override suspend fun setCountry(country:String){
        dao.deleteAllUsersInfo()
        val userInfo = UserInfo(null, null, null, null, null, country,  ArrayList())
        dao.upsertUserInfo(userInfo)
    }

    override suspend fun updateUserPersonalInformation(
        sex: Sex, age: Int, height: Int, weight: Int
    ) {
        val userInfo = dao.getUserInfo()
        userInfo.apply {
            this.sex = sex
            this.age = age
            this.height = height
            this.weight = weight
        }
        dao.insertUserInfo(userInfo)
    }

    override suspend fun saveSmokeStatus(smokingStatus: SmokingStatus) {
        val userInfo = dao.getUserInfo()
        userInfo.smokingStatus = smokingStatus
        dao.upsertUserInfo(userInfo)
    }

    override suspend fun updateBackgroundDiseases(backgroundDiseases: List<BackDiseases>){
        val userInfo = dao.getUserInfo()
        userInfo.backgroundDiseases = backgroundDiseases

        val userInfoRes = networkDataSource.updateUserInfo(userInfo)
        if(userInfoRes != null) {
            dao.upsertUserInfo(userInfoRes)
        }
    }

    override suspend fun saveResult(healthResult: HealthResult) {
        lastHealthResult = healthResult
        dao.insertHealth(healthResult)
    }

    override fun getLastResult() = lastHealthResult

    override fun getIsFirstTime() = sharedProvider.getIsFirstTime()
    override fun setIsFirstTime(isFirstTime: Boolean) = sharedProvider.setIsFirstTime(isFirstTime)

    override suspend fun uploadAudioRecording(file: File) {
        try {
            val id = dao.getMeasurement().id!!
            networkDataSource.uploadAudioRecording(file, id)

        } catch (e: Exception) {
            error.postValue(e.message)
            e.printStackTrace()
        }
    }

    override suspend fun submitMeasurement(measurement: Measurement): Measurement {
        val responseMeasurement = networkDataSource.submitMeasurement(measurement)
        dao.upsertMeasurement(responseMeasurement)
        return responseMeasurement
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override suspend fun submitPpgMeasurement(
        oxymeterAverages: OxymeterAverages,
        cc: CameraCharacteristics
    ) {
        Log.i(TAG, "Sending camera characteristics!")
        val sensitivityRange = cc[CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE]
        val ppgMeasurement = PpgMeasurement(
            null,
            oxymeterAverages.red,
            oxymeterAverages.green,
            oxymeterAverages.blue,
            oxymeterAverages.timepoint,
            cc[CameraCharacteristics.SENSOR_CALIBRATION_TRANSFORM1]?.toString(),
            cc[CameraCharacteristics.SENSOR_CALIBRATION_TRANSFORM2]?.toString(),
            cc[CameraCharacteristics.SENSOR_COLOR_TRANSFORM1]?.toString(),
            cc[CameraCharacteristics.SENSOR_COLOR_TRANSFORM2]?.toString(),
            cc[CameraCharacteristics.SENSOR_FORWARD_MATRIX1]?.toString(),
            cc[CameraCharacteristics.SENSOR_FORWARD_MATRIX2]?.toString(),
            cc[CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED],
            sensitivityRange?.lower,
            sensitivityRange?.upper,
            cc[CameraCharacteristics.SENSOR_INFO_WHITE_LEVEL],
            cc[CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY],
            cc[CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT1],
            cc[CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT2],
            dao.getMeasurement().id!!
        )

        try {
            networkDataSource.submitPpgMeasurement(ppgMeasurement)
        } catch (e: Exception) {
            error.postValue(e.message)
            e.printStackTrace()
        }
    }

    override suspend fun getQuestions(type: QuestionType) = dao.getQuestions(type)

    override suspend fun getSimpleQuestions() = dao.getSimpleQuestions()

    override suspend fun loadQuestionsToDB(): List<Question> {

        val questionsJson = networkDataSource.getQuestions()
        val questions = MyRetrofitConverter()
            .convertJsonToQuestionList(questionsJson)

        dao.insertQuestions(questions)
        return questions
    }

    override suspend fun getNextSelectableQuestion(currentQuestion: SelectQuestion?): SelectQuestion? {
        val questions = dao.getSelectQuestions()

        if (currentQuestion == null)
            return questions.firstOrNull()

        val index = questions.indexOf(currentQuestion)

        return if (index != -1) questions.getOrNull(index + 1)
        else null
    }

    override suspend fun addAnswer(answer: AnswersResponse) {
        addMeasurement(answer)
        dao.insertUser(answer)
    }

    private fun addMeasurement(answer: AnswersResponse) {
        answer.measurement = dao.getMeasurement().id!!
    }

    override suspend fun addAnswers(answers: List<AnswersResponse>) {
        answers.forEach(this::addMeasurement)
        dao.insertAnswers(answers)
    }

    override suspend fun sendUserAnswers() {
        val answers: List<AnswersResponse> = dao.getAnswers()
        networkDataSource.sendAnswers(answers)
    }
}