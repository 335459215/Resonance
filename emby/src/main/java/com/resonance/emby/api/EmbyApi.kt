package com.resonance.emby.api

import retrofit2.Call
import retrofit2.http.*

interface EmbyApi {
    // 认证
    @POST("/Users/AuthenticateByName")
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>

    // 获取用户信息
    @GET("/Users/{userId}")
    fun getUserInfo(@Path("userId") userId: String): Call<UserInfo>

    // 获取媒体库
    @GET("/Users/{userId}/Items")
    fun getMediaFolders(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String = "",
        @Query("Recursive") recursive: Boolean = false,
        @Query("IncludeItemTypes") includeItemTypes: String = "Folder"
    ): Call<ItemsResponse>

    // 获取媒体项目
    @GET("/Users/{userId}/Items")
    fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String,
        @Query("Recursive") recursive: Boolean = false,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("Limit") limit: Int? = null,
        @Query("Offset") offset: Int? = null,
        @Query("SortBy") sortBy: String? = null,
        @Query("SortOrder") sortOrder: String? = null
    ): Call<ItemsResponse>

    // 获取项目详情
    @GET("/Users/{userId}/Items/{itemId}")
    fun getItemDetails(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String,
        @Query("Fields") fields: String = "MediaStreams,Audio,Subtitle,Path,Played,Overview"
    ): Call<ItemDetails>

    // 获取播放信息
    @GET("/Items/{itemId}/PlaybackInfo")
    fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Query("DeviceId") deviceId: String,
        @Query("MediaSourceId") mediaSourceId: String? = null
    ): Call<PlaybackInfoResponse>

    // 报告播放状态
    @POST("/Sessions/Playing")
    fun reportPlaying(@Body request: PlaybackStatusRequest): Call<Void>

    // 报告播放进度
    @POST("/Sessions/Playing/Progress")
    fun reportProgress(@Body request: PlaybackProgressRequest): Call<Void>

    // 报告播放完成
    @POST("/Sessions/Playing/Stopped")
    fun reportStopped(@Body request: PlaybackStatusRequest): Call<Void>

    // 搜索
    @GET("/Users/{userId}/Items")
    fun search(
        @Path("userId") userId: String,
        @Query("SearchTerm") searchTerm: String,
        @Query("Limit") limit: Int = 50,
        @Query("IncludeItemTypes") includeItemTypes: String = "Movie,Series,Episode,MusicArtist,MusicAlbum,MusicVideo"
    ): Call<ItemsResponse>

    // 获取字幕
    @GET("/Items/{itemId}/Subtitles")
    fun getSubtitles(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String
    ): Call<SubtitlesResponse>
}

// 认证请求
data class AuthRequest(
    val Username: String,
    val Pw: String
)

// 认证响应
data class AuthResponse(
    val User: UserInfo,
    val AccessToken: String
)

// 用户信息
data class UserInfo(
    val Id: String,
    val Name: String,
    val ServerId: String
)

// 项目响应
data class ItemsResponse(
    val Items: List<Item>
)

// 项目
data class Item(
    val Id: String,
    val Name: String,
    val Type: String,
    val ParentId: String?,
    val PrimaryImageTag: String?,
    val BackdropImageTags: List<String>?,
    val MediaType: String?,
    val RunTimeTicks: Long?,
    val Played: Boolean?,
    val PlayCount: Int?
)

// 项目详情
data class ItemDetails(
    val Id: String,
    val Name: String,
    val Type: String,
    val MediaSources: List<MediaSource>,
    val MediaStreams: List<MediaStream>,
    val RunTimeTicks: Long,
    val Played: Boolean,
    val Overview: String?
)

// 媒体源
data class MediaSource(
    val Id: String,
    val Name: String,
    val Path: String,
    val Protocol: String,
    val TranscodingUrl: String?,
    val DirectStreamUrl: String?,
    val DirectPlayUrl: String?,
    val MediaStreams: List<MediaStream>,
    val Width: Int?,
    val Height: Int?,
    val Bitrate: Int?,
    val Container: String
)

// 媒体流
data class MediaStream(
    val Index: Int,
    val Type: String,
    val Codec: String,
    val Language: String?,
    val Title: String?,
    val IsDefault: Boolean,
    val IsForced: Boolean,
    val Width: Int?,
    val Height: Int?
)

// 播放信息响应
data class PlaybackInfoResponse(
    val MediaSources: List<MediaSource>
)

// 播放状态请求
data class PlaybackStatusRequest(
    val PlaySessionId: String,
    val ItemId: String,
    val SessionId: String,
    val MediaSourceId: String,
    val PositionTicks: Long,
    val IsPaused: Boolean,
    val IsMuted: Boolean,
    val VolumeLevel: Int
)

// 播放进度请求
data class PlaybackProgressRequest(
    val PlaySessionId: String,
    val ItemId: String,
    val SessionId: String,
    val MediaSourceId: String,
    val PositionTicks: Long,
    val IsPaused: Boolean,
    val VolumeLevel: Int,
    val Brightness: Int?
)

// 字幕响应
data class SubtitlesResponse(
    val Subtitles: List<Subtitle>
)

// 字幕
data class Subtitle(
    val Id: String,
    val Name: String,
    val Language: String,
    val Format: String,
    val Url: String
)