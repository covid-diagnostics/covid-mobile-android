package com.example.coronadiagnosticapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.User

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

}
