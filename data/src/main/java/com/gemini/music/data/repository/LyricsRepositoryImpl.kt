package com.gemini.music.data.repository

import com.gemini.music.core.common.parser.LrcParser
import com.gemini.music.data.database.LyricsDao
import com.gemini.music.data.database.LyricsEntity
import com.gemini.music.data.network.LrcLibApi
import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.LyricsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lrcLibApi: LrcLibApi,
    private val lyricsDao: LyricsDao
) : LyricsRepository {

    override suspend fun getLyrics(song: Song): List<LyricLine> = withContext(Dispatchers.IO) {
        // 1. 本地 .lrc 檔案查找 (最高優先級)
        val localLyrics = getLocalLyrics(song.dataPath)
        if (localLyrics.isNotEmpty()) return@withContext localLyrics

        // 2. Room 資料庫快取查找
        val cachedLyrics = lyricsDao.getLyrics(song.id)
        if (cachedLyrics != null) {
            return@withContext LrcParser.parse(cachedLyrics.lyricsContent)
        }

        // 3. 網絡查找
        try {
            val durationSeconds = (song.duration / 1000).toInt()
            // LrcLib API requires duration between 1 and 3600 seconds.
            // If outside this range, pass null to search by text only.
            val validDuration = if (durationSeconds in 1..3600) durationSeconds else null

            val response = lrcLibApi.getLyrics(
                artistName = song.artist,
                trackName = song.title,
                duration = validDuration
            )

            // 優先使用同步歌詞
            val lyricsText = response.syncedLyrics ?: response.plainLyrics
            val isSynced = response.syncedLyrics != null
            
            if (!lyricsText.isNullOrBlank()) {
                // Cache to Room database (always reliable)
                lyricsDao.insertLyrics(
                    LyricsEntity(
                        songId = song.id,
                        lyricsContent = lyricsText,
                        isSynced = isSynced
                    )
                )
                
                // Also try to save to local .lrc file (may fail due to permissions)
                saveLyricsToLocal(song.dataPath, lyricsText)
                
                return@withContext LrcParser.parse(lyricsText)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext emptyList()
    }

    private fun getLocalLyrics(audioPath: String): List<LyricLine> {
        try {
            val audioFile = File(audioPath)
            val parentDir = audioFile.parentFile ?: return emptyList()
            val nameWithoutExtension = audioFile.nameWithoutExtension
            
            val lrcFile = File(parentDir, "$nameWithoutExtension.lrc")
            
            if (lrcFile.exists() && lrcFile.canRead()) {
                val content = lrcFile.readText()
                return LrcParser.parse(content)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }

    private fun saveLyricsToLocal(audioPath: String, lyricsContent: String) {
        try {
            val audioFile = File(audioPath)
            val parentDir = audioFile.parentFile ?: return
            val nameWithoutExtension = audioFile.nameWithoutExtension
            val lrcFile = File(parentDir, "$nameWithoutExtension.lrc")

            if (!lrcFile.exists()) {
                lrcFile.writeText(lyricsContent)
            }
        } catch (e: Exception) {
            // Silently fail - Room cache is the fallback
            e.printStackTrace()
        }
    }
}

