package com.sigma.music.data.repository

import com.sigma.music.core.datastore.UserPreferencesRepository
import com.sigma.music.data.database.SongDao
import com.sigma.music.data.database.asDomainModel
import com.sigma.music.data.database.asEntity
import com.sigma.music.data.source.LocalAudioSource
import com.sigma.music.domain.model.Album
import com.sigma.music.domain.model.Artist
import com.sigma.music.domain.model.Song
import com.sigma.music.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MusicRepositoryImpl @Inject constructor(
    private val localAudioSource: LocalAudioSource,
    private val songDao: SongDao,
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
}
