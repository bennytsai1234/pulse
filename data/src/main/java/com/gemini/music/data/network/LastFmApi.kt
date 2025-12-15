package com.gemini.music.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Last.fm API 接口
 * 文檔: https://www.last.fm/api
 */
interface LastFmApi {
    
    /**
     * 獲取請求令牌 (OAuth 第一步)
     */
    @GET("2.0/")
    suspend fun getToken(
        @Query("method") method: String = "auth.getToken",
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): LastFmTokenResponse
    
    /**
     * 獲取 Session Key (OAuth 第三步，用戶授權後)
     */
    @GET("2.0/")
    suspend fun getSession(
        @Query("method") method: String = "auth.getSession",
        @Query("api_key") apiKey: String,
        @Query("token") token: String,
        @Query("api_sig") apiSig: String,
        @Query("format") format: String = "json"
    ): LastFmSessionResponse
    
    /**
     * Scrobble 一首歌曲
     */
    @FormUrlEncoded
    @POST("2.0/")
    suspend fun scrobble(
        @Field("method") method: String = "track.scrobble",
        @Field("api_key") apiKey: String,
        @Field("sk") sessionKey: String,
        @Field("artist") artist: String,
        @Field("track") track: String,
        @Field("album") album: String? = null,
        @Field("timestamp") timestamp: Long,
        @Field("api_sig") apiSig: String,
        @Field("format") format: String = "json"
    ): LastFmScrobbleResponse
    
    /**
     * 更新正在播放
     */
    @FormUrlEncoded
    @POST("2.0/")
    suspend fun updateNowPlaying(
        @Field("method") method: String = "track.updateNowPlaying",
        @Field("api_key") apiKey: String,
        @Field("sk") sessionKey: String,
        @Field("artist") artist: String,
        @Field("track") track: String,
        @Field("album") album: String? = null,
        @Field("api_sig") apiSig: String,
        @Field("format") format: String = "json"
    ): LastFmNowPlayingResponse
    
    /**
     * 獲取用戶資訊
     */
    @GET("2.0/")
    suspend fun getUserInfo(
        @Query("method") method: String = "user.getInfo",
        @Query("api_key") apiKey: String,
        @Query("sk") sessionKey: String,
        @Query("format") format: String = "json"
    ): LastFmUserResponse
}

// Response models
data class LastFmTokenResponse(
    val token: String?
)

data class LastFmSessionResponse(
    val session: LastFmSession?
)

data class LastFmSession(
    val name: String,
    val key: String,
    val subscriber: Int
)

data class LastFmScrobbleResponse(
    val scrobbles: Scrobbles?
)

data class Scrobbles(
    val scrobble: Any?, // Can be object or array
    @SerializedName("@attr") val attr: ScrobbleAttr?
)

data class ScrobbleAttr(
    val accepted: Int = 0,
    val ignored: Int = 0
)

data class LastFmNowPlayingResponse(
    val nowplaying: Any?
)

data class LastFmUserResponse(
    val user: LastFmUser?
)

data class LastFmUser(
    val name: String,
    val realname: String?,
    val url: String,
    val playcount: String?,
    val image: List<LastFmImage>?
)

data class LastFmImage(
    val size: String,
    @SerializedName("#text") val url: String
)

