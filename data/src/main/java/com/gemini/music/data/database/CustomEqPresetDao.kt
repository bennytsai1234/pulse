package com.gemini.music.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEqPresetDao {
    
    @Query("SELECT * FROM custom_eq_presets ORDER BY createdAt DESC")
    fun getAllPresets(): Flow<List<CustomEqPresetEntity>>
    
    @Query("SELECT * FROM custom_eq_presets WHERE id = :id")
    suspend fun getPresetById(id: Long): CustomEqPresetEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CustomEqPresetEntity): Long
    
    @Update
    suspend fun updatePreset(preset: CustomEqPresetEntity)
    
    @Delete
    suspend fun deletePreset(preset: CustomEqPresetEntity)
    
    @Query("DELETE FROM custom_eq_presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
    
    @Query("SELECT COUNT(*) FROM custom_eq_presets")
    suspend fun getPresetCount(): Int
}
