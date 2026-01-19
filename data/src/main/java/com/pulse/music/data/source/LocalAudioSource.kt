package com.pulse.music.data.source

import android.content.ContentUris
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import com.pulse.music.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * 負責從 Android MediaStore 查詢本地音訊檔案。
 * 支援掃描整臺手機的所有資料夾。
 */
class LocalAudioSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // 支援的音訊 MIME 類型
        private val AUDIO_MIME_TYPES = listOf(
            "audio/mpeg",       // MP3
            "audio/mp4",        // M4A, AAC
            "audio/x-m4a",      // M4A alternative
            "audio/flac",       // FLAC
            "audio/x-flac",     // FLAC alternative
            "audio/ogg",        // OGG
            "audio/opus",       // Opus
            "audio/wav",        // WAV
            "audio/x-wav",      // WAV alternative
            "audio/aac",        // AAC
            "audio/aiff",       // AIFF
            "audio/x-aiff",     // AIFF alternative
            "audio/webm",       // WebM audio
            "audio/*"           // Fallback for other audio types
        )

        // 支援的檔案副檔名（作為備用過濾）
        private val AUDIO_EXTENSIONS = listOf(
            ".mp3", ".m4a", ".flac", ".ogg", ".opus", ".wav",
            ".aac", ".wma", ".aiff", ".aif", ".webm", ".3gp"
        )
    }

    /**
     * 觸發 MediaStore 重新掃描指定路徑或整個外部存儲。
     * 這會讓系統重新索引新添加或修改的媒體檔案。
     */
    suspend fun triggerMediaScan(paths: List<String>? = null) = withContext(Dispatchers.IO) {
        val scanPaths = paths ?: listOf(
            Environment.getExternalStorageDirectory().absolutePath
        )

        MediaScannerConnection.scanFile(
            context,
            scanPaths.toTypedArray(),
            null
        ) { _, _ -> }
    }

    suspend fun loadMusic(
        minDurationMs: Long = 10000L,
        includedPaths: Set<String> = emptySet()
    ): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            // Genre is not directly in Audio.Media in older APIs, but often queryable.
            // However, standard way is querying MediaStore.Audio.Genres table separately or using projection if supported.
            // On newer Android versions, Genre might be tricky.
            // Let's stick to standard columns first. If we want Genre, we usually need a separate query or join.
            // For MVP, we might skip direct genre query here unless we implement the complex join.
            // Wait, Android Q+ deprecates direct file paths but we use DATA for compatibility or relative path.
            // Actually, querying Genres is separate.
            // To be efficient, we might need to query all Genres members.
            // Let's defer complex genre query implementation to avoid slowing down scanning massively.
            // We'll set genre to null for now in this batch query, and maybe update it later or use a specific loader.
            // Or, if we are lucky, some devices support "genre_name" column but it's not standard.
        )

        // 移除 IS_MUSIC 限制，改用時長過濾 + MIME 類型驗證
        // 這樣可以捕捉到更多可能沒有正確標記的音樂檔案
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val dataPath = cursor.getString(dataColumn) ?: ""
                    val mimeType = cursor.getString(mimeTypeColumn) ?: ""

                    // MIME 類型過濾（或副檔名過濾作為備用）
                    val isValidAudio = AUDIO_MIME_TYPES.any { mimeType.startsWith(it.replace("/*", "")) } ||
                                       AUDIO_EXTENSIONS.any { dataPath.lowercase().endsWith(it) }

                    if (!isValidAudio) continue

                    // Path filtering (只有當用戶設定了特定資料夾時才過濾)
                    if (includedPaths.isNotEmpty()) {
                        val isIncluded = includedPaths.any { path -> dataPath.startsWith(path) }
                        if (!isIncluded) continue
                    }

                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)
                    val track = cursor.getInt(trackColumn)
                    val year = cursor.getInt(yearColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val contentUri = ContentUris.withAppendedId(collection, id)

                    songs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            duration = duration,
                            contentUri = contentUri.toString(),
                            dataPath = dataPath,
                            trackNumber = track,
                            year = year,
                            dateAdded = dateAdded,
                            genre = null // Genre fetching requires separate query
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // Log error or handle permission denial gracefully
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext songs
    }

    suspend fun deleteSong(song: Song) = withContext(Dispatchers.IO) {
        try {
            val uri = android.net.Uri.parse(song.contentUri)
            context.contentResolver.delete(uri, null, null)
        } catch (e: SecurityException) {
            // Re-throw so the UI layer can handle RecoverableSecurityException on Android 10+
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    /**
     * For Android R (API 30) and above, creates a batch delete request.
     */
    fun createDeleteRequest(songs: List<Song>): android.content.IntentSender? {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val uris = songs.map { android.net.Uri.parse(it.contentUri) }
            val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
            return pendingIntent.intentSender
        }
        return null
    }
}

