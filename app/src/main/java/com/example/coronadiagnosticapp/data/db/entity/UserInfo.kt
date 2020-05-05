package com.example.coronadiagnosticapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "user_info_table")
data class UserInfo(
    var age: Int?,
    var sex: Sex?,
    var weight: Int?,
    var height: Int?,
    var smokingStatus: String?,
    var backgroundDiseases: List<String>
) {
    @PrimaryKey(autoGenerate = true)
    @Expose(serialize = false, deserialize = false)
    var id: Long = -1
}