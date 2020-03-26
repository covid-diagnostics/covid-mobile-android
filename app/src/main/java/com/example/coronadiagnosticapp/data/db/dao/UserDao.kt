package com.example.coronadiagnosticapp.data.db.dao

import androidx.room.*
import com.example.coronadiagnosticapp.data.db.entity.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.User

@Dao
interface UserDao {
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

}
