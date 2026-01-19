package com.pulse.music.domain.usecase.sync

import com.pulse.music.domain.repository.MusicRepository
import javax.inject.Inject

/**
 * UseCase for syncing metadata from remote WebDAV sources.
 *
 * Currently a placeholder for future implementation as WebDAV repository is not yet fully exposed to Domain.
 * In a real implementation, this would iterate through remote files and sync metadata to the local DB.
 */
class SyncWebDavMetadataUseCase @Inject constructor(
    private val musicRepository: MusicRepository
) {
    suspend operator fun invoke(remoteUrl: String) {
        // Implementation pending WebDAV repository interface in Domain layer.
        // For now, this is a no-op to satisfy the architectural skeleton.
        // Once WebDAV is integrated into MusicRepository or a new RemoteRepository,
        // we will implement the sync logic here.
    }
}
