package com.gemini.music.domain.usecase

import com.gemini.music.domain.model.MusicState
import com.gemini.music.domain.model.RepeatMode
import com.gemini.music.domain.repository.MusicController
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * TogglePlayPauseUseCase 單元測試
 * 
 * 測試策略：驗證播放/暫停切換邏輯的正確性
 */
class TogglePlayPauseUseCaseTest {

    private lateinit var musicController: MusicController
    private lateinit var togglePlayPauseUseCase: TogglePlayPauseUseCase
    private lateinit var musicStateFlow: MutableStateFlow<MusicState>

    @BeforeEach
    fun setUp() {
        musicController = mockk(relaxed = true)
        musicStateFlow = MutableStateFlow(MusicState())
        every { musicController.musicState } returns musicStateFlow
        togglePlayPauseUseCase = TogglePlayPauseUseCase(musicController)
    }

    @Nested
    @DisplayName("當目前正在播放時")
    inner class WhenCurrentlyPlaying {

        @Test
        @DisplayName("應該呼叫 pause()")
        fun `should call pause`() {
            // Given
            musicStateFlow.value = MusicState(isPlaying = true)

            // When
            togglePlayPauseUseCase()

            // Then
            verify(exactly = 1) { musicController.pause() }
            verify(exactly = 0) { musicController.resume() }
        }
    }

    @Nested
    @DisplayName("當目前已暫停時")
    inner class WhenCurrentlyPaused {

        @Test
        @DisplayName("應該呼叫 resume()")
        fun `should call resume`() {
            // Given
            musicStateFlow.value = MusicState(isPlaying = false)

            // When
            togglePlayPauseUseCase()

            // Then
            verify(exactly = 1) { musicController.resume() }
            verify(exactly = 0) { musicController.pause() }
        }
    }

    @Nested
    @DisplayName("多次切換時")
    inner class WhenTogglingMultipleTimes {

        @Test
        @DisplayName("應該交替呼叫 pause 和 resume")
        fun `should alternate between pause and resume`() {
            // Given - 初始為播放中
            musicStateFlow.value = MusicState(isPlaying = true)

            // When - 第一次切換 (播放中 -> 暫停)
            togglePlayPauseUseCase()

            // Then
            verify(exactly = 1) { musicController.pause() }

            // Given - 模擬狀態變更為暫停
            musicStateFlow.value = MusicState(isPlaying = false)

            // When - 第二次切換 (暫停 -> 播放)
            togglePlayPauseUseCase()

            // Then
            verify(exactly = 1) { musicController.resume() }
        }
    }

    @Nested
    @DisplayName("狀態邊界情況")
    inner class EdgeCases {

        @Test
        @DisplayName("當正在緩衝且播放時應該暫停")
        fun `should pause when buffering and playing`() {
            // Given
            musicStateFlow.value = MusicState(
                isPlaying = true,
                isBuffering = true
            )

            // When
            togglePlayPauseUseCase()

            // Then
            verify(exactly = 1) { musicController.pause() }
        }

        @Test
        @DisplayName("當有重複模式設定時應該正常切換")
        fun `should toggle correctly with repeat mode set`() {
            // Given
            musicStateFlow.value = MusicState(
                isPlaying = true,
                repeatMode = RepeatMode.ALL
            )

            // When
            togglePlayPauseUseCase()

            // Then
            verify(exactly = 1) { musicController.pause() }
        }
    }
}
