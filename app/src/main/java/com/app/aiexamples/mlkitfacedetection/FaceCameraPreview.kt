package com.app.aiexamples.mlkitfacedetection

import android.content.Context
import android.graphics.PointF
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.app.aiexamples.core.theme.AccentGreen
import com.app.aiexamples.core.theme.AccentPink
import com.app.aiexamples.core.theme.AccentPurple
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

@Composable
fun FaceCameraPreview(modifier: Modifier = Modifier, torchEnabled: Boolean = false, onFacesDetected: (List<DetectedFace>, Int, Int) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ── ML Kit Face Detector — configured with all features ──
    val faceDetector = remember {
        val options = FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // smile + eyes
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)               // face outline
            .enableTracking()                                                    // assigns face ID
            .build()
        FaceDetection.getClient(options)
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }

    Box(modifier = modifier) {

        // ── Camera Preview ────────────────────────────────
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize(), update = { view ->
            startFaceCamera(context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = view,
                cameraExecutor = cameraExecutor,
                torchEnabled = torchEnabled,
                faceDetector = faceDetector,
                onFacesDetected = onFacesDetected)
        })
    }
}

// ── Face Overlay — draws on top of camera ────────────────
@Composable
fun FaceOverlay(state: FaceDetectionState, modifier: Modifier = Modifier) {
    if (state !is FaceDetectionState.FacesDetected) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // ✅ Image comes rotated 90° from analyzer
        // so actual image width = imageHeight and vice versa
        val imageWidth = state.imageHeight.toFloat()
        val imageHeight = state.imageWidth.toFloat()

        val scaleX = canvasWidth / imageWidth
        val scaleY = canvasHeight / imageHeight

        state.faces.forEach { face ->
            val box = face.boundingBox

            // ✅ Mirror X for front camera
            val mirroredLeft = imageWidth - box.right
            val mirroredRight = imageWidth - box.left

            val left = mirroredLeft * scaleX
            val top = box.top * scaleY
            val width = (mirroredRight - mirroredLeft) * scaleX
            val height = box.height() * scaleY

            val boxColor = when {
                (face.smilingProbability ?: 0f) > 0.7f -> AccentGreen
                else -> AccentPurple
            }

            // ── Bounding box ──────────────────────────────
            drawRect(color = boxColor, topLeft = Offset(left, top), size = Size(width, height), style = Stroke(width = 3f))

            // ── Corner accents ────────────────────────────
            val cornerLen = width * 0.15f

            // Top-left
            drawLine(boxColor, Offset(left, top + cornerLen), Offset(left, top), 6f)
            drawLine(boxColor, Offset(left, top), Offset(left + cornerLen, top), 6f)
            // Top-right
            drawLine(boxColor, Offset(left + width - cornerLen, top), Offset(left + width, top), 6f)
            drawLine(boxColor, Offset(left + width, top), Offset(left + width, top + cornerLen), 6f)
            // Bottom-left
            drawLine(boxColor, Offset(left, top + height - cornerLen), Offset(left, top + height), 6f)
            drawLine(boxColor, Offset(left, top + height), Offset(left + cornerLen, top + height), 6f)
            // Bottom-right
            drawLine(boxColor, Offset(left + width - cornerLen, top + height), Offset(left + width, top + height), 6f)
            drawLine(boxColor, Offset(left + width, top + height - cornerLen), Offset(left + width, top + height), 6f)

            // ── Contour points ────────────────────────────
            face.contours.forEach { point ->
                // ✅ Mirror contour points too
                val mirroredX = (imageWidth - point.x) * scaleX
                val mirroredY = point.y * scaleY
                drawCircle(color = AccentPink.copy(alpha = 0.8f), radius = 3f, center = Offset(mirroredX, mirroredY))
            }
        }
    }
}

private fun startFaceCamera(context: Context,
                            lifecycleOwner: androidx.lifecycle.LifecycleOwner,
                            previewView: PreviewView,
                            cameraExecutor: java.util.concurrent.ExecutorService,
                            torchEnabled: Boolean,
                            faceDetector: com.google.mlkit.vision.face.FaceDetector,
                            onFacesDetected: (List<DetectedFace>, Int, Int) -> Unit) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { analysis ->
            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    val imageWidth = imageProxy.width
                    val imageHeight = imageProxy.height

                    faceDetector.process(image).addOnSuccessListener { faces ->
                        val detectedFaces = faces.map { face ->
                            face.toDetectedFace()
                        }
                        onFacesDetected(detectedFaces, imageWidth, imageHeight)
                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
                } else {
                    imageProxy.close()
                }
            }
        }

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, // ✅ front camera for face detection
                preview, imageAnalyzer)
            camera.cameraControl.enableTorch(torchEnabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }, ContextCompat.getMainExecutor(context))
}

// ── Extension — convert ML Kit Face to our DetectedFace ──
private fun Face.toDetectedFace(): DetectedFace {
    // Get all contour points flattened into one list
    val allContourPoints = mutableListOf<PointF>()
    listOf(FaceContour.FACE,
        FaceContour.LEFT_EYEBROW_TOP,
        FaceContour.LEFT_EYEBROW_BOTTOM,
        FaceContour.RIGHT_EYEBROW_TOP,
        FaceContour.RIGHT_EYEBROW_BOTTOM,
        FaceContour.LEFT_EYE,
        FaceContour.RIGHT_EYE,
        FaceContour.UPPER_LIP_TOP,
        FaceContour.UPPER_LIP_BOTTOM,
        FaceContour.LOWER_LIP_TOP,
        FaceContour.LOWER_LIP_BOTTOM,
        FaceContour.NOSE_BRIDGE,
        FaceContour.NOSE_BOTTOM,
        FaceContour.LEFT_CHEEK,
        FaceContour.RIGHT_CHEEK).forEach { contourType ->
        getContour(contourType)?.points?.let {
            allContourPoints.addAll(it)
        }
    }

    return DetectedFace(id = trackingId ?: 0,
        boundingBox = boundingBox,
        smilingProbability = smilingProbability,
        leftEyeOpenProbability = leftEyeOpenProbability,
        rightEyeOpenProbability = rightEyeOpenProbability,
        headEulerAngleX = headEulerAngleX,
        headEulerAngleY = headEulerAngleY,
        headEulerAngleZ = headEulerAngleZ,
        contours = allContourPoints)
}