package com.pulse.music.data.source

import android.content.Context
import com.pulse.music.domain.model.backup.BackupResult
import com.pulse.music.domain.model.backup.RestoreResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

import com.pulse.music.core.common.auth.GoogleAuthProvider
import com.google.api.client.http.javanet.NetHttpTransport

@Singleton
class GoogleDriveService @Inject constructor(
    @ApplicationContext private val context: Context
) : GoogleAuthProvider {

    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val transport = NetHttpTransport()

    // 備份檔案名稱
    private val BACKUP_FILE_NAME = "PULSE_music_backup.json"
    private val MIME_TYPE_JSON = "application/json"

    override fun getSignInIntent(): android.content.Intent {
        return getSignInClient().signInIntent
    }

    fun getSignInClient(): GoogleSignInClient {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, signInOptions)
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(transport, jsonFactory, credential)
            .setApplicationName("Pulse")
            .build()
    }

    suspend fun uploadBackup(account: GoogleSignInAccount, jsonContent: String): BackupResult = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(account)

            // 1. 查找舊備份
            val fileList = service.files().list()
                .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val content = ByteArrayContent.fromString(MIME_TYPE_JSON, jsonContent)

            if (fileList.files.isNotEmpty()) {
                // 更新現有檔案
                val fileId = fileList.files[0].id
                service.files().update(fileId, null, content).execute()
            } else {
                // 創建新檔案
                val fileMetadata = com.google.api.services.drive.model.File()
                fileMetadata.name = BACKUP_FILE_NAME
                fileMetadata.mimeType = MIME_TYPE_JSON
                // fileMetadata.parents = listOf("appDataFolder") // Use appDataFolder for hidden backup

                service.files().create(fileMetadata, content).execute()
            }

            BackupResult.Success
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult.Error(e.message ?: "Upload failed", e)
        }
    }

    suspend fun downloadBackup(account: GoogleSignInAccount): Pair<String?, RestoreResult> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService(account)

            val fileList = service.files().list()
                .setQ("name = '$BACKUP_FILE_NAME' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            if (fileList.files.isEmpty()) {
                return@withContext null to RestoreResult.Error("找不到備份檔案")
            }

            val fileId = fileList.files[0].id
            val inputStream = service.files().get(fileId).executeMediaAsInputStream()

            val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            content to RestoreResult.Success(0) // Count will be updated by repository
        } catch (e: Exception) {
            e.printStackTrace()
            null to RestoreResult.Error(e.message ?: "Download failed", e)
        }
    }
}
