package com.gemini.music.data.repository


import com.gemini.music.data.database.PlaylistDao
import com.gemini.music.data.database.SongDao
import com.gemini.music.data.database.asDomainModel
import com.gemini.music.data.database.asEntity
import com.gemini.music.data.source.LocalAudioSource
import com.gemini.music.domain.model.Album
import com.gemini.music.domain.model.Artist
import com.gemini.music.domain.model.Playlist
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val localAudioSource: LocalAudioSource,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : MusicRepository {

    override fun getSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override fun getSong(id: Long): Flow<Song?> {
        return songDao.getSong(id).map { it?.asDomainModel() }
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override fun getRecentlyAdded(): Flow<List<Song>> {
        return songDao.getRecentlyAdded().map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override fun getAlbums(): Flow<List<Album>> {
        // 在記憶體中聚合 (MVP 方案)
        return getSongs().map { songs ->
            songs.groupBy { it.albumId }
                .map { (albumId, albumSongs) ->
                    val firstSong = albumSongs.first()
                    Album(
                        id = albumId,
                        title = firstSong.album,
                        artist = firstSong.artist,
                        songCount = albumSongs.size
                    )
                }
                .sortedBy { it.title }
        }
    }

    override fun getArtists(): Flow<List<Artist>> {
        // 在記憶體中聚合 (MVP 方案)
        return getSongs().map { songs ->
            songs.groupBy { it.artist }
                .map { (artistName, artistSongs) ->
                    Artist(
                        name = artistName,
                        songCount = artistSongs.size
                    )
                }
                .sortedBy { it.name }
        }
    }

    override fun getSongsByAlbumId(albumId: Long): Flow<List<Song>> {
        return songDao.getSongsByAlbumId(albumId).map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override suspend fun scanLocalMusic() {
        val minDuration = userPreferencesRepository.minAudioDuration.first()
        val includedFolders = userPreferencesRepository.includedFolders.first()

        // 1. 從 MediaStore 獲取最新資料
        val songsFromSystem = localAudioSource.loadMusic(minDuration, includedFolders)
        
        // 2. 轉換為 Entity 並更新至 Room 資料庫 (Transaction)
        songDao.updateMusicLibrary(songsFromSystem.map { it.asEntity() })
    }

    // --- Playlist Implementation ---

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { 
                Playlist(
                    id = it.playlist.playlistId,
                    name = it.playlist.name,
                    songCount = it.songCount,
                    coverArtUri = it.coverAlbumId?.let { albumId -> "content://media/external/audio/albumart/$albumId" }
                ) 
            }
        }
    }

    override fun getPlaylist(playlistId: Long): Flow<Playlist?> {
        return playlistDao.getPlaylist(playlistId).map { it ->
            it?.let {
                Playlist(
                    id = it.playlist.playlistId,
                    name = it.playlist.name,
                    songCount = it.songCount,
                    coverArtUri = it.coverAlbumId?.let { albumId -> "content://media/external/audio/albumart/$albumId" }
                )
            }
        }
    }

    override fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.asDomainModel() }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(
            com.gemini.music.data.database.PlaylistEntity(name = name)
        )
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.insertPlaylistSongCrossRef(
            com.gemini.music.data.database.PlaylistSongCrossRef(playlistId, songId)
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
}
