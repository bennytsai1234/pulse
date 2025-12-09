package com.gemini.music

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom Test Runner for Hilt
 * 
 * 使用 HiltTestApplication 作為測試應用程式類別，
 * 讓 Hilt 能在 UI 測試中正確注入依賴。
 * 
 * 設定方式：在 app/build.gradle.kts 中指定：
 * testInstrumentationRunner = "com.gemini.music.HiltTestRunner"
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
