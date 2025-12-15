package com.gemini.music.domain.repository

import com.gemini.music.domain.model.FolderContent
import com.gemini.music.domain.model.MusicFolder
import com.gemini.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Repository for folder-based music browsing
 */
interface FolderRepository {
    
    /**
     * Get all root folders containing music
     */
    fun getRootFolders(): Flow<List<MusicFolder>>
    
    /**
     * Get content of a specific folder
     */
    fun getFolderContent(path: String): Flow<FolderContent>
    
    /**
     * Get all songs in a folder (including subfolders optionally)
     */
    fun getSongsInFolder(path: String, includeSubfolders: Boolean = false): Flow<List<Song>>
    
    /**
     * Get folder hierarchy as a tree
     */
    fun getFolderTree(): Flow<List<MusicFolder>>
    
    /**
     * Search within a specific folder
     */
    fun searchInFolder(path: String, query: String): Flow<List<Song>>
    
    /**
     * Get parent folder path
     */
    fun getParentPath(path: String): String?
    
    /**
     * Get breadcrumb path segments
     */
    fun getBreadcrumbs(path: String): List<Pair<String, String>>
}
