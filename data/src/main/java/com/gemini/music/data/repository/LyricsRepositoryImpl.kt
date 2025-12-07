package com.gemini.music.data.repository

import com.gemini.music.core.common.parser.LrcParser
import com.gemini.music.domain.model.LyricLine
import com.gemini.music.domain.repository.LyricsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor() : LyricsRepository {

    override suspend fun getLyrics(audioPath: String): List<LyricLine> = withContext(Dispatchers.IO) {
        try {
            val audioFile = File(audioPath)
            val parentDir = audioFile.parentFile ?: return@withContext emptyList()
            val nameWithoutExtension = audioFile.nameWithoutExtension
            
            // 嘗試尋找 .lrc 檔案
            val lrcFile = File(parentDir, "$nameWithoutExtension.lrc")
            
            if (lrcFile.exists() && lrcFile.canRead()) {
                val content = lrcFile.readText()
                return@withContext LrcParser.parse(content)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }
}
