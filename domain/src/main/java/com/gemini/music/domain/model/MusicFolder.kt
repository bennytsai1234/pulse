package com.gemini.music.domain.model

/**
 * 音樂資料夾
 */
data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int = 0,
    val totalDuration: Long = 0,
    val lastModified: Long = 0,
    val coverArtUri: String? = null // 封面從第一首歌取得
)

/**
 * 資料夾內容
 */
data class FolderContent(
    val folder: MusicFolder,
    val songs: List<Song>,
    val subfolders: List<MusicFolder> = emptyList()
)

/**
 * 資料夾樹節點
 */
data class FolderTreeNode(
    val path: String,
    val name: String,
    val isExpanded: Boolean = false,
    val depth: Int = 0,
    val songCount: Int = 0,
    val children: List<FolderTreeNode> = emptyList()
)
