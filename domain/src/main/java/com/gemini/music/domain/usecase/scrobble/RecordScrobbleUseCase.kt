package com.gemini.music.domain.usecase.scrobble

import com.gemini.music.domain.model.ScrobbleEntry
import com.gemini.music.domain.model.Song
import com.gemini.music.domain.repository.ScrobbleRepository
import javax.inject.Inject

/**
 * 記錄 Scrobble 的 UseCase。
 * 當歌曲播放超過一定比例時調用。
 */
class RecordScrobbleUseCase @Inject constructor(
    private val scrobbleRepository: ScrobbleRepository
) {
    /**
     * 記錄一次 Scrobble。
     * @param song 播放的歌曲
     * @param playedDuration 實際播放時長 (毫秒)
     * @param minPlayPercentage 最小播放百分比 (預設 50%)
     */
    suspend operator fun invoke(
        song: Song,
        playedDuration: Long,
        minPlayPercentage: Float = 0.5f
    ): Boolean {
        // 檢查是否達到 scrobble 標準
        val minDuration = (song.duration * minPlayPercentage).toLong()
        val minAbsoluteDuration = 30_000L // 至少播放 30 秒
        
        if (playedDuration < minOf(minDuration, minAbsoluteDuration)) {
            return false
        }
        
        val entry = ScrobbleEntry(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            duration = song.duration,
            timestamp = System.currentTimeMillis()
        )
        
        scrobbleRepository.recordScrobble(entry)
        return true
    }
}
