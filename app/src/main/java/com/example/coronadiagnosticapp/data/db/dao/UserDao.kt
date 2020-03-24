package com.example.coronadiagnosticapp.data.db.dao

import androidx.room.*
import com.example.coronadiagnosticapp.data.db.entity.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.ResponseUser
import com.example.coronadiagnosticapp.data.db.entity.User

@Dao
interface UserDao {
    @Transaction
    fun upsert(user: User) {
        deleteAll()
        insert(user)
    }


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User)


    @Query("DELETE FROM user_table")
    fun deleteAll()


    @Query("SELECT * FROM user_table LIMIT 1")
    fun getUser(): User


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(responseMetric: ResponseMetric)

}
