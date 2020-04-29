package com.example.coronadiagnosticapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.userResponse.User

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


    @Insert(onConflict = REPLACE)
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

    @Insert(onConflict = REPLACE)
    fun insert(answer: AnswersResponse)

    @Query("SELECT * FROM answers")
    fun getAnswers(): List<AnswersResponse>

    @Insert(onConflict = REPLACE)
    fun insertAnswers(answers: List<AnswersResponse>)

    @Insert(onConflict = REPLACE)
    fun insertQuestions(questions: List<Question>)

    @Query("SELECT * FROM questions WHERE type IN(:type)")
    fun getQuestions(vararg type: QuestionType): List<Question>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertHealth(healthResult: HealthResult)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(ppgMeasurement: PpgMeasurement)

    @Query("SELECT * FROM health_table ORDER BY date DESC LIMIT 1")
    fun getLastHealthResult(): LiveData<HealthResult>
}
