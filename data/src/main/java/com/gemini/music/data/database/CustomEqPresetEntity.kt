package com.gemini.music.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 自定義 EQ 預設的資料庫實體
 */
@Entity(tableName = "custom_eq_presets")
data class CustomEqPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bandLevels: String, // Comma-separated millibel values
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 轉換為 Domain Model
 */
fun CustomEqPresetEntity.asDomainModel() = com.gemini.music.domain.model.CustomEqPreset(
    id = id,
    name = name,
    bandLevels = bandLevels.split(",").mapNotNull { it.toIntOrNull() },
    bassBoostStrength = bassBoostStrength,
    virtualizerStrength = virtualizerStrength,
    loudnessGain = loudnessGain,
    createdAt = createdAt
)

/**
 * Domain Model 轉換為 Entity
 */
fun com.gemini.music.domain.model.CustomEqPreset.asEntity() = CustomEqPresetEntity(
    id = id,
    name = name,
    bandLevels = bandLevels.joinToString(","),
    bassBoostStrength = bassBoostStrength,
    virtualizerStrength = virtualizerStrength,
    loudnessGain = loudnessGain,
    createdAt = createdAt
)
