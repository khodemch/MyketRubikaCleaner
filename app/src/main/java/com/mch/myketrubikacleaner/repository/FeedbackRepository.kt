package com.mch.myketrubikacleaner.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mch.myketrubikacleaner.database.Feedback
import com.mch.myketrubikacleaner.database.FeedbackDao
import com.mch.myketrubikacleaner.database.FeedbackDatabase

class FeedbackRepository(application: Application) {

    private val feedbackDao: FeedbackDao
    val allFeedback: LiveData<List<Feedback>>

    init {
        val database: FeedbackDatabase = FeedbackDatabase.getDatabase(application)
        feedbackDao = database.feedbackDao()
        allFeedback = feedbackDao.getAllFeedback()
    }

    suspend fun insert(feedback: Feedback) {
        feedbackDao.insert(feedback)
    }
}