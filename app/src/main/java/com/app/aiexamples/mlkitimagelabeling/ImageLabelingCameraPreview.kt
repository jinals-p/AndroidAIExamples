package com.app.aiexamples.mlkitimagelabeling

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.Executors

@Composable
fun ImageLabelingCameraPreview(modifier: Modifier = Modifier, torchEnabled: Boolean = false, confidenceThreshold: Float = 0.65f, onLabelsDetected: (List<ImageLabel>) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ✅ ML Kit Image Labeler
    val labeler = remember(confidenceThreshold) {
        val options = ImageLabelerOptions.Builder().setConfidenceThreshold(confidenceThreshold).build()
        ImageLabeling.getClient(options)
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            labeler.close()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier, update = { view ->
        startLabelingCamera(context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = view,
            cameraExecutor = cameraExecutor,
            torchEnabled = torchEnabled,
            labeler = labeler,
            onLabelsDetected = onLabelsDetected)
    })
}

private fun startLabelingCamera(context: Context,
                                lifecycleOwner: androidx.lifecycle.LifecycleOwner,
                                previewView: PreviewView,
                                cameraExecutor: java.util.concurrent.ExecutorService,
                                torchEnabled: Boolean,
                                labeler: com.google.mlkit.vision.label.ImageLabeler,
                                onLabelsDetected: (List<ImageLabel>) -> Unit) {
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
                        // ✅ ML Kit labels the image
                        labeler.process(image).addOnSuccessListener { labels ->
                                val imageLabels = labels.map { label ->
                                    ImageLabel(text = label.text, confidence = label.confidence, emoji = "" // filled by ViewModel
                                    )
                                }
                                onLabelsDetected(imageLabels)
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
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            camera.cameraControl.enableTorch(torchEnabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }, ContextCompat.getMainExecutor(context))
}