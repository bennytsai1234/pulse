package com.gemini.music.data.repository

import com.gemini.music.data.database.SongDao
import com.gemini.music.data.database.asDomainModel
import com.gemini.music.domain.model.FolderContent
import com.gemini.music.domain.model.MusicFolder
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.FolderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : FolderRepository {
    
    override fun getRootFolders(): Flow<List<MusicFolder>> {
        return songDao.getAllSongs().map { songs ->
            songs.groupBy { extractFolderPath(it.dataPath) }
                .map { (path, folderSongs) ->
                    MusicFolder(
                        path = path,
                        name = File(path).name.ifEmpty { path },
                        songCount = folderSongs.size,
                        totalDuration = folderSongs.sumOf { it.duration },
                        lastModified = folderSongs.maxOfOrNull { it.dateAdded } ?: 0L,
                        coverArtUri = folderSongs.firstOrNull()?.let { 
                            "content://media/external/audio/albumart/${it.albumId}" 
                        }
                    )
                }
                .sortedBy { it.name.lowercase() }
        }.flowOn(Dispatchers.Default)
    }
    
    override fun getFolderContent(path: String): Flow<FolderContent> {
        return songDao.getAllSongs().map { songs ->
            // Filter songs in this exact folder
            val folderSongs = songs.filter { 
                extractFolderPath(it.dataPath) == path 
            }.map { it.asDomainModel() }
            
            // Find subfolders
            val subfolderPaths = songs
                .filter { it.dataPath.startsWith(path) && extractFolderPath(it.dataPath) != path }
                .map { extractFolderPath(it.dataPath) }
                .distinct()
                .filter { subPath ->
                    // Only include immediate children
                    val relativePath = subPath.removePrefix(path).trimStart(File.separatorChar)
                    !relativePath.contains(File.separatorChar)
                }
            
            val subfolders = subfolderPaths.map { subPath ->
                val subSongs = songs.filter { extractFolderPath(it.dataPath).startsWith(subPath) }
                MusicFolder(
                    path = subPath,
                    name = File(subPath).name,
                    songCount = subSongs.size,
                    totalDuration = subSongs.sumOf { it.duration },
                    lastModified = subSongs.maxOfOrNull { it.dateAdded } ?: 0L,
                    coverArtUri = subSongs.firstOrNull()?.let {
                        "content://media/external/audio/albumart/${it.albumId}"
                    }
                )
            }.sortedBy { it.name.lowercase() }
            
            val folder = MusicFolder(
                path = path,
                name = File(path).name.ifEmpty { path },
                songCount = folderSongs.size,
                totalDuration = folderSongs.sumOf { it.duration },
                coverArtUri = folderSongs.firstOrNull()?.albumArtUri
            )
            
            FolderContent(
                folder = folder,
                songs = folderSongs.sortedBy { it.title.lowercase() },
                subfolders = subfolders
            )
        }.flowOn(Dispatchers.Default)
    }
    
    override fun getSongsInFolder(path: String, includeSubfolders: Boolean): Flow<List<Song>> {
        return songDao.getAllSongs().map { songs ->
            val filtered = if (includeSubfolders) {
                songs.filter { it.dataPath.startsWith(path) }
            } else {
                songs.filter { extractFolderPath(it.dataPath) == path }
            }
            filtered.map { it.asDomainModel() }.sortedBy { it.title.lowercase() }
        }.flowOn(Dispatchers.Default)
    }
    
    override fun getFolderTree(): Flow<List<MusicFolder>> {
        return getRootFolders()
    }
    
    override fun searchInFolder(path: String, query: String): Flow<List<Song>> {
        val lowerQuery = query.lowercase()
        return getSongsInFolder(path, includeSubfolders = true).map { songs ->
            songs.filter { song ->
                song.title.lowercase().contains(lowerQuery) ||
                song.artist.lowercase().contains(lowerQuery) ||
                song.album.lowercase().contains(lowerQuery)
            }
        }
    }
    
    override fun getParentPath(path: String): String? {
        val parent = File(path).parent
        return if (parent != null && parent != "/") parent else null
    }
    
    override fun getBreadcrumbs(path: String): List<Pair<String, String>> {
        val parts = mutableListOf<Pair<String, String>>()
        var currentPath = path
        
        while (currentPath.isNotEmpty() && currentPath != "/") {
            val name = File(currentPath).name
            if (name.isNotEmpty()) {
                parts.add(0, currentPath to name)
            }
            currentPath = File(currentPath).parent ?: break
        }
        
        return parts
    }
    
    private fun extractFolderPath(filePath: String): String {
        return File(filePath).parent ?: ""
    }
}
