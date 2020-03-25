package com.example.coronadiagnosticapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.coronadiagnosticapp.data.db.dao.UserDao
import com.example.coronadiagnosticapp.data.db.entity.ResponseMetric
import com.example.coronadiagnosticapp.data.db.entity.Token
import com.example.coronadiagnosticapp.data.db.entity.User

@Database(
    entities = [User::class, ResponseMetric::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUserDao(): UserDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "appDatabase.db"
            ).fallbackToDestructiveMigration()
                .build()

    }
}