package com.gemini.music.domain.repository

import com.gemini.music.domain.model.Recommendation
import com.gemini.music.domain.model.RecommendationPlaylist
import com.gemini.music.domain.model.RecommendationPlaylistType
import com.gemini.music.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * 推薦系統資料倉庫介面。
 * 基於用戶聆聽習慣生成個人化音樂推薦。
 */
interface RecommendationRepository {
    
    /**
     * 取得個人化推薦歌曲。
     * @param limit 推薦數量上限
     */
    fun getRecommendations(limit: Int = 20): Flow<List<Recommendation>>
    
    /**
     * 取得特定類型的推薦播放清單。
     */
    fun getRecommendationPlaylist(type: RecommendationPlaylistType): Flow<RecommendationPlaylist?>
    
    /**
     * 取得所有可用的推薦播放清單。
     */
    fun getAllRecommendationPlaylists(): Flow<List<RecommendationPlaylist>>
    
    /**
     * 取得「每日精選」播放清單。
     * 每天會根據聆聽習慣重新生成。
     */
    fun getDailyMix(): Flow<RecommendationPlaylist>
    
    /**
     * 取得基於時段的推薦。
     * @param hourOfDay 當前小時 (0-23)
     */
    fun getTimeBasedRecommendations(hourOfDay: Int): Flow<List<Song>>
    
    /**
     * 取得類似歌曲推薦。
     * @param songId 基準歌曲 ID
     */
    fun getSimilarSongs(songId: Long, limit: Int = 10): Flow<List<Song>>
    
    /**
     * 取得藝人電台。
     * @param artistName 藝人名稱
     */
    fun getArtistRadio(artistName: String): Flow<List<Song>>
    
    /**
     * 取得被遺忘的最愛 (曾經常聽但近期未播放)。
     */
    fun getForgottenFavorites(daysSinceLastPlayed: Int = 30, limit: Int = 20): Flow<List<Song>>
    
    /**
     * 取得深度探索 (較少播放但品質高的歌曲)。
     */
    fun getDeepCuts(limit: Int = 20): Flow<List<Song>>
    
    /**
     * 刷新推薦數據。
     * 通常在歌曲播放完成後調用。
     */
    suspend fun refreshRecommendations()
    
    /**
     * 標記推薦為「不喜歡」，將來減少類似推薦。
     */
    suspend fun markAsDisliked(songId: Long)
}
