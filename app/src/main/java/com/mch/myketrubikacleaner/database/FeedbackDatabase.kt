package com.mch.myketrubikacleaner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Feedback::class], version = 1, exportSchema = false)
abstract class FeedbackDatabase : RoomDatabase() {

    abstract fun feedbackDao(): FeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: FeedbackDatabase? = null

        fun getDatabase(context: Context): FeedbackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FeedbackDatabase::class.java,
                    "feedback_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}