package com.app.aiexamples.mlkitfacedetection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FaceDetectionViewModel : ViewModel() {

    private val _state = MutableStateFlow<FaceDetectionState>(FaceDetectionState.Idle)
    val state: StateFlow<FaceDetectionState> = _state.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    // Called every frame from camera analyzer
    fun onFacesDetected(faces: List<DetectedFace>, imageWidth: Int, imageHeight: Int) {
        _state.value = if (faces.isEmpty()) {
            FaceDetectionState.NoFace
        } else {
            FaceDetectionState.FacesDetected(faces, imageWidth, imageHeight)
        }
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }

    // Helper — human readable emotion from smile probability
    fun getEmotion(smilingProb: Float?): String {
        return when {
            smilingProb == null -> "Unknown"
            smilingProb > 0.7f -> "😊 Smiling"
            smilingProb > 0.3f -> "🙂 Neutral"
            else -> "😐 Not Smiling"
        }
    }

    // Helper — eye state from probability
    fun getEyeState(prob: Float?): String {
        return when {
            prob == null -> "Unknown"
            prob > 0.7f -> "Open"
            prob > 0.3f -> "Half Open"
            else -> "Closed"
        }
    }

    // Helper — head direction from euler angles
    fun getHeadDirection(angleY: Float): String {
        return when {
            angleY > 20f -> "Looking Left"   // ← swapped
            angleY < -20f -> "Looking Right"  // ← swapped
            else -> "Looking Forward"
        }
    }
}// Holds all info about a single detected face

data class DetectedFace(val id: Int,
                        val boundingBox: android.graphics.Rect,
                        val smilingProbability: Float?,
                        val leftEyeOpenProbability: Float?,
                        val rightEyeOpenProbability: Float?,
                        val headEulerAngleX: Float,  // up/down tilt
                        val headEulerAngleY: Float,  // left/right turn
                        val headEulerAngleZ: Float,  // sideways tilt
                        val contours: List<android.graphics.PointF>) // face outline points)

sealed class FaceDetectionState {
    object Idle : FaceDetectionState()
    object NoFace : FaceDetectionState()        // camera on, no face found
    data class FacesDetected(val faces: List<DetectedFace>, val imageWidth: Int, val imageHeight: Int) : FaceDetectionState()
}
