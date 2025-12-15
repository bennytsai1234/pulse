package com.gemini.music.domain.model

/**
 * 自定義 EQ 預設
 */
data class CustomEqPreset(
    val id: Long = 0,
    val name: String,
    val bandLevels: List<Int>, // millibel values for each band
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0, // millibel
    val createdAt: Long = System.currentTimeMillis()
)
