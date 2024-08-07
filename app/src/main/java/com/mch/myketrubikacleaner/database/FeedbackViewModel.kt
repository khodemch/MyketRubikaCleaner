package com.mch.myketrubikacleaner.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.mch.myketrubikacleaner.repository.FeedbackRepository
import kotlinx.coroutines.launch

class FeedbackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FeedbackRepository = FeedbackRepository(application)
    val allFeedback: LiveData<List<Feedback>> = repository.allFeedback

    fun insert(feedback: Feedback) {
        viewModelScope.launch {
            repository.insert(feedback)
        }
    }
}