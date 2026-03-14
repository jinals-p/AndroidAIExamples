package com.app.aiexamples.mlkitqrscanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel : ViewModel() {

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    // Called when ML Kit detects a barcode
    fun onBarcodeDetected(value: String, type: String) {
        // Ignore if already scanned same value
        if (_scannerState.value is ScannerState.Scanned && (_scannerState.value as ScannerState.Scanned).value == value) return

        _scannerState.value = ScannerState.Scanned(value, type)
    }

    fun startScanning() {
        _scannerState.value = ScannerState.Scanning
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Scanning  // back to scanning
    }

    fun toggleTorch() {
        _torchEnabled.value = !_torchEnabled.value
    }
}

// All possible scanner states

sealed class ScannerState {
    object Idle : ScannerState()           // nothing scanned yet
    object Scanning : ScannerState()       // camera active, waiting
    data class Scanned(val value: String, val type: String) : ScannerState()// result received
}
