package com.sigma.music.core.util

import android.net.Uri
import android.os.Environment

object StorageUtils {
    fun getPathFromUri(uri: Uri): String? {
        val path = uri.path ?: return null
        // Expected format for Primary Storage: /tree/primary:Music
        // We want to convert "primary:Music" -> "/storage/emulated/0/Music"
        
        if (path.contains("primary:")) {
            val relativePath = path.substringAfter("primary:")
            return "${Environment.getExternalStorageDirectory().absolutePath}/$relativePath"
        }
        
        // Fallback or other SD cards could be handled here, but for MVP we focus on Primary
        return null
    }
}
