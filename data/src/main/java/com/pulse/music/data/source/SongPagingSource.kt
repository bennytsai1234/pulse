package com.pulse.music.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pulse.music.data.database.SongDao
import com.pulse.music.data.database.asDomainModel
import com.pulse.music.domain.model.Song
import javax.inject.Inject

enum class SongSortOrder {
    TITLE,
    DATE_ADDED,
    ARTIST,
    ALBUM
}

class SongPagingSource @Inject constructor(
    private val songDao: SongDao,
    private val query: String = "", // Optional search query
    private val sortOrder: SongSortOrder = SongSortOrder.TITLE
) : PagingSource<Int, Song>() {

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        val page = params.key ?: 0
        val pageSize = params.loadSize

        return try {
            val offset = page * pageSize
            val entities = if (query.isNotBlank()) {
                songDao.searchSongsPaged(query = query, limit = pageSize, offset = offset)
            } else {
                when (sortOrder) {
                    SongSortOrder.TITLE -> songDao.getSongsPaged(limit = pageSize, offset = offset)
                    SongSortOrder.DATE_ADDED -> songDao.getSongsPagedByDateAdded(limit = pageSize, offset = offset)
                    SongSortOrder.ARTIST -> songDao.getSongsPagedByArtist(limit = pageSize, offset = offset)
                    SongSortOrder.ALBUM -> songDao.getSongsPagedByAlbum(limit = pageSize, offset = offset)
                }
            }

            val songs = entities.map { it.asDomainModel() }
            
            LoadResult.Page(
                data = songs,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (songs.isEmpty() || songs.size < pageSize) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
