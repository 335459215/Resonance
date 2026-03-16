package com.resonance.core

import android.content.Context
import android.view.Surface
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class IjkPlayerImpl : IPlayer {
    private var player: IjkMediaPlayer? = null
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
        initializePlayer()
    }
    
    private fun initializePlayer() {
        try {
            IjkMediaPlayer.loadLibrariesOnce(null)
            IjkMediaPlayer.native_profileBegin("libijkplayer.so")
        } catch (e: Exception) {
            android.util.Log.e("IjkPlayerImpl", "Failed to load IJK libraries: ${e.message}", e)
            throw RuntimeException("Failed to initialize IJK player", e)
        }
        
        player = IjkMediaPlayer()
        setupListeners()
    }
    
    private fun setupListeners() {
        player?.setOnPreparedListener {
            isPrepared = true
            onPreparedListener?.onPrepared()
        }
        
        player?.setOnCompletionListener {
            onCompletionListener?.onCompletion()
        }
        
        player?.setOnErrorListener { mp, what, extra ->
            isPrepared = false
            onErrorListener?.onError(what, "Error code: $what, extra: $extra") ?: false
        }
        
        player?.setOnInfoListener { mp, what, extra ->
            onInfoListener?.onInfo(what, "Info code: $what, extra: $extra")
            true
        }
        
        player?.setOnBufferingUpdateListener { mp, percent ->
            onBufferingUpdateListener?.onBufferingUpdate(percent)
        }
        
        player?.setOnSeekCompleteListener {
            onSeekCompleteListener?.onSeekComplete()
        }
        
        player?.setOnVideoSizeChangedListener { mp, width, height, sarNum, sarDen ->
            onVideoSizeChangedListener?.onVideoSizeChanged(width, height, sarNum, sarDen)
        }
        
        player?.setOnTimedTextListener { mp, text ->
            onTimedTextListener?.onTimedText(text?.text)
        }
    }

    override fun setSurface(surface: Surface?) {
        player?.setSurface(surface)
    }

    override fun setDataSource(url: String) {
        player?.setDataSource(url)
    }
    
    override fun setDataSource(path: String, headers: Map<String, String>) {
        player?.setDataSource(path, headers)
    }

    override fun prepare() {
        isPrepared = false
        player?.prepareAsync()
    }
    
    override fun prepareAsync() {
        prepare()
    }

    override fun start() {
        player?.start()
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.stop()
        isPrepared = false
    }
    
    override fun reset() {
        player?.reset()
        isPrepared = false
    }

    override fun release() {
        try {
            player?.stop()
            player?.release()
            IjkMediaPlayer.native_profileEnd()
        } catch (e: Exception) {
            android.util.Log.e("IjkPlayerImpl", "Error releasing player: ${e.message}", e)
        } finally {
            player = null
            isPrepared = false
        }
    }

    override fun seekTo(position: Long) {
        player?.seekTo(position)
    }
    
    override fun seekTo(position: Long, mode: Int) {
        player?.seekTo(position)
    }

    override fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0
    }

    override fun getDuration(): Long {
        return player?.duration ?: 0
    }

    override fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }
    
    override fun isPrepared(): Boolean {
        return isPrepared
    }

    override fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        player?.setVolume(currentVolume, currentVolume)
    }
    
    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        currentVolume = ((leftVolume + rightVolume) / 2).coerceIn(0f, 1f)
        player?.setVolume(leftVolume.coerceIn(0f, 1f), rightVolume.coerceIn(0f, 1f))
    }
    
    override fun getVolume(): Float {
        return currentVolume
    }

    override fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.25f, 4f)
        player?.setSpeed(currentSpeed)
    }
    
    override fun getSpeed(): Float {
        return currentSpeed
    }

    override fun getBufferedPercentage(): Int {
        return try {
            val meta = player?.getMediaMeta()
            if (meta != null) {
                val buffered = meta.getLong("buffered")
                val duration = player?.duration ?: 0
                if (duration > 0) {
                    ((buffered.toFloat() / duration) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }
            } else {
                0
            }
        } catch (e: Exception) {
            android.util.Log.e("IjkPlayerImpl", "Error getting buffered percentage: ${e.message}")
            0
        }
    }
    
    override fun getBufferedPosition(): Long {
        return try {
            val meta = player?.getMediaMeta()
            if (meta != null) {
                meta.getLong("buffered")
            } else {
                0L
            }
        } catch (e: Exception) {
            android.util.Log.e("IjkPlayerImpl", "Error getting buffered position: ${e.message}")
            0L
        }
    }
    
    override fun getVideoWidth(): Int {
        return player?.videoWidth ?: 0
    }
    
    override fun getVideoHeight(): Int {
        return player?.videoHeight ?: 0
    }
    
    override fun getVideoSarNum(): Int {
        return player?.videoSarNum ?: 1
    }
    
    override fun getVideoSarDen(): Int {
        return player?.videoSarDen ?: 1
    }
    
    override fun getAudioSessionId(): Int {
        return player?.audioSessionId ?: 0
    }
    
    override fun setAudioTrack(index: Int) {
        // ijkplayer 暂不支持此功能
    }
    
    override fun getAudioTracks(): List<TrackInfo> {
        return emptyList()
    }
    
    override fun setSubtitleTrack(index: Int) {
        // ijkplayer 暂不支持此功能
    }
    
    override fun getSubtitleTracks(): List<TrackInfo> {
        return emptyList()
    }
    
    override fun setLooping(looping: Boolean) {
        isLooping = looping
        player?.setLooping(looping)
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