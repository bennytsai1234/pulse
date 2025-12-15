package com.gemini.music.domain.model

/**
 * 智慧播放清單類型
 */
enum class SmartPlaylistType {
    MOST_PLAYED,        // 最常播放
    RECENTLY_PLAYED,    // 最近播放
    RECENTLY_ADDED,     // 最近新增
    NEVER_PLAYED,       // 從未播放
    FAVORITES,          // 最愛歌曲
    LONG_SONGS,         // 長時間歌曲 (> 5 分鐘)
    SHORT_SONGS,        // 短歌曲 (< 3 分鐘)
    THIS_WEEK,          // 本週播放
    THIS_MONTH          // 本月播放
}

/**
 * 智慧播放清單
 */
data class SmartPlaylist(
    val type: SmartPlaylistType,
    val name: String,
    val description: String,
    val iconName: String, // Material Icon name
    val songCount: Int = 0,
    val totalDuration: Long = 0
) {
    companion object {
        fun createAll(): List<SmartPlaylist> = listOf(
            SmartPlaylist(
                type = SmartPlaylistType.MOST_PLAYED,
                name = "Most Played",
                description = "Your all-time favorites",
                iconName = "TrendingUp"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.RECENTLY_PLAYED,
                name = "Recently Played",
                description = "Songs you've listened to lately",
                iconName = "History"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.RECENTLY_ADDED,
                name = "Recently Added",
                description = "Newly added to your library",
                iconName = "NewReleases"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.NEVER_PLAYED,
                name = "Discover",
                description = "Songs you haven't played yet",
                iconName = "Explore"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.FAVORITES,
                name = "Favorites",
                description = "Songs you've marked as favorites",
                iconName = "Favorite"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.LONG_SONGS,
                name = "Long Songs",
                description = "Songs over 5 minutes",
                iconName = "Timer"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.SHORT_SONGS,
                name = "Quick Hits",
                description = "Songs under 3 minutes",
                iconName = "FlashOn"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.THIS_WEEK,
                name = "This Week",
                description = "Your weekly mix",
                iconName = "DateRange"
            ),
            SmartPlaylist(
                type = SmartPlaylistType.THIS_MONTH,
                name = "This Month",
                description = "Your monthly mix",
                iconName = "CalendarMonth"
            )
        )
    }
}
