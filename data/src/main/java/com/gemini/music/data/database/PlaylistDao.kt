package com.gemini.music.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.ColumnInfo
import androidx.room.Embedded
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("""
        SELECT 
            p.*, 
            (SELECT s.albumId FROM playlist_songs ps JOIN songs s ON ps.songId = s.id WHERE ps.playlistId = p.playlistId ORDER BY ps.dateAdded ASC LIMIT 1) as coverAlbumId,
            (SELECT COUNT(*) FROM playlist_songs ps WHERE ps.playlistId = p.playlistId) as songCount
        FROM playlists p
        ORDER BY p.name ASC
    """)
    fun getAllPlaylists(): Flow<List<PlaylistWithMeta>>

    @Query("""
        SELECT 
            p.*, 
            (SELECT s.albumId FROM playlist_songs ps JOIN songs s ON ps.songId = s.id WHERE ps.playlistId = p.playlistId ORDER BY ps.dateAdded ASC LIMIT 1) as coverAlbumId,
            (SELECT COUNT(*) FROM playlist_songs ps WHERE ps.playlistId = p.playlistId) as songCount
        FROM playlists p
        WHERE p.playlistId = :playlistId
    """)
    fun getPlaylist(playlistId: Long): Flow<PlaylistWithMeta?>

    data class PlaylistWithMeta(
        @Embedded val playlist: PlaylistEntity,
        @ColumnInfo(name = "coverAlbumId") val coverAlbumId: Long?,
        @ColumnInfo(name = "songCount") val songCount: Int
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    // Using a custom data class (embedded) usually, but for mapping to Domain we can just return Songs
    // However, Room needs to know how to join.
    @Transaction
    @Query("""
        SELECT * FROM songs 
        INNER JOIN playlist_songs ON songs.id = playlist_songs.songId 
        WHERE playlist_songs.playlistId = :playlistId
        ORDER BY playlist_songs.dateAdded ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>
}
