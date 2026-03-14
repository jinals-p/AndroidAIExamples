package com.app.aiexamples.mlkitqrscanner

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(modifier: Modifier = Modifier, torchEnabled: Boolean = false, onBarcodeDetected: (value: String, type: String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera executor — runs on background thread
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // ML Kit barcode scanner instance
    val barcodeScanner = remember { BarcodeScanning.getClient() }

    // PreviewView — bridges CameraX with Compose
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Cleanup when composable leaves screen
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    // AndroidView — embeds traditional Android View inside Compose
    AndroidView(factory = { previewView }, modifier = modifier, update = { view ->
        startCamera(context = context,
            lifecycleOwner = lifecycleOwner,
            previewView = view,
            cameraExecutor = cameraExecutor,
            torchEnabled = torchEnabled,
            barcodeScanner = barcodeScanner,
            onBarcodeDetected = onBarcodeDetected)
    })
}

private fun startCamera(context: Context,
                        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
                        previewView: PreviewView,
                        cameraExecutor: ExecutorService,
                        torchEnabled: Boolean,
                        barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
                        onBarcodeDetected: (value: String, type: String) -> Unit) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        // Preview use case — shows camera feed
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        // Image analysis use case — feeds frames to ML Kit
        val imageAnalyzer = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { analysis ->
                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        // ✅ ML Kit scans every frame
                        barcodeScanner.process(image).addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val value = barcode.rawValue ?: continue
                                    val type = getBarcodeType(barcode.format)
                                    onBarcodeDetected(value, type)
                                }
                            }.addOnCompleteListener {
                                imageProxy.close() // ← always close!
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalyzer)
            // Toggle torch
            camera.cameraControl.enableTorch(torchEnabled)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }, ContextCompat.getMainExecutor(context))
}

// Convert ML Kit format code to readable string
private fun getBarcodeType(format: Int): String {
    return when (format) {
        Barcode.FORMAT_QR_CODE -> "QR Code"
        Barcode.FORMAT_EAN_13 -> "EAN-13"
        Barcode.FORMAT_EAN_8 -> "EAN-8"
        Barcode.FORMAT_CODE_128 -> "Code 128"
        Barcode.FORMAT_CODE_39 -> "Code 39"
        Barcode.FORMAT_UPC_A -> "UPC-A"
        Barcode.FORMAT_UPC_E -> "UPC-E"
        Barcode.FORMAT_PDF417 -> "PDF417"
        Barcode.FORMAT_AZTEC -> "Aztec"
        Barcode.FORMAT_DATA_MATRIX -> "Data Matrix"
        else -> "Barcode"
    }
}