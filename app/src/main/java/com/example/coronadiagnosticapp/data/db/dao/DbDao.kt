package com.example.coronadiagnosticapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.coronadiagnosticapp.data.db.entity.*
import com.example.coronadiagnosticapp.data.db.entity.question.*
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(answer: AnswersResponse)

    @Query("SELECT * FROM answers")
    fun getAnswers(): List<AnswersResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnswers(answers: List<AnswersResponse>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSelectQuestion(question: SelectQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCheckboxQuestion(question: CheckBoxQuestion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTextQuestion(question: TextQuestion)

    fun insertQuestions(questions: List<Question>) = questions.forEach {
        when (it) {
            is CheckBoxQuestion -> insertCheckboxQuestion(it)
            is TextQuestion -> insertTextQuestion(it)
            is SelectQuestion ->insertSelectQuestion(it)
        }
    }

    fun getQuestions(type: QuestionType): List<Question> = when (type) {
        QuestionType.CHECKBOX -> getCheckboxQuestions()
        QuestionType.TEXT -> getTextQuestions()
        QuestionType.MULTI_SELECT,
        QuestionType.SELECT -> getSelectQuestions()
    }

    @Query("SELECT * FROM check_box_questions")
    fun getCheckboxQuestions(): List<CheckBoxQuestion>

    @Query("SELECT * FROM text_questions")
    fun getTextQuestions(): List<TextQuestion>

    @Query("SELECT * FROM select_questions")
    fun getSelectQuestions(): List<SelectQuestion>


    fun getSimpleQuestions(): List<Question>{
        return getTextQuestions() + getCheckboxQuestions()
    }


}
