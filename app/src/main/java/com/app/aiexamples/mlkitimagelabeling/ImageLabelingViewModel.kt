package com.app.aiexamples.mlkitimagelabeling

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class ImageLabelingViewModel : ViewModel() {

    private val _state = MutableStateFlow<ImageLabelingState>(ImageLabelingState.Idle)
    val state: StateFlow<ImageLabelingState> = _state.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    // Minimum confidence threshold — labels below this are ignored
    private val _confidenceThreshold = MutableStateFlow(0.65f)
    val confidenceThreshold: StateFlow<Float> = _confidenceThreshold.asStateFlow()

    fun onLabelsDetected(labels: List<ImageLabel>) {
        val filtered = labels.filter { it.confidence >= _confidenceThreshold.value }.sortedByDescending { it.confidence }  // highest confidence first
            .take(8)                                // max 8 labels

        _state.value = if (filtered.isEmpty()) {
            ImageLabelingState.NoLabel
        } else {
            ImageLabelingState.LabelsDetected(filtered)
        }
    }

    fun setConfidenceThreshold(value: Float) {
        _confidenceThreshold.value = value
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    // Map label text to a relevant emoji
    fun getEmojiForLabel(label: String): String {
        val lower = label.lowercase()
        return when {
            lower.contains("person") || lower.contains("human") || lower.contains("face") -> "👤"
            lower.contains("dog") || lower.contains("puppy") -> "🐶"
            lower.contains("cat") || lower.contains("kitten") -> "🐱"
            lower.contains("car") || lower.contains("vehicle") -> "🚗"
            lower.contains("phone") || lower.contains("mobile") -> "📱"
            lower.contains("food") || lower.contains("meal") -> "🍽️"
            lower.contains("tree") || lower.contains("plant") -> "🌳"
            lower.contains("flower") -> "🌸"
            lower.contains("water") || lower.contains("ocean") -> "🌊"
            lower.contains("sky") || lower.contains("cloud") -> "☁️"
            lower.contains("building") || lower.contains("house") -> "🏠"
            lower.contains("book") -> "📚"
            lower.contains("computer") || lower.contains("laptop") -> "💻"
            lower.contains("chair") || lower.contains("furniture") -> "🪑"
            lower.contains("table") || lower.contains("desk") -> "🪞"
            lower.contains("sport") || lower.contains("ball") -> "⚽"
            lower.contains("bird") -> "🐦"
            lower.contains("fish") -> "🐟"
            lower.contains("mountain") -> "⛰️"
            lower.contains("road") || lower.contains("street") -> "🛣️"
            lower.contains("fire") -> "🔥"
            lower.contains("sun") || lower.contains("light") -> "☀️"
            lower.contains("night") || lower.contains("dark") -> "🌙"
            lower.contains("music") || lower.contains("instrument") -> "🎵"
            lower.contains("art") || lower.contains("paint") -> "🎨"
            lower.contains("clothes") || lower.contains("shirt") -> "👕"
            lower.contains("fruit") -> "🍎"
            lower.contains("vegetable") -> "🥦"
            lower.contains("drink") || lower.contains("bottle") -> "🥤"
            lower.contains("money") || lower.contains("cash") -> "💰"
            else -> "🏷️"
        }
    }
}

data class ImageLabel(val text: String, val confidence: Float,  // 0.0 to 1.0
                      val emoji: String/*visual representation*/)

sealed class ImageLabelingState {
    object Idle : ImageLabelingState()
    object NoLabel : ImageLabelingState()
    data class LabelsDetected(val labels: List<ImageLabel>) : ImageLabelingState()
}