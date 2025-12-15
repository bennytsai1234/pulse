package com.gemini.music.domain.repository

import com.gemini.music.domain.model.CustomEqPreset
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing custom EQ presets
 */
interface EqPresetRepository {
    
    /**
     * Get all custom presets
     */
    fun getAllPresets(): Flow<List<CustomEqPreset>>
    
    /**
     * Get a preset by ID
     */
    suspend fun getPresetById(id: Long): CustomEqPreset?
    
    /**
     * Save a new preset or update existing
     */
    suspend fun savePreset(preset: CustomEqPreset): Long
    
    /**
     * Delete a preset by ID
     */
    suspend fun deletePreset(id: Long)
    
    /**
     * Get total count of presets
     */
    suspend fun getPresetCount(): Int
}
