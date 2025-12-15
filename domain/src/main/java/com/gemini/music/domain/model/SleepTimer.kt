package com.gemini.music.domain.model

/**
 * 睡眠定時器模式
 */
enum class SleepTimerMode {
    OFF,                    // 關閉
    END_OF_TRACK,          // 當前曲結束
    TRACKS,                // 指定曲目數後
    DURATION               // 指定時間後
}

/**
 * 睡眠定時器狀態
 */
data class SleepTimerState(
    val mode: SleepTimerMode = SleepTimerMode.OFF,
    val isActive: Boolean = false,
    val remainingTimeMs: Long = 0,
    val remainingTracks: Int = 0,
    val fadeOut: Boolean = true,
    val fadeDurationSeconds: Int = 30,
    
    // 原始設定 (用於顯示)
    val originalDurationMs: Long = 0,
    val originalTrackCount: Int = 0
) {
    val progress: Float
        get() = when (mode) {
            SleepTimerMode.DURATION -> if (originalDurationMs > 0) {
                1f - (remainingTimeMs.toFloat() / originalDurationMs)
            } else 0f
            SleepTimerMode.TRACKS -> if (originalTrackCount > 0) {
                1f - (remainingTracks.toFloat() / originalTrackCount)
            } else 0f
            else -> 0f
        }
    
    val formattedRemainingTime: String
        get() {
            val totalSeconds = remainingTimeMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    
    companion object {
        val PRESET_DURATIONS = listOf(
            5L * 60 * 1000,   // 5 min
            10L * 60 * 1000,  // 10 min
            15L * 60 * 1000,  // 15 min
            30L * 60 * 1000,  // 30 min
            45L * 60 * 1000,  // 45 min
            60L * 60 * 1000,  // 1 hour
            90L * 60 * 1000,  // 1.5 hours
            120L * 60 * 1000  // 2 hours
        )
        
        val PRESET_TRACKS = listOf(1, 2, 3, 5, 10, 15, 20)
    }
}
