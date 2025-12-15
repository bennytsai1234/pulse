package com.gemini.music.domain.model

/**
 * 單次播放記錄
 */
data class PlaybackRecord(
    val id: Long = 0,
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val playedAt: Long = System.currentTimeMillis(),
    val durationPlayed: Long = 0, // 實際播放的毫秒數
    val completed: Boolean = false // 是否完整播放
)

/**
 * 歌曲播放統計
 */
data class SongPlayStats(
    val songId: Long,
    val songTitle: String,
    val artistName: String,
    val albumName: String,
    val albumArtUri: String? = null,
    val totalPlayCount: Int = 0,
    val totalPlayTime: Long = 0, // 總播放時間 (毫秒)
    val lastPlayedAt: Long = 0
)

/**
 * 藝術家播放統計
 */
data class ArtistPlayStats(
    val artistName: String,
    val totalPlayCount: Int = 0,
    val totalPlayTime: Long = 0,
    val songCount: Int = 0
)

/**
 * 每日聆聽統計
 */
data class DailyListeningStats(
    val date: String, // YYYY-MM-DD
    val totalPlayCount: Int = 0,
    val totalPlayTime: Long = 0, // 毫秒
    val uniqueSongsPlayed: Int = 0
)

/**
 * 總體聆聽統計概覽
 */
data class ListeningStatsOverview(
    val totalPlayCount: Int = 0,
    val totalPlayTime: Long = 0,
    val uniqueSongsPlayed: Int = 0,
    val uniqueArtistsPlayed: Int = 0,
    val averageDailyPlayTime: Long = 0,
    val mostPlayedSong: SongPlayStats? = null,
    val mostPlayedArtist: ArtistPlayStats? = null,
    val recentlyPlayed: List<PlaybackRecord> = emptyList(),
    val topSongs: List<SongPlayStats> = emptyList(),
    val topArtists: List<ArtistPlayStats> = emptyList()
)
