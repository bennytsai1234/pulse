package com.gemini.music.data.source

import android.content.Context
import android.media.MediaScannerConnection
import com.gemini.music.domain.model.SongTags
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.inject.Inject

/**
 * 負責讀取和寫入音樂檔案的 ID3 標籤。
 * 使用 JAudioTagger 庫支援多種音頻格式。
 */
class TagEditorSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    init {
        // Suppress JAudioTagger's verbose logging
        Logger.getLogger("org.jaudiotagger").level = Level.OFF
    }
    
    /**
     * 從音樂檔案讀取標籤資訊
     */
    suspend fun readTags(filePath: String, songId: Long): SongTags? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists() || !file.canRead()) return@withContext null
            
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag ?: audioFile.createDefaultTag()
            
            SongTags(
                songId = songId,
                filePath = filePath,
                title = tag.getFirst(FieldKey.TITLE) ?: "",
                artist = tag.getFirst(FieldKey.ARTIST) ?: "",
                album = tag.getFirst(FieldKey.ALBUM) ?: "",
                albumArtist = tag.getFirst(FieldKey.ALBUM_ARTIST) ?: "",
                genre = tag.getFirst(FieldKey.GENRE) ?: "",
                year = tag.getFirst(FieldKey.YEAR) ?: "",
                trackNumber = tag.getFirst(FieldKey.TRACK) ?: "",
                discNumber = tag.getFirst(FieldKey.DISC_NO) ?: "",
                comment = tag.getFirst(FieldKey.COMMENT) ?: ""
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 將標籤資訊寫入音樂檔案
     */
    suspend fun writeTags(tags: SongTags): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(tags.filePath)
            if (!file.exists() || !file.canWrite()) return@withContext false
            
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tagOrCreateAndSetDefault
            
            // Update all fields
            tag.setField(FieldKey.TITLE, tags.title)
            tag.setField(FieldKey.ARTIST, tags.artist)
            tag.setField(FieldKey.ALBUM, tags.album)
            tag.setField(FieldKey.ALBUM_ARTIST, tags.albumArtist)
            tag.setField(FieldKey.GENRE, tags.genre)
            tag.setField(FieldKey.YEAR, tags.year)
            tag.setField(FieldKey.TRACK, tags.trackNumber)
            tag.setField(FieldKey.DISC_NO, tags.discNumber)
            tag.setField(FieldKey.COMMENT, tags.comment)
            
            // Commit changes
            audioFile.commit()
            
            // Trigger MediaStore rescan for the file
            MediaScannerConnection.scanFile(
                context,
                arrayOf(tags.filePath),
                null
            ) { _, _ -> }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 將歌詞嵌入音樂檔案（LYRICS 標籤）
     */
    suspend fun embedLyrics(filePath: String, lyricsContent: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists() || !file.canWrite()) return@withContext false
            
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tagOrCreateAndSetDefault
            
            // 嵌入歌詞到 LYRICS 欄位
            tag.setField(FieldKey.LYRICS, lyricsContent)
            
            // Commit changes
            audioFile.commit()
            
            // Trigger MediaStore rescan
            MediaScannerConnection.scanFile(
                context,
                arrayOf(filePath),
                null
            ) { _, _ -> }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 從音樂檔案提取嵌入的歌詞
     */
    suspend fun extractLyrics(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists() || !file.canRead()) return@withContext null
            
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag ?: return@withContext null
            
            val lyrics = tag.getFirst(FieldKey.LYRICS)
            if (lyrics.isNullOrBlank()) null else lyrics
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
