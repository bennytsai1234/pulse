package com.gemini.music.domain.model

/**
 * 代表一個音樂推薦項目。
 * 推薦引擎會根據用戶的聆聽習慣生成個人化推薦。
 */
data class Recommendation(
    val song: Song,
    val reason: RecommendationReason,
    val score: Float, // 0.0 ~ 1.0，推薦分數
    val context: String? = null // 額外說明，如 "因為你喜歡 XXX"
)

/**
 * 推薦原因類型。
 */
enum class RecommendationReason {
    FREQUENTLY_PLAYED,      // 經常播放的歌曲
    SIMILAR_ARTIST,         // 類似藝人的歌曲
    SAME_GENRE,             // 相同類型
    TIME_BASED,             // 時段推薦 (早晨/夜晚)
    RECENTLY_DISCOVERED,    // 最近發現但未深度聆聽
    FORGOTTEN_FAVORITE,     // 曾經常聽但近期未播放
    MOOD_BASED,             // 心情推薦
    RELEASE_ANNIVERSARY     // 發行週年紀念
}

/**
 * 推薦播放清單類型。
 */
data class RecommendationPlaylist(
    val type: RecommendationPlaylistType,
    val title: String,
    val subtitle: String,
    val songs: List<Song>,
    val coverArtUri: String? = null
)

enum class RecommendationPlaylistType {
    DAILY_MIX,              // 每日精選
    MORNING_VIBES,          // 早晨活力
    EVENING_CHILL,          // 夜間放鬆
    WORKOUT_BOOST,          // 運動激勵
    THROWBACK,              // 懷舊回憶
    DEEP_CUTS,              // 深度探索 (較少播放的歌曲)
    NEW_DISCOVERIES,        // 新發現
    ARTIST_RADIO            // 藝人電台
}
