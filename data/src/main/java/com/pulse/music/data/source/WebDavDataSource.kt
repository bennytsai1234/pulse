package com.pulse.music.data.source

import com.pulse.music.domain.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.InputStream
import javax.inject.Inject

class WebDavDataSource @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    // Basic WebDAV implementation using OkHttp
    
    suspend fun getStream(url: String, offset: Long = 0): InputStream? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("Range", "bytes=$offset-")
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.byteStream()
            } else {
                response.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getMetadata(url: String): WebDavMetadata? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .build()
                
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                WebDavMetadata(
                    contentType = response.header("Content-Type"),
                    contentLength = response.header("Content-Length")?.toLongOrNull() ?: 0,
                    lastModified = response.header("Last-Modified")
                )
            } else {
                response.close()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

data class WebDavMetadata(
    val contentType: String?,
    val contentLength: Long,
    val lastModified: String?
)
