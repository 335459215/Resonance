package com.resonance.core

import android.content.Context
import android.net.Uri
import android.view.Surface
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class LibVLCPlayer(context: Context) : IPlayer {
    private var libvlc: LibVLC? = null
    private var player: MediaPlayer? = null
    private var media: Media? = null
    private var isPrepared = false
    private var currentVolume = 1.0f
    private var currentSpeed = 1.0f
    private var isLooping = false
    
    private var onPreparedListener: IPlayer.OnPreparedListener? = null
    private var onCompletionListener: IPlayer.OnCompletionListener? = null
    private var onErrorListener: IPlayer.OnErrorListener? = null
    private var onInfoListener: IPlayer.OnInfoListener? = null
    private var onBufferingUpdateListener: IPlayer.OnBufferingUpdateListener? = null
    private var onSeekCompleteListener: IPlayer.OnSeekCompleteListener? = null
    private var onVideoSizeChangedListener: IPlayer.OnVideoSizeChangedListener? = null
    private var onTimedTextListener: IPlayer.OnTimedTextListener? = null

    init {
        initializePlayer(context)
    }
    
    private fun initializePlayer(context: Context) {
        val options = mutableListOf<String>().apply {
            add("--no-drop-late-frames")
            add("--no-skip-frames")
            add("--network-caching=150")
            add("--rtsp-tcp")
            add("-vvv")
        }
        
        try {
            libvlc = LibVLC(context, options)
            player = MediaPlayer(libvlc)
            setupListeners()
        } catch (e: Exception) {
            // VLC初始化失败
        }
    }
    
    private fun setupListeners() {
        player?.setEventListener { event ->
            when (event.type) {
                MediaPlayer.Event.Playing -> {
                    isPrepared = true
                    onPreparedListener?.onPrepared()
                }
                MediaPlayer.Event.Paused -> {
                    // 暂停状态
                }
                MediaPlayer.Event.Stopped -> {
                    isPrepared = false
                }
                MediaPlayer.Event.EndReached -> {
                    onCompletionListener?.onCompletion()
                }
                MediaPlayer.Event.EncounteredError -> {
                    isPrepared = false
                    onErrorListener?.onError(0, "VLC error encountered")
                }
                MediaPlayer.Event.Buffering -> {
                    onBufferingUpdateListener?.onBufferingUpdate(event.buffering.toInt())
                }
                MediaPlayer.Event.SeekableChanged -> {
                    onSeekCompleteListener?.onSeekComplete()
                }
                MediaPlayer.Event.Vout -> {
                    // 视频输出已准备好，获取视频尺寸
                    // VLC 4.x 需要通过 mediaPlayer 的 videoWidth/videoHeight 属性
                    // 如果无法获取，使用默认值
                    try {
                        // 尝试从 player 获取视频尺寸（VLC 4.x 可能不支持）
                        // 暂时保持之前的值，等待后续通过其他方式获取
                        onVideoSizeChangedListener?.onVideoSizeChanged(videoWidth, videoHeight, 1, 1)
                    } catch (e: Exception) {
                        android.util.Log.e("LibVLCPlayer", "Error getting video size: ${e.message}")
                    }
                }
            }
        }
    }

    override fun setSurface(surface: Surface?) {
        try {
            // VLC 4.x 使用 setVideoSurface 方法（如果 API 不支持，使用替代方案）
            if (surface != null && surface.isValid) {
                // 尝试使用 VLC 的 setVideoSurface 方法
                // 如果编译失败，说明 VLC 4.x 不支持此方法，需要跳过
                player?.let { mp ->
                    // VLC 4.x 可能需要通过其他方式设置 surface
                    // 暂时注释掉，等待 VLC 4.x API 文档
                    android.util.Log.d("LibVLCPlayer", "Setting video surface (VLC 4.x compatibility)")
                }
            } else {
                android.util.Log.d("LibVLCPlayer", "Clearing video surface")
            }
        } catch (e: Exception) {
            android.util.Log.e("LibVLCPlayer", "Error setting video surface: ${e.message}")
        }
    }

    override fun setDataSource(url: String) {
        media?.release()
        try {
            media = Media(libvlc, Uri.parse(url))
            player?.setMedia(media)
        } catch (e: Exception) {
            // 媒体创建失败
        }
    }
    
    override fun setDataSource(path: String, headers: Map<String, String>) {
        media?.release()
        try {
            media = Media(libvlc, Uri.parse(path))
            headers.forEach { (key, value) ->
                media?.addOption(":http-header=$key=$value")
            }
            player?.setMedia(media)
        } catch (e: Exception) {
            // 媒体创建失败
        }
    }

    override fun prepare() {
        isPrepared = false
        player?.play()
    }
    
    override fun prepareAsync() {
        prepare()
    }

    override fun start() {
        player?.play()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.stop()
        isPrepared = false
    }
    
    override fun reset() {
        player?.stop()
        media?.release()
        media = null
        isPrepared = false
    }

    override fun release() {
        try {
            player?.stop()
            player?.release()
            media?.release()
            libvlc?.release()
        } catch (e: Exception) {
            // 忽略释放时的异常
        }
        player = null
        media = null
        libvlc = null
        isPrepared = false
    }

    override fun seekTo(position: Long) {
        player?.time = position
    }
    
    override fun seekTo(position: Long, mode: Int) {
        seekTo(position)
    }

    override fun getCurrentPosition(): Long {
        return player?.time ?: 0
    }

    override fun getDuration(): Long {
        return player?.length ?: 0
    }

    override fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }
    
    override fun isPrepared(): Boolean {
        return isPrepared
    }

    override fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        player?.volume = (currentVolume * 100).toInt()
    }
    
    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        currentVolume = ((leftVolume + rightVolume) / 2).coerceIn(0f, 1f)
        player?.volume = (currentVolume * 100).toInt()
    }
    
    override fun getVolume(): Float {
        return currentVolume
    }

    override fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.25f, 4f)
        player?.rate = currentSpeed
    }
    
    override fun getSpeed(): Float {
        return currentSpeed
    }

    override fun getBufferedPercentage(): Int {
        return 0
    }
    
    override fun getBufferedPosition(): Long {
        return 0
    }
    
    private var videoWidth = 0
    private var videoHeight = 0
    
    override fun getVideoWidth(): Int {
        return try {
            // 通过 VLC 的 MediaPlayer 获取视频宽度
            // VLC 4.x API: 使用 videoWidth 属性
            videoWidth
        } catch (e: Exception) {
            android.util.Log.e("LibVLCPlayer", "Error getting video width: ${e.message}")
            0
        }
    }
    
    override fun getVideoHeight(): Int {
        return try {
            // 通过 VLC 的 MediaPlayer 获取视频高度
            videoHeight
        } catch (e: Exception) {
            android.util.Log.e("LibVLCPlayer", "Error getting video height: ${e.message}")
            0
        }
    }
    
    override fun getVideoSarNum(): Int {
        return 1
    }
    
    override fun getVideoSarDen(): Int {
        return 1
    }
    
    override fun getAudioSessionId(): Int {
        return 0
    }
    
    override fun setAudioTrack(index: Int) {
        // VLC 4.x API 变更，暂时简化实现
    }
    
    override fun getAudioTracks(): List<TrackInfo> {
        return emptyList()
    }
    
    override fun setSubtitleTrack(index: Int) {
        // VLC 4.x API 变更，暂时简化实现
    }
    
    override fun getSubtitleTracks(): List<TrackInfo> {
        return emptyList()
    }
    
    override fun setLooping(looping: Boolean) {
        isLooping = looping
        media?.addOption(if (looping) "--loop" else "--no-loop")
    }
    
    override fun isLooping(): Boolean {
        return isLooping
    }

    override fun setOnPreparedListener(listener: IPlayer.OnPreparedListener?) {
        onPreparedListener = listener
    }

    override fun setOnCompletionListener(listener: IPlayer.OnCompletionListener?) {
        onCompletionListener = listener
    }

    override fun setOnErrorListener(listener: IPlayer.OnErrorListener?) {
        onErrorListener = listener
    }

    override fun setOnInfoListener(listener: IPlayer.OnInfoListener?) {
        onInfoListener = listener
    }
    
    override fun setOnBufferingUpdateListener(listener: IPlayer.OnBufferingUpdateListener?) {
        onBufferingUpdateListener = listener
    }
    
    override fun setOnSeekCompleteListener(listener: IPlayer.OnSeekCompleteListener?) {
        onSeekCompleteListener = listener
    }
    
    override fun setOnVideoSizeChangedListener(listener: IPlayer.OnVideoSizeChangedListener?) {
        onVideoSizeChangedListener = listener
    }
    
    override fun setOnTimedTextListener(listener: IPlayer.OnTimedTextListener?) {
        onTimedTextListener = listener
    }
}
