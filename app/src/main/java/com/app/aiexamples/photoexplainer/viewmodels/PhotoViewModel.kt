package com.app.aiexamples.photoexplainer.viewmodels// PhotoViewModel.kt
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.aiexamples.photoexplainer.helper.GeminiHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PhotoViewModel : ViewModel() {

    private val gemini = GeminiHelper()

    // UI states as flows
    private val _analyzeState = MutableStateFlow<AnalyzeState>(AnalyzeState.Idle)
    val analyzeState: StateFlow<AnalyzeState> = _analyzeState

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap

    fun setImage(bitmap: Bitmap) {
        _selectedBitmap.value = bitmap
        _analyzeState.value = AnalyzeState.Idle  // reset state on new image
    }

    fun analyzeImage(prompt: String) {
        val bitmap = _selectedBitmap.value ?: return

        viewModelScope.launch {
            _analyzeState.value = AnalyzeState.Loading

            val result = gemini.analyzeImage(bitmap, prompt)

            _analyzeState.value = if (result.startsWith("Error")) {
                AnalyzeState.Error(result)
            } else {
                AnalyzeState.Success(result)
            }
        }
    }
}

// Sealed class — represents every possible UI state
sealed class AnalyzeState {
    object Idle : AnalyzeState()        // nothing happened yet
    object Loading : AnalyzeState()     // waiting for Gemini
    data class Success(val result: String) : AnalyzeState()  // got response
    data class Error(val message: String) : AnalyzeState()   // something failed
}
