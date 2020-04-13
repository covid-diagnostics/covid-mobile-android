package com.example.coronadiagnosticapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.coronadiagnosticapp.data.db.entity.AnonymousMetrics
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.User
import com.example.coronadiagnosticapp.ui.activities.testing_flow.BasicsInformation

@Dao
interface DbDao {
    @Transaction
    fun upsertUser(user: User) {
        deleteAllUsers()
        insert(user)
    }

    @Transaction
    fun upsertMetric(metric: ResponseMetric) {
        deleteAllMetrics()
        insert(metric)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)


    @Query("DELETE FROM user_table")
    fun deleteAllUsers()


    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): User

    @Query("DELETE FROM metric_table")
    fun deleteAllMetrics()


    @Query("SELECT * FROM metric_table LIMIT 1")
    fun getMetric(): ResponseMetric


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(responseMetric: ResponseMetric)


    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertHealth(healthResult: HealthResult)

    @Query("SELECT * FROM health_table ORDER BY date DESC LIMIT 1")
    fun getLastHealthResult(): LiveData<HealthResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBasicsInformation(basicsInformation: BasicsInformation)

    @Query("SELECT * FROM basics_information LIMIT 1")
    fun getBasicsInformation(): LiveData<BasicsInformation>

    @Query("SELECT * FROM basics_information LIMIT 1")
    fun getBasicsInformationWithoutLiveData(): BasicsInformation


    @Insert
    fun insertAnonymousMetrics(anonymousMetrics: AnonymousMetrics)

    @Query("SELECT * FROM anonymous_metrics_table")
    fun getAllAnonymousMetrics(): LiveData<List<AnonymousMetrics>>


}
