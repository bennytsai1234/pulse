package com.gemini.music.domain.repository

import com.gemini.music.domain.model.ScrobbleEntry
import com.gemini.music.domain.model.ScrobbleStatus
import kotlinx.coroutines.flow.Flow

/**
 * Scrobbling 資料倉庫介面。
 * 負責管理 Scrobble 記錄的儲存與同步。
 */
interface ScrobbleRepository {
    
    /**
     * 觀察所有 Scrobble 記錄。
     */
    fun getScrobbles(): Flow<List<ScrobbleEntry>>
    
    /**
     * 觀察待同步的 Scrobble 記錄。
     */
    fun getPendingScrobbles(): Flow<List<ScrobbleEntry>>
    
    /**
     * 記錄一次 Scrobble。
     */
    suspend fun recordScrobble(entry: ScrobbleEntry)
    
    /**
     * 批量更新 Scrobble 狀態。
     */
    suspend fun updateScrobbleStatus(ids: List<Long>, status: ScrobbleStatus)
    
    /**
     * 取得指定時間範圍內的 Scrobble 統計。
     */
    suspend fun getScrobbleCount(startTime: Long, endTime: Long): Int
    
    /**
     * 取得最常 Scrobble 的藝人。
     */
    fun getTopScrobbledArtists(limit: Int = 10): Flow<List<Pair<String, Int>>>
    
    /**
     * 取得最常 Scrobble 的歌曲。
     */
    fun getTopScrobbledSongs(limit: Int = 10): Flow<List<Pair<Long, Int>>>
    
    /**
     * 清除舊的 Scrobble 記錄。
     */
    suspend fun cleanupOldScrobbles(beforeTimestamp: Long)
    
    /**
     * 同步 Scrobbles 到外部服務 (如 Last.fm)。
     * @return 成功同步的數量
     */
    suspend fun syncToExternalService(): Int
    
    /**
     * 檢查是否已連接外部 Scrobbling 服務。
     */
    fun isExternalServiceConnected(): Flow<Boolean>
}
