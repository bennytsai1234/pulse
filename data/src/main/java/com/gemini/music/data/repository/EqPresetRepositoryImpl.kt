package com.gemini.music.data.repository

import com.gemini.music.data.database.CustomEqPresetDao
import com.gemini.music.data.database.asDomainModel
import com.gemini.music.data.database.asEntity
import com.gemini.music.domain.model.CustomEqPreset
import com.gemini.music.domain.repository.EqPresetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqPresetRepositoryImpl @Inject constructor(
    private val customEqPresetDao: CustomEqPresetDao
) : EqPresetRepository {
    
    override fun getAllPresets(): Flow<List<CustomEqPreset>> {
        return customEqPresetDao.getAllPresets().map { entities ->
            entities.map { it.asDomainModel() }
        }
    }
    
    override suspend fun getPresetById(id: Long): CustomEqPreset? {
        return customEqPresetDao.getPresetById(id)?.asDomainModel()
    }
    
    override suspend fun savePreset(preset: CustomEqPreset): Long {
        return customEqPresetDao.insertPreset(preset.asEntity())
    }
    
    override suspend fun deletePreset(id: Long) {
        customEqPresetDao.deletePresetById(id)
    }
    
    override suspend fun getPresetCount(): Int {
        return customEqPresetDao.getPresetCount()
    }
}
