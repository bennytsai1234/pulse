package com.gemini.music.data.source

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.gemini.music.domain.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 負責從 Android MediaStore 查詢本地音訊檔案。
 */
class LocalAudioSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
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
            MediaStore.Audio.Media.DATA
        )

        // 過濾掉太短的音訊
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
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

                while (cursor.moveToNext()) {
                    val dataPath = cursor.getString(dataColumn) ?: ""
                    
                    // Path filtering
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
                            dateAdded = dateAdded
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
}
