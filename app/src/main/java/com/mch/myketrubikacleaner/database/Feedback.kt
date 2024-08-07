package com.mch.myketrubikacleaner.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback_table")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String
)