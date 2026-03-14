package com.app.aiexamples.geminichatapp.viewmodels// PhotoViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.aiexamples.BuildConfig
import com.app.aiexamples.geminichatapp.Message
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    //apiKey = Your Gemini api key
    private val model = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)
    private val chat = model.startChat()

    val analyzeState = MutableStateFlow<AnalyzeState>(AnalyzeState.Idle)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // 1. Add user message
        _messages.value += Message(text = userText, isUser = true)

        // 2. Add empty AI message (loading state)
        val loadingMessage = Message(text = "", isUser = false, isLoading = true)
        _messages.value += loadingMessage

        analyzeState.value = AnalyzeState.Loading

        viewModelScope.launch {
            try {
                var fullResponse = ""

                // 3. Stream response word by word
                chat.sendMessageStream(userText).collect { chunk ->
                    fullResponse += chunk.text ?: ""
                    updateLastAiMessage(fullResponse)  // update as words arrive
                }

            } catch (e: Exception) {
                updateLastAiMessage("Error: ${e.message}")
            }
        }
    }

    private fun updateLastAiMessage(text: String) {
        val currentList = _messages.value.toMutableList()
        val lastAiIndex = currentList.indexOfLast { !it.isUser }
        if (lastAiIndex != -1) {
            currentList[lastAiIndex] = currentList[lastAiIndex].copy(text = text, isLoading = false)
            // Replace entire list — triggers recomposition ✅
            _messages.value = currentList.toList()
        }
        analyzeState.value = AnalyzeState.Idle
    }
}

// Sealed class — represents every possible UI state
sealed class AnalyzeState {
    object Idle : AnalyzeState()        // nothing happened yet
    object Loading : AnalyzeState()     // waiting for Gemini
    data class Success(val result: String) : AnalyzeState()  // got response
    data class Error(val message: String) : AnalyzeState()   // something failed
}
