package com.mch.myketrubikacleaner.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FeedbackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feedback: Feedback) {
        Log.d("FeedbackDao", "Inserting feedback: $feedback")
    }

    @Query("SELECT * FROM feedback_table ORDER BY id ASC")
    fun getAllFeedback(): LiveData<List<Feedback>>


}