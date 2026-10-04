package com.example.ui.consultant

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.repository.NavigatorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConsultantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NavigatorRepository(application)

    val messages: StateFlow<List<ChatMessageEntity>> = repository.allMessagesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    fun updateInputText(newText: String) {
        _inputText.value = newText
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun clearSelectedImage() {
        _selectedImageUri.value = null
    }

    fun clearErrorBanner() {
        _errorBanner.value = null
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val imageUri = _selectedImageUri.value

        if (text.isEmpty() && imageUri == null) return

        val promptToSend = if (text.isEmpty()) {
            "Проведи глубокий аудит этого графика/отчёта по стандарту AI Navigator (краткий вывод, анализ, риски, сценарии, следующие шаги)."
        } else {
            text
        }

        val imageUriString = imageUri?.toString()

        _inputText.value = ""
        _selectedImageUri.value = null
        _isLoading.value = true
        _errorBanner.value = null

        viewModelScope.launch {
            try {
                // Save user message to Room
                repository.saveUserMessage(promptToSend, imageUriString)

                // Current message history
                val currentHistory = messages.value

                // Call AI Navigator
                val responseText = repository.askNavigator(
                    userPrompt = promptToSend,
                    imageUri = imageUriString,
                    history = currentHistory
                )

                // Save Navigator response to Room
                repository.saveNavigatorMessage(responseText)
            } catch (e: Exception) {
                _errorBanner.value = "Ошибка генерации ответа: ${e.localizedMessage ?: "проверь подключение"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendQuickPrompt(promptText: String) {
        _inputText.value = promptText
        sendMessage()
    }

    fun toggleBookmark(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(id, currentStatus)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }
}
