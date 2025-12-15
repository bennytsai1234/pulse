package com.gemini.music.domain.model

/**
 * 代表一次 Scrobble 記錄。
 * 當歌曲播放超過一定時長（通常是 50%）時，會創建一條 Scrobble 記錄。
 */
data class ScrobbleEntry(
    val id: Long = 0,
    val songId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val timestamp: Long, // Unix timestamp in milliseconds
    val scrobbledAt: Long? = null, // When it was synced to external service
    val status: ScrobbleStatus = ScrobbleStatus.PENDING
)

enum class ScrobbleStatus {
    PENDING,     // 等待上傳
    SCROBBLED,   // 已同步到外部服務
    FAILED,      // 上傳失敗
    LOCAL_ONLY   // 僅本地記錄
}
