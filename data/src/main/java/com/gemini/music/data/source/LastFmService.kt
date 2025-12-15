package com.gemini.music.data.source

import android.util.Log
import com.gemini.music.data.network.LastFmApi
import com.gemini.music.data.network.LastFmSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Last.fm 服務層
 * 處理 API 認證、簽名生成和 Scrobbling
 */
@Singleton
class LastFmService @Inject constructor(
    private val lastFmApi: LastFmApi
) {
    companion object {
        // TODO: 替換為實際的 API Key 和 Secret
        // 用戶需要在 https://www.last.fm/api/account/create 建立應用
        const val API_KEY = "YOUR_API_KEY"
        const val API_SECRET = "YOUR_API_SECRET"
        
        private const val TAG = "LastFmService"
    }
    
    /**
     * 獲取授權 Token
     */
    suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        try {
            val response = lastFmApi.getToken(apiKey = API_KEY)
            response.token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get auth token", e)
            null
        }
    }
    
    /**
     * 生成授權 URL
     * 用戶需要在瀏覽器中打開此 URL 進行授權
     */
    fun getAuthUrl(token: String): String {
        return "https://www.last.fm/api/auth/?api_key=$API_KEY&token=$token"
    }
    
    /**
     * 用 Token 獲取 Session
     * 需要在用戶完成網頁授權後調用
     */
    suspend fun getSession(token: String): LastFmSession? = withContext(Dispatchers.IO) {
        try {
            val sig = generateSignature(
                mapOf(
                    "api_key" to API_KEY,
                    "method" to "auth.getSession",
                    "token" to token
                )
            )
            val response = lastFmApi.getSession(
                apiKey = API_KEY,
                token = token,
                apiSig = sig
            )
            response.session
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get session", e)
            null
        }
    }
    
    /**
     * Scrobble 一首歌曲
     */
    suspend fun scrobble(
        sessionKey: String,
        artist: String,
        track: String,
        album: String?,
        timestamp: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf(
                "api_key" to API_KEY,
                "artist" to artist,
                "method" to "track.scrobble",
                "sk" to sessionKey,
                "timestamp" to timestamp.toString(),
                "track" to track
            )
            album?.let { params["album"] = it }
            
            val sig = generateSignature(params)
            
            val response = lastFmApi.scrobble(
                apiKey = API_KEY,
                sessionKey = sessionKey,
                artist = artist,
                track = track,
                album = album,
                timestamp = timestamp,
                apiSig = sig
            )
            
            val accepted = response.scrobbles?.attr?.accepted ?: 0
            accepted > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to scrobble", e)
            false
        }
    }
    
    /**
     * 更新正在播放
     */
    suspend fun updateNowPlaying(
        sessionKey: String,
        artist: String,
        track: String,
        album: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val params = mutableMapOf(
                "api_key" to API_KEY,
                "artist" to artist,
                "method" to "track.updateNowPlaying",
                "sk" to sessionKey,
                "track" to track
            )
            album?.let { params["album"] = it }
            
            val sig = generateSignature(params)
            
            lastFmApi.updateNowPlaying(
                apiKey = API_KEY,
                sessionKey = sessionKey,
                artist = artist,
                track = track,
                album = album,
                apiSig = sig
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update now playing", e)
            false
        }
    }
    
    /**
     * 獲取用戶資訊
     */
    suspend fun getUserInfo(sessionKey: String): String? = withContext(Dispatchers.IO) {
        try {
            val response = lastFmApi.getUserInfo(
                apiKey = API_KEY,
                sessionKey = sessionKey
            )
            response.user?.name
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user info", e)
            null
        }
    }
    
    /**
     * 生成 API 簽名
     * 按照 Last.fm 文檔: 將所有參數按字母順序排序，拼接為 key1value1key2value2...，
     * 然後附加 API Secret，最後計算 MD5
     */
    private fun generateSignature(params: Map<String, String>): String {
        val sortedParams = params.toSortedMap()
        val signatureBase = buildString {
            sortedParams.forEach { (key, value) ->
                append(key)
                append(value)
            }
            append(API_SECRET)
        }
        return md5(signatureBase)
    }
    
    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }
}
