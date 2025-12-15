package com.gemini.music.data.repository

import com.gemini.music.data.database.ScrobbleDao
import com.gemini.music.data.database.ScrobbleEntity
import com.gemini.music.domain.model.ScrobbleEntry
import com.gemini.music.domain.model.ScrobbleStatus
import com.gemini.music.domain.repository.ScrobbleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScrobbleRepositoryImpl @Inject constructor(
    private val scrobbleDao: ScrobbleDao
) : ScrobbleRepository {
    
    // TODO: 整合 Last.fm API 後設為 true
    private val _isExternalServiceConnected = MutableStateFlow(false)
    
    override fun getScrobbles(): Flow<List<ScrobbleEntry>> {
        return scrobbleDao.getAllScrobbles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPendingScrobbles(): Flow<List<ScrobbleEntry>> {
        return scrobbleDao.getPendingScrobbles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun recordScrobble(entry: ScrobbleEntry) {
        withContext(Dispatchers.IO) {
            scrobbleDao.insert(entry.toEntity())
        }
    }
    
    override suspend fun updateScrobbleStatus(ids: List<Long>, status: ScrobbleStatus) {
        withContext(Dispatchers.IO) {
            val scrobbledAt = if (status == ScrobbleStatus.SCROBBLED) {
                System.currentTimeMillis()
            } else null
            scrobbleDao.updateStatus(ids, status.name, scrobbledAt)
        }
    }
    
    override suspend fun getScrobbleCount(startTime: Long, endTime: Long): Int {
        return withContext(Dispatchers.IO) {
            scrobbleDao.getScrobbleCount(startTime, endTime)
        }
    }
    
    override fun getTopScrobbledArtists(limit: Int): Flow<List<Pair<String, Int>>> {
        return scrobbleDao.getTopArtists(limit).map { list ->
            list.map { it.artist to it.count }
        }
    }
    
    override fun getTopScrobbledSongs(limit: Int): Flow<List<Pair<Long, Int>>> {
        return scrobbleDao.getTopSongs(limit).map { list ->
            list.map { it.songId to it.count }
        }
    }
    
    override suspend fun cleanupOldScrobbles(beforeTimestamp: Long) {
        withContext(Dispatchers.IO) {
            scrobbleDao.deleteOldScrobbles(beforeTimestamp)
        }
    }
    
    override suspend fun syncToExternalService(): Int {
        // TODO: 實作 Last.fm API 整合
        // 目前返回 0，表示無外部服務連接
        return 0
    }
    
    override fun isExternalServiceConnected(): Flow<Boolean> {
        return _isExternalServiceConnected
    }
    
    // === Mapping Functions ===
    
    private fun ScrobbleEntity.toDomain(): ScrobbleEntry {
        return ScrobbleEntry(
            id = id,
            songId = songId,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            timestamp = timestamp,
            scrobbledAt = scrobbledAt,
            status = ScrobbleStatus.valueOf(status)
        )
    }
    
    private fun ScrobbleEntry.toEntity(): ScrobbleEntity {
        return ScrobbleEntity(
            id = id,
            songId = songId,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            timestamp = timestamp,
            scrobbledAt = scrobbledAt,
            status = status.name
        )
    }
}
