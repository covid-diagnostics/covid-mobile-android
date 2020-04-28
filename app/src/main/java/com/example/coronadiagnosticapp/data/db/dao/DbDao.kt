package com.example.coronadiagnosticapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.example.coronadiagnosticapp.data.db.entity.AnswersResponse
import com.example.coronadiagnosticapp.data.db.entity.HealthResult
import com.example.coronadiagnosticapp.data.db.entity.Question
import com.example.coronadiagnosticapp.data.db.entity.QuestionType
import com.example.coronadiagnosticapp.data.db.entity.responseMetric.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.userResponse.User

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


    @Insert(onConflict = REPLACE)
    fun insert(user: User)


    @Query("DELETE FROM user_table")
    fun deleteAllUsers()


    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): User

    @Query("DELETE FROM metric_table")
    fun deleteAllMetrics()


    @Query("SELECT * FROM metric_table LIMIT 1")
    fun getMetric(): ResponseMetric

    @Insert(onConflict = REPLACE)
    fun insert(responseMetric: ResponseMetric)

    @Insert(onConflict = REPLACE)
    fun insert(answer: AnswersResponse)

    @Query("SELECT * FROM answers")
    fun getAnswers(): List<AnswersResponse>

    @Insert(onConflict = REPLACE)
    fun insert(questions: List<Question>)

    @Query("SELECT * FROM questions WHERE type IN(:type)")
    fun getQuestions(vararg type: QuestionType): List<Question>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertHealth(healthResult: HealthResult)

    @Query("SELECT * FROM health_table ORDER BY date DESC LIMIT 1")
    fun getLastHealthResult(): LiveData<HealthResult>
}
