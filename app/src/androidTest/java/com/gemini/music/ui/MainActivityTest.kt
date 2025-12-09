package com.gemini.music.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.gemini.music.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * MainActivity UI 測試
 * 
 * 端到端測試驗證應用程式的主要 UI 元素和導航功能
 * 
 * 注意：這些測試需要在 Android 模擬器或實機上執行
 * 執行命令：gradlew :app:connectedDebugAndroidTest
 */
@HiltAndroidTest
class MainActivityTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun mainScreen_displaysBottomNavigation() {
        // 驗證底部導航欄存在
        composeTestRule.onNodeWithText("首頁").assertIsDisplayed()
    }

    @Test
    fun mainScreen_displaysAppTitle() {
        // 驗證應用程式標題或 Logo 存在
        // 根據您的實際 UI 調整這個測試
        composeTestRule.waitForIdle()
        // 這只是一個範例，實際的斷言需要根據您的 UI 調整
    }

    @Test
    fun homeScreen_scanMusicButton_isClickable() {
        // 等待 UI 加載
        composeTestRule.waitForIdle()
        
        // 這是一個範例測試，根據您的實際 UI 元素調整
        // composeTestRule.onNodeWithContentDescription("Scan Music").performClick()
    }

    // ===== 導航測試 =====

    @Test
    fun navigation_toSettingsScreen() {
        // 點擊設定按鈕並驗證設定畫面顯示
        composeTestRule.waitForIdle()
        
        // 根據您的實際導航結構調整
        // composeTestRule.onNodeWithContentDescription("Settings").performClick()
        // composeTestRule.onNodeWithText("設定").assertIsDisplayed()
    }

    // ===== MiniPlayer 測試 =====

    @Test
    fun miniPlayer_isHiddenWhenNoSongPlaying() {
        // 當沒有歌曲播放時，MiniPlayer 應該隱藏
        composeTestRule.waitForIdle()
        
        // 根據您的 MiniPlayer 實作調整
        // composeTestRule.onNodeWithTag("mini_player").assertDoesNotExist()
    }
}
