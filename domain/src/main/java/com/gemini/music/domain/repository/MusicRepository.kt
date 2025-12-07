package com.gemini.music.domain.repository

import com.gemini.music.domain.model.Album
import com.gemini.music.domain.model.Artist
import com.gemini.music.domain.model.Playlist
import com.gemini.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * 音樂資料倉庫介面。
 * 負責協調本地資料庫 (Room) 與系統媒體庫 (MediaStore) 的資料流。
 */
interface MusicRepository {
    // 觀察所有歌曲 (單一來源真相：Database)
    fun getSongs(): Flow<List<Song>>

    // 觀察特定歌曲
    fun getSong(id: Long): Flow<Song?>

    // 搜尋歌曲
    fun searchSongs(query: String): Flow<List<Song>>

    // 取得最近加入的歌曲
    fun getRecentlyAdded(): Flow<List<Song>>

    // 取得所有專輯 (聚合)
    fun getAlbums(): Flow<List<Album>>

    // 取得所有藝人 (聚合)
    fun getArtists(): Flow<List<Artist>>

    // 取得特定專輯的歌曲
    fun getSongsByAlbumId(albumId: Long): Flow<List<Song>>

    // 觸發掃描系統媒體庫並更新資料庫
    suspend fun scanLocalMusic()
    
    // --- Playlist Management ---
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylist(playlistId: Long): Flow<Playlist?>
    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>>
    suspend fun createPlaylist(name: String): Long
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun addSongToPlaylist(playlistId: Long, songId: Long)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
}
