package com.gemini.music.data.repository

import com.gemini.music.data.database.PlaybackHistoryDao
import com.gemini.music.data.database.SongDao
import com.gemini.music.domain.model.Recommendation
import com.gemini.music.domain.model.RecommendationPlaylist
import com.gemini.music.domain.model.RecommendationPlaylistType
import com.gemini.music.domain.model.RecommendationReason
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.MusicRepository
import com.gemini.music.domain.repository.RecommendationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 推薦引擎實作。
 * 基於本地聆聽歷史和歌曲特徵生成個人化推薦。
 */
@Singleton
class RecommendationRepositoryImpl @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playbackHistoryDao: PlaybackHistoryDao,
    private val songDao: SongDao
) : RecommendationRepository {
    
    override fun getRecommendations(limit: Int): Flow<List<Recommendation>> = flow {
        val allSongs = musicRepository.getSongs().first()
        val topPlayed = playbackHistoryDao.getMostPlayedSongs(20).first()
        val recentlyPlayed = playbackHistoryDao.getRecentlyPlayed(50).first()
        
        val recommendations = mutableListOf<Recommendation>()
        val usedSongIds = mutableSetOf<Long>()
        
        // 1. 經常播放的歌曲 (高分)
        topPlayed.take(5).forEach { stats ->
            allSongs.find { it.id == stats.songId }?.let { song ->
                if (usedSongIds.add(song.id)) {
                    recommendations.add(
                        Recommendation(
                            song = song,
                            reason = RecommendationReason.FREQUENTLY_PLAYED,
                            score = 0.9f,
                            context = "你的最愛之一"
                        )
                    )
                }
            }
        }
        
        // 2. 類似藝人的歌曲
        val topArtists = topPlayed.mapNotNull { stats ->
            allSongs.find { it.id == stats.songId }?.artist
        }.groupingBy { it }.eachCount().entries.sortedByDescending { entry -> entry.value }.take(3).map { entry -> entry.key }
        
        topArtists.forEach { artist ->
            allSongs.filter { it.artist == artist && it.id !in usedSongIds }
                .shuffled()
                .take(2)
                .forEach { song ->
                    if (usedSongIds.add(song.id)) {
                        recommendations.add(
                            Recommendation(
                                song = song,
                                reason = RecommendationReason.SIMILAR_ARTIST,
                                score = 0.75f,
                                context = "來自 $artist"
                            )
                        )
                    }
                }
        }
        
        // 3. 被遺忘的最愛
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val recentSongIds = recentlyPlayed.filter { it.playedAt > thirtyDaysAgo }.map { it.songId }.toSet()
        val forgottenFavorites = topPlayed.filter { it.songId !in recentSongIds }
        
        forgottenFavorites.take(3).forEach { stats ->
            allSongs.find { it.id == stats.songId }?.let { song ->
                if (usedSongIds.add(song.id)) {
                    recommendations.add(
                        Recommendation(
                            song = song,
                            reason = RecommendationReason.FORGOTTEN_FAVORITE,
                            score = 0.7f,
                            context = "你可能忘記了這首歌"
                        )
                    )
                }
            }
        }
        
        // 4. 新發現 (從未播放或很少播放)
        val playedSongIds = recentlyPlayed.map { it.songId }.toSet()
        allSongs.filter { it.id !in playedSongIds }
            .shuffled()
            .take(5)
            .forEach { song ->
                if (usedSongIds.add(song.id)) {
                    recommendations.add(
                        Recommendation(
                            song = song,
                            reason = RecommendationReason.RECENTLY_DISCOVERED,
                            score = 0.6f,
                            context = "探索新歌曲"
                        )
                    )
                }
            }
        
        emit(recommendations.sortedByDescending { it.score }.take(limit))
    }.flowOn(Dispatchers.Default)
    
    override fun getRecommendationPlaylist(type: RecommendationPlaylistType): Flow<RecommendationPlaylist?> {
        return when (type) {
            RecommendationPlaylistType.DAILY_MIX -> getDailyMix()
            RecommendationPlaylistType.MORNING_VIBES -> getTimeBasedPlaylist("早晨活力", 6..11)
            RecommendationPlaylistType.EVENING_CHILL -> getTimeBasedPlaylist("夜間放鬆", 20..23)
            RecommendationPlaylistType.THROWBACK -> getForgottenFavorites(60, 30).map { songs ->
                RecommendationPlaylist(
                    type = type,
                    title = "懷舊回憶",
                    subtitle = "重溫你曾經喜愛的歌曲",
                    songs = songs
                )
            }
            RecommendationPlaylistType.DEEP_CUTS -> getDeepCuts(30).map { songs ->
                RecommendationPlaylist(
                    type = type,
                    title = "深度探索",
                    subtitle = "發現隱藏的寶石",
                    songs = songs
                )
            }
            else -> flow { emit(null) }
        }
    }
    
    override fun getAllRecommendationPlaylists(): Flow<List<RecommendationPlaylist>> {
        return combine(
            getDailyMix(),
            getDeepCuts(20).map { songs ->
                RecommendationPlaylist(
                    type = RecommendationPlaylistType.DEEP_CUTS,
                    title = "深度探索",
                    subtitle = "發現隱藏的寶石",
                    songs = songs
                )
            },
            getForgottenFavorites(30, 20).map { songs ->
                RecommendationPlaylist(
                    type = RecommendationPlaylistType.THROWBACK,
                    title = "懷舊回憶",
                    subtitle = "重溫你曾經喜愛的歌曲",
                    songs = songs
                )
            }
        ) { dailyMix, deepCuts, throwback ->
            listOfNotNull(
                dailyMix,
                deepCuts.takeIf { it.songs.isNotEmpty() },
                throwback.takeIf { it.songs.isNotEmpty() }
            )
        }
    }
    
    override fun getDailyMix(): Flow<RecommendationPlaylist> = flow {
        val allSongs = musicRepository.getSongs().first()
        val topPlayed = playbackHistoryDao.getMostPlayedSongs(50).first()
        
        val mixSongs = mutableListOf<Song>()
        val usedIds = mutableSetOf<Long>()
        
        // 40% 來自最愛
        topPlayed.shuffled().take((allSongs.size * 0.4).toInt().coerceAtMost(15)).forEach { stats ->
            allSongs.find { it.id == stats.songId }?.let { song ->
                if (usedIds.add(song.id)) mixSongs.add(song)
            }
        }
        
        // 30% 來自喜愛藝人的其他歌曲
        val favoriteArtists = topPlayed.mapNotNull { stats ->
            allSongs.find { it.id == stats.songId }?.artist
        }.distinct().take(5)
        
        allSongs.filter { it.artist in favoriteArtists && it.id !in usedIds }
            .shuffled()
            .take(10)
            .forEach { if (usedIds.add(it.id)) mixSongs.add(it) }
        
        // 30% 隨機發現
        allSongs.filter { it.id !in usedIds }
            .shuffled()
            .take(10)
            .forEach { if (usedIds.add(it.id)) mixSongs.add(it) }
        
        emit(
            RecommendationPlaylist(
                type = RecommendationPlaylistType.DAILY_MIX,
                title = "每日精選",
                subtitle = "根據你的品味精心挑選",
                songs = mixSongs.shuffled(),
                coverArtUri = mixSongs.firstOrNull()?.albumArtUri
            )
        )
    }.flowOn(Dispatchers.Default)
    
    override fun getTimeBasedRecommendations(hourOfDay: Int): Flow<List<Song>> = flow {
        val allSongs = musicRepository.getSongs().first()
        
        // 基於時段的簡單推薦邏輯
        val filtered = when (hourOfDay) {
            in 6..11 -> {
                // 早晨：較輕快的歌曲（時長較短）
                allSongs.filter { it.duration < 4 * 60 * 1000 }
            }
            in 12..17 -> {
                // 下午：中等能量
                allSongs
            }
            in 18..21 -> {
                // 傍晚：放鬆
                allSongs.filter { it.duration > 3 * 60 * 1000 }
            }
            else -> {
                // 深夜：安靜的歌曲（較長的歌曲通常較抒情）
                allSongs.filter { it.duration > 4 * 60 * 1000 }
            }
        }
        
        emit(filtered.shuffled().take(20))
    }.flowOn(Dispatchers.Default)
    
    override fun getSimilarSongs(songId: Long, limit: Int): Flow<List<Song>> = flow {
        val allSongs = musicRepository.getSongs().first()
        val targetSong = allSongs.find { it.id == songId } ?: run {
            emit(emptyList())
            return@flow
        }
        
        // 相似度計算：同藝人 > 同專輯 > 相似時長
        val scored = allSongs.filter { it.id != songId }.map { song ->
            var score = 0f
            if (song.artist == targetSong.artist) score += 0.5f
            if (song.album == targetSong.album) score += 0.3f
            
            // 時長相似性 (±30秒內得分)
            val durationDiff = kotlin.math.abs(song.duration - targetSong.duration)
            if (durationDiff < 30_000) score += 0.2f * (1 - durationDiff / 30_000f)
            
            song to score
        }.filter { it.second > 0 }.sortedByDescending { it.second }
        
        emit(scored.take(limit).map { it.first })
    }.flowOn(Dispatchers.Default)
    
    override fun getArtistRadio(artistName: String): Flow<List<Song>> = flow {
        val allSongs = musicRepository.getSongs().first()
        
        // 50% 來自該藝人
        val artistSongs = allSongs.filter { it.artist.equals(artistName, ignoreCase = true) }.shuffled()
        val radioSongs = mutableListOf<Song>()
        radioSongs.addAll(artistSongs.take((artistSongs.size * 0.5).toInt().coerceAtMost(15)))
        
        // 50% 來自其他相似風格（這裡簡化為隨機）
        val otherSongs = allSongs.filter { !it.artist.equals(artistName, ignoreCase = true) }.shuffled()
        radioSongs.addAll(otherSongs.take(15))
        
        emit(radioSongs.shuffled())
    }.flowOn(Dispatchers.Default)
    
    override fun getForgottenFavorites(daysSinceLastPlayed: Int, limit: Int): Flow<List<Song>> = flow {
        val allSongs = musicRepository.getSongs().first()
        val topPlayed = playbackHistoryDao.getMostPlayedSongs(100).first()
        val recentHistory = playbackHistoryDao.getRecentlyPlayed(500).first()
        
        val cutoffTime = System.currentTimeMillis() - daysSinceLastPlayed.toLong() * 24 * 60 * 60 * 1000
        
        val recentSongIds = recentHistory.filter { it.playedAt > cutoffTime }.map { it.songId }.toSet()
        
        val forgotten = topPlayed
            .filter { it.songId !in recentSongIds }
            .take(limit)
            .mapNotNull { stats -> allSongs.find { it.id == stats.songId } }
        
        emit(forgotten)
    }.flowOn(Dispatchers.Default)
    
    override fun getDeepCuts(limit: Int): Flow<List<Song>> = flow {
        val allSongs = musicRepository.getSongs().first()
        val allHistory = playbackHistoryDao.getRecentlyPlayed(Int.MAX_VALUE).first()
        
        val playCountMap = allHistory.groupingBy { it.songId }.eachCount()
        
        // 找出播放次數少於 3 次的歌曲
        val deepCuts = allSongs.filter { song ->
            (playCountMap[song.id] ?: 0) < 3
        }.shuffled().take(limit)
        
        emit(deepCuts)
    }.flowOn(Dispatchers.Default)
    
    override suspend fun refreshRecommendations() {
        // 推薦是實時計算的，無需刷新快取
    }
    
    override suspend fun markAsDisliked(songId: Long) {
        // TODO: 實作不喜歡標記功能，影響未來推薦
    }
    
    // === Helper Functions ===
    
    private fun getTimeBasedPlaylist(title: String, hourRange: IntRange): Flow<RecommendationPlaylist?> = flow {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour !in hourRange) {
            emit(null)
            return@flow
        }
        
        val songs = getTimeBasedRecommendations(currentHour).first()
        emit(
            RecommendationPlaylist(
                type = when (hourRange.first) {
                    in 6..11 -> RecommendationPlaylistType.MORNING_VIBES
                    else -> RecommendationPlaylistType.EVENING_CHILL
                },
                title = title,
                subtitle = "適合現在的心情",
                songs = songs
            )
        )
    }
}
