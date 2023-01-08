package com.example.jacie.assistant


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException // Here This can be changed like comments
import com.example.jacie.data.AssistantDao

class AssistantViewModelFactory(
    private val datasource:AssistantDao, private val application: Application
):ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssistantViewModel::class.java)){
            return AssistantViewModel(datasource,application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}