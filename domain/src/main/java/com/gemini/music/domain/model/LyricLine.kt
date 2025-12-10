package com.gemini.music.domain.model

/**
 * 歌詞行數據模型
 * 支援整行時間戳和逐字時間戳（卡拉OK效果）
 */
data class LyricLine(
    val startTime: Long,       // 行開始時間 (毫秒)
    val text: String,          // 完整歌詞文字
    val endTime: Long = 0L,    // 行結束時間 (可選，用於計算動畫)
    val words: List<LyricWord> = emptyList()  // 逐字時間戳 (卡拉OK專用)
) {
    /**
     * 判斷是否有逐字時間資料
     */
    val hasWordTimings: Boolean get() = words.isNotEmpty()
}

/**
 * 單字時間戳（用於卡拉OK效果）
 */
data class LyricWord(
    val text: String,          // 單字或片段
    val startTime: Long,       // 開始時間 (毫秒)
    val endTime: Long          // 結束時間 (毫秒)
) {
    /**
     * 計算在給定時間點的高亮進度 (0.0 ~ 1.0)
     */
    fun getProgress(currentTime: Long): Float {
        if (currentTime < startTime) return 0f
        if (currentTime >= endTime) return 1f
        val duration = (endTime - startTime).toFloat()
        if (duration <= 0) return 1f
        return ((currentTime - startTime) / duration).coerceIn(0f, 1f)
    }
}
