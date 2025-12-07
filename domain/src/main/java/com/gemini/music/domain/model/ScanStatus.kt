package com.gemini.music.domain.model

sealed class ScanStatus {
    data object Idle : ScanStatus()
    data class Scanning(val progress: Int, val total: Int, val currentFile: String = "") : ScanStatus()
    data class Completed(val totalAdded: Int) : ScanStatus()
    data class Failed(val error: String) : ScanStatus()
}
