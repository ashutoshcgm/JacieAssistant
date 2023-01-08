package com.example.jacie.assistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.jacie.data.Assistant
import com.example.jacie.data.AssistantDao
import kotlinx.coroutines.*


class AssistantViewModel(private val database: AssistantDao,application: Application):AndroidViewModel(application) {
    private var viewModeJob= Job()
    override fun onCleared() {
        super.onCleared()
        viewModeJob.cancel()
    }
    private var uiScope= CoroutineScope(Dispatchers.Main+viewModeJob)
    private var currentMessage=MutableLiveData<Assistant?>()
    val messages=database.getAllMessages()
    init {
        initializeCurrentMessage()
    }

    private fun initializeCurrentMessage() {
        uiScope.launch {
            currentMessage.value=getCurrentMessageFromDatabase()
        }
    }

    private suspend fun getCurrentMessageFromDatabase(): Assistant? {
        return withContext(Dispatchers.IO){
            var message = database.getCurrentMessage()
            if (message?.assistant_message=="DEFAULT_MESSAGE" || message?.human_message=="DEFAULT_MESSAGE"){
                message=null
            }
            message
        }
    }
    fun sendMessageToDatabase(assistantMessage:String, humanMessage:String){
        uiScope.launch {
            var newAssistant=Assistant()
            newAssistant.assistant_message=assistantMessage
            newAssistant.human_message=humanMessage
            insert(newAssistant)
            currentMessage.value=getCurrentMessageFromDatabase()
        }
    }

    suspend fun insert(message:Assistant){
        withContext(Dispatchers.IO){
            database.insert(message)
        }
    }
    suspend fun update(message: Assistant){
        withContext(Dispatchers.IO){
            database.update(message)
        }
    }
    fun onClear(){
        uiScope.launch {
            clear()
            currentMessage.value=null
        }
    }
    private suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clear()
        }
    }

}