package com.gemini.music.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gemini.music.data.database.ScrobbleDao
import com.gemini.music.data.database.ScrobbleEntity
import com.gemini.music.data.source.LastFmService
import com.gemini.music.domain.model.ScrobbleEntry
import com.gemini.music.domain.model.ScrobbleStatus
import com.gemini.music.domain.repository.ScrobbleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.lastFmDataStore by preferencesDataStore(name = "lastfm_prefs")

@Singleton
class ScrobbleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scrobbleDao: ScrobbleDao,
    private val lastFmService: LastFmService
) : ScrobbleRepository {
    
    companion object {
        private val KEY_SESSION = stringPreferencesKey("lastfm_session_key")
        private val KEY_USERNAME = stringPreferencesKey("lastfm_username")
    }
    
    private val _isExternalServiceConnected = MutableStateFlow(false)
    
    init {
        // Check if session exists on init
        CoroutineScope(Dispatchers.IO).launch {
            val session = getStoredSessionKey()
            _isExternalServiceConnected.value = session != null
        }
    }
    
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
            
            // 如果已連接 Last.fm，嘗試立即 scrobble
            val sessionKey = getStoredSessionKey()
            if (sessionKey != null) {
                val success = lastFmService.scrobble(
                    sessionKey = sessionKey,
                    artist = entry.artist,
                    track = entry.title,
                    album = entry.album,
                    timestamp = entry.timestamp / 1000 // Last.fm uses seconds
                )
                if (success) {
                    scrobbleDao.updateStatus(
                        listOf(entry.id),
                        ScrobbleStatus.SCROBBLED.name,
                        System.currentTimeMillis()
                    )
                }
            }
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
        return withContext(Dispatchers.IO) {
            val sessionKey = getStoredSessionKey() ?: return@withContext 0
            
            val pending = scrobbleDao.getPendingScrobblesSync()
            var successCount = 0
            
            for (entry in pending) {
                val success = lastFmService.scrobble(
                    sessionKey = sessionKey,
                    artist = entry.artist,
                    track = entry.title,
                    album = entry.album,
                    timestamp = entry.timestamp / 1000
                )
                if (success) {
                    scrobbleDao.updateStatus(
                        listOf(entry.id),
                        ScrobbleStatus.SCROBBLED.name,
                        System.currentTimeMillis()
                    )
                    successCount++
                }
            }
            
            successCount
        }
    }
    
    override fun isExternalServiceConnected(): Flow<Boolean> {
        return _isExternalServiceConnected
    }
    
    // === Last.fm Authentication ===
    
    suspend fun getAuthToken(): String? {
        return lastFmService.getAuthToken()
    }
    
    fun getAuthUrl(token: String): String {
        return lastFmService.getAuthUrl(token)
    }
    
    suspend fun completeAuthentication(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            val session = lastFmService.getSession(token) ?: return@withContext false
            
            context.lastFmDataStore.edit { prefs ->
                prefs[KEY_SESSION] = session.key
                prefs[KEY_USERNAME] = session.name
            }
            
            _isExternalServiceConnected.value = true
            true
        }
    }
    
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            context.lastFmDataStore.edit { prefs ->
                prefs.remove(KEY_SESSION)
                prefs.remove(KEY_USERNAME)
            }
            _isExternalServiceConnected.value = false
        }
    }
    
    suspend fun getUsername(): String? {
        return context.lastFmDataStore.data.first()[KEY_USERNAME]
    }
    
    private suspend fun getStoredSessionKey(): String? {
        return context.lastFmDataStore.data.first()[KEY_SESSION]
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

