package com.gemini.music.data.repository

import com.gemini.music.data.database.CustomLyricsDao
import com.gemini.music.data.database.CustomLyricsEntity
import com.gemini.music.data.database.SongDao
import com.gemini.music.data.source.TagEditorSource
import com.gemini.music.domain.model.EditableLyricLine
import com.gemini.music.domain.model.LyricsEdit
import com.gemini.music.domain.model.LyricsSource
import com.gemini.music.domain.model.LyricsSyncEvent
import com.gemini.music.domain.repository.LyricsEditRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsEditRepositoryImpl @Inject constructor(
    private val customLyricsDao: CustomLyricsDao,
    private val songDao: SongDao,
    private val tagEditorSource: TagEditorSource
) : LyricsEditRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getEditableLyrics(songId: Long): Flow<LyricsEdit?> {
        return customLyricsDao.getLyricsBySongId(songId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun saveLyrics(lyricsEdit: LyricsEdit) {
        withContext(Dispatchers.IO) {
            customLyricsDao.insert(lyricsEdit.toEntity())
        }
    }
    
    override suspend fun applySyncEvent(songId: Long, event: LyricsSyncEvent) {
        withContext(Dispatchers.IO) {
            val existing = customLyricsDao.getLyricsBySongIdSync(songId) ?: return@withContext
            val lyrics = existing.toDomain()
            
            val updatedLines = when (event) {
                is LyricsSyncEvent.AdjustOffset -> {
                    customLyricsDao.updateOffset(songId, event.offsetMs)
                    return@withContext
                }
                is LyricsSyncEvent.AdjustLineTimestamp -> {
                    lyrics.lines.mapIndexed { index, line ->
                        if (index == event.lineIndex) {
                            line.copy(timestamp = event.newTimestamp)
                        } else line
                    }
                }
                is LyricsSyncEvent.InsertLine -> {
                    val mutableLines = lyrics.lines.toMutableList()
                    mutableLines.add(event.afterIndex + 1, event.line)
                    mutableLines.mapIndexed { index, line -> line.copy(index = index) }
                }
                is LyricsSyncEvent.DeleteLine -> {
                    lyrics.lines.filterIndexed { index, _ -> index != event.index }
                        .mapIndexed { index, line -> line.copy(index = index) }
                }
                is LyricsSyncEvent.UpdateLineText -> {
                    lyrics.lines.mapIndexed { index, line ->
                        if (index == event.index) {
                            line.copy(text = event.newText)
                        } else line
                    }
                }
            }
            
            val updatedLyrics = lyrics.copy(
                lines = updatedLines,
                lastModified = System.currentTimeMillis()
            )
            customLyricsDao.insert(updatedLyrics.toEntity())
        }
    }
    
    override suspend fun adjustGlobalOffset(songId: Long, offsetMs: Long) {
        withContext(Dispatchers.IO) {
            customLyricsDao.updateOffset(songId, offsetMs)
        }
    }
    
    override suspend fun importFromLrc(songId: Long, lrcContent: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val lines = parseLrc(lrcContent)
                if (lines.isEmpty()) return@withContext false
                
                val lyricsEdit = LyricsEdit(
                    songId = songId,
                    lines = lines,
                    offset = 0,
                    source = LyricsSource.IMPORTED,
                    lastModified = System.currentTimeMillis()
                )
                customLyricsDao.insert(lyricsEdit.toEntity())
                true
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override suspend fun exportToLrc(songId: Long): String? {
        return withContext(Dispatchers.IO) {
            val entity = customLyricsDao.getLyricsBySongIdSync(songId) ?: return@withContext null
            val lyrics = entity.toDomain()
            
            buildString {
                lyrics.lines.forEach { line ->
                    val minutes = (line.timestamp / 60000).toInt()
                    val seconds = ((line.timestamp % 60000) / 1000).toInt()
                    val hundredths = ((line.timestamp % 1000) / 10).toInt()
                    appendLine("[${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.${hundredths.toString().padStart(2, '0')}]${line.text}")
                }
            }
        }
    }
    
    override suspend fun embedLyricsToFile(songId: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 獲取歌曲檔案路徑
                val song = songDao.getSongSync(songId) ?: return@withContext false
                val filePath = song.dataPath
                
                // 2. 獲取自訂歌詞
                val lyricsEntity = customLyricsDao.getLyricsBySongIdSync(songId) 
                    ?: return@withContext false
                val lyrics = lyricsEntity.toDomain()
                
                // 3. 生成 LRC 格式歌詞
                val lrcContent = buildString {
                    lyrics.lines.forEach { line ->
                        val minutes = (line.timestamp / 60000).toInt()
                        val seconds = ((line.timestamp % 60000) / 1000).toInt()
                        val hundredths = ((line.timestamp % 1000) / 10).toInt()
                        appendLine("[${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}.${hundredths.toString().padStart(2, '0')}]${line.text}")
                    }
                }
                
                // 4. 嵌入歌詞到檔案
                tagEditorSource.embedLyrics(filePath, lrcContent)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    override suspend fun extractEmbeddedLyrics(songId: Long): LyricsEdit? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 獲取歌曲檔案路徑
                val song = songDao.getSongSync(songId) ?: return@withContext null
                val filePath = song.dataPath
                
                // 2. 從檔案提取歌詞
                val lyricsContent = tagEditorSource.extractLyrics(filePath) 
                    ?: return@withContext null
                
                // 3. 解析歌詞
                val lines = parseLrc(lyricsContent)
                if (lines.isEmpty()) return@withContext null
                
                // 4. 建立 LyricsEdit 物件
                LyricsEdit(
                    songId = songId,
                    lines = lines,
                    offset = 0,
                    source = LyricsSource.EMBEDDED,
                    lastModified = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    override suspend fun deleteLyrics(songId: Long) {
        withContext(Dispatchers.IO) {
            customLyricsDao.delete(songId)
        }
    }
    
    override fun hasCustomLyrics(songId: Long): Flow<Boolean> {
        return customLyricsDao.hasCustomLyrics(songId)
    }
    
    // === LRC Parsing ===
    
    private fun parseLrc(content: String): List<EditableLyricLine> {
        val lines = mutableListOf<EditableLyricLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})](.*)""")
        
        content.lines().forEachIndexed { index, line ->
            regex.find(line)?.let { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0
                val seconds = match.groupValues[2].toLongOrNull() ?: 0
                val hundredths = match.groupValues[3].let {
                    val value = it.toLongOrNull() ?: 0
                    if (it.length == 3) value else value * 10 // Handle both .xx and .xxx formats
                }
                val text = match.groupValues[4]
                
                val timestamp = minutes * 60000 + seconds * 1000 + hundredths
                lines.add(EditableLyricLine(index = lines.size, timestamp = timestamp, text = text))
            }
        }
        
        return lines.sortedBy { it.timestamp }
    }
    
    // === Mapping Functions ===
    
    private fun CustomLyricsEntity.toDomain(): LyricsEdit {
        val linesList: List<LyricLineJson> = try {
            json.decodeFromString(lyricsJson)
        } catch (e: Exception) {
            emptyList()
        }
        
        return LyricsEdit(
            songId = songId,
            lines = linesList.mapIndexed { index, it ->
                EditableLyricLine(
                    index = index,
                    timestamp = it.timestamp,
                    text = it.text,
                    translatedText = it.translatedText
                )
            },
            offset = globalOffset,
            source = LyricsSource.valueOf(source),
            lastModified = lastModified
        )
    }
    
    private fun LyricsEdit.toEntity(): CustomLyricsEntity {
        val linesList = lines.map {
            LyricLineJson(
                timestamp = it.timestamp,
                text = it.text,
                translatedText = it.translatedText
            )
        }
        
        return CustomLyricsEntity(
            songId = songId,
            lyricsJson = json.encodeToString(linesList),
            globalOffset = offset,
            source = source.name,
            lastModified = lastModified
        )
    }
    
    @Serializable
    private data class LyricLineJson(
        val timestamp: Long,
        val text: String,
        val translatedText: String? = null
    )
}
