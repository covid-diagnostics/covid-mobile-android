package com.example.coronadiagnosticapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.coronadiagnosticapp.data.db.entity.*

@Dao
interface DbDao {
    @Transaction
    fun upsertUser(user: User) {
        deleteAllUsers()
        insert(user)
    }

    @Transaction
    fun upsertMeasurement(measurement: Measurement) {
        deleteAllMeasurements()
        insert(measurement)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)


    @Query("DELETE FROM user_table")
    fun deleteAllUsers()


    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): User

    @Query("DELETE FROM measurement_table")
    fun deleteAllMeasurements()

    @Query("SELECT * FROM measurement_table LIMIT 1")
    fun getMeasurement(): Measurement

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(measurement: Measurement)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertHealth(healthResult: HealthResult)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(ppgMeasurement: PpgMeasurement)

    @Query("SELECT * FROM health_table ORDER BY date DESC LIMIT 1")
    fun getLastHealthResult(): LiveData<HealthResult>
}
