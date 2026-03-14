package com.app.aiexamples.mlkittextrecognition

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TextRecognitionViewModel : ViewModel() {

    private val _state = MutableStateFlow<TextRecognitionState>(TextRecognitionState.Idle)
    val state: StateFlow<TextRecognitionState> = _state.asStateFlow()

    private val _isFrozen = MutableStateFlow(false)
    val isFrozen: StateFlow<Boolean> = _isFrozen.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _copiedSuccess = MutableStateFlow(false)
    val copiedSuccess: StateFlow<Boolean> = _copiedSuccess.asStateFlow()

    fun onTextDetected(fullText: String, blocks: List<String>) {
        // Don't update if frozen — user is reading result
        if (_isFrozen.value) return

        _state.value = if (fullText.isBlank()) {
            TextRecognitionState.NoText
        } else {
            TextRecognitionState.TextDetected(fullText, blocks)
        }
    }

    fun startScanning() {
        _isFrozen.value = false
        _state.value = TextRecognitionState.Scanning
    }

    // Freeze — stop updating result so user can read
    fun freeze() {
        if (_state.value is TextRecognitionState.TextDetected) {
            _isFrozen.value = true
        }
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Text", text))
        _copiedSuccess.value = true
    }

    fun resetCopied() {
        _copiedSuccess.value = false
    }
}

sealed class TextRecognitionState {
    object Idle : TextRecognitionState()
    object Scanning : TextRecognitionState()
    object NoText : TextRecognitionState()
    data class TextDetected(val fullText: String, val blocks: List<String>/* each paragraph as separate block */) : TextRecognitionState()
}