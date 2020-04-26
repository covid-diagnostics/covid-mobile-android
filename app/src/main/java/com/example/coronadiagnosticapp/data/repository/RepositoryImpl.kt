package com.example.coronadiagnosticapp.data.repository

import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.example.coronadiagnosticapp.data.db.dao.DbDao
import com.example.coronadiagnosticapp.data.db.entity.UserCameraInfo
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.UserRegister
import com.example.coronadiagnosticapp.data.network.NetworkDataSource
import com.example.coronadiagnosticapp.data.network.TokenServiceInterceptor
import com.example.coronadiagnosticapp.data.providers.SharedProvider
import java.io.File
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    val networkDataSource: NetworkDataSource,
    val dao: DbDao,
    val sharedProvider: SharedProvider,
    val tokenServiceInterceptor: TokenServiceInterceptor
) : Repository {
    companion object {
        private var lastHealthResult : HealthResult? = null
        private var breathingRate_: Double = -1.0
    }
    override var breathingRate : Double
        get() = breathingRate_
        set(value) { breathingRate_ = value }

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
            val responseMetric = networkDataSource.updateUserMetrics(temp, cough, isWet)
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun updateUserCameraCharacteristics(cc: CameraCharacteristics) {
        val sensitivityRange = cc[CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE]
        Log.i("ASDASD", "INSERTED CC")

        dao.insertCameraInfo(UserCameraInfo(
            "test",
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
            cc[CameraCharacteristics.SENSOR_REFERENCE_ILLUMINANT2]
        ))
    }
}