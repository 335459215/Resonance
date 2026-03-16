package com.resonance.data.model

/**
 * 媒体类型枚举
 */
enum class MediaType {
    MOVIE,      // 电影
    SERIES,     // 剧集
    MUSIC,      // 音乐
    ALBUM,      // 专辑
    ARTIST,     // 艺术家
    PLAYLIST,   // 歌单
    VIDEO,      // 视频
    UNKNOWN     // 未知
}

/**
 * 媒体项数据类
 * 统一的媒体数据模型
 */
data class MediaItem(
    val id: String,
    val name: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val mediaType: MediaType,
    val posterUrl: String? = null,
    val backdropUrl: String? = null,
    val rating: Float? = null,
    val releaseYear: Int? = null,
    val releaseDate: String? = null,
    val runTimeTicks: Long? = null,
    val parentId: String? = null,
    val serverId: String? = null,
    val isPlayed: Boolean = false,
    val playCount: Int = 0,
    val userData: MediaUserData? = null,
    val mediaSources: List<MediaSource>? = null,
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) {
    /**
     * 获取显示标题
     */
    fun getDisplayTitle(): String {
        return originalTitle ?: name
    }
    
    /**
     * 获取副标题（年份/专辑等）
     */
    fun getSubtitle(): String? {
        return releaseYear?.toString() ?: releaseDate
    }
    
    /**
     * 判断是否为视频类型
     */
    fun isVideo(): Boolean {
        return mediaType in listOf(MediaType.MOVIE, MediaType.SERIES, MediaType.VIDEO)
    }
    
    /**
     * 判断是否为音频类型
     */
    fun isAudio(): Boolean {
        return mediaType in listOf(MediaType.MUSIC, MediaType.ALBUM, MediaType.ARTIST, MediaType.PLAYLIST)
    }
}

/**
 * 媒体用户数据
 */
data class MediaUserData(
    val playedPercentage: Int? = null,
    val playbackPositionTicks: Long = 0,
    val isFavorite: Boolean = false,
    val lastPlayedDate: String? = null,
    val played: Boolean = false,
    val playCount: Int = 0
)

/**
 * 媒体源
 */
data class MediaSource(
    val id: String,
    val path: String,
    val container: String? = null,
    val size: Long? = null,
    val bitrate: Int? = null,
    val protocols: List<String>? = null
)

/**
 * 媒体项扩展属性
 */
val MediaItem.displayName: String
    get() = getDisplayTitle()

val MediaItem.subtitle: String?
    get() = getSubtitle()
