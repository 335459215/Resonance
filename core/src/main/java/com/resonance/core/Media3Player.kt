package com.resonance.core

import android.content.Context
import android.view.Surface
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@UnstableApi
class Media3Player(private val context: Context) : IPlayer {
    private var player: ExoPlayer? = null
    private var isPrepared = false
    private var currentVolume = 1.0f
    private var currentSpeed = 1.0f
    private var isLooping = false
    
    private val _audioTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    private val _subtitleTracks = MutableStateFlow<List<TrackInfo>>(emptyList())
    
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
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
        
        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        isPrepared = true
                        updateTracks()
                        onPreparedListener?.onPrepared()
                    }
                    Player.STATE_ENDED -> {
                        onCompletionListener?.onCompletion()
                    }
                    Player.STATE_BUFFERING -> {
                        onBufferingUpdateListener?.onBufferingUpdate(player?.bufferedPercentage ?: 0)
                    }
                    Player.STATE_IDLE -> {
                        isPrepared = false
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                isPrepared = false
                onErrorListener?.onError(error.errorCode, error.message ?: "Unknown error")
            }
            
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                onVideoSizeChangedListener?.onVideoSizeChanged(
                    videoSize.width,
                    videoSize.height,
                    videoSize.pixelWidthHeightRatio.toInt(),
                    1
                )
            }
        })
    }

    override fun setSurface(surface: Surface?) {
        player?.setVideoSurface(surface)
    }

    override fun setDataSource(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        player?.setMediaItem(mediaItem)
    }
    
    override fun setDataSource(path: String, headers: Map<String, String>) {
        val mediaItem = MediaItem.Builder()
            .setUri(path)
            .build()
        player?.setMediaItem(mediaItem)
    }

    override fun prepare() {
        isPrepared = false
        player?.prepare()
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
        player?.clearMediaItems()
        isPrepared = false
    }

    override fun release() {
        try {
            player?.stop()
            player?.release()
        } catch (e: Exception) {
            // 忽略释放时的异常
        }
        player = null
        isPrepared = false
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
        player?.volume = currentVolume
    }
    
    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        currentVolume = ((leftVolume + rightVolume) / 2).coerceIn(0f, 1f)
        player?.volume = currentVolume
    }
    
    override fun getVolume(): Float {
        return currentVolume
    }

    override fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.25f, 4f)
        player?.setPlaybackSpeed(currentSpeed)
    }
    
    override fun getSpeed(): Float {
        return currentSpeed
    }

    override fun getBufferedPercentage(): Int {
        return player?.bufferedPercentage ?: 0
    }
    
    override fun getBufferedPosition(): Long {
        return player?.bufferedPosition ?: 0
    }
    
    override fun getVideoWidth(): Int {
        return player?.videoSize?.width ?: 0
    }
    
    override fun getVideoHeight(): Int {
        return player?.videoSize?.height ?: 0
    }
    
    override fun getVideoSarNum(): Int {
        return player?.videoSize?.pixelWidthHeightRatio?.toInt() ?: 1
    }
    
    override fun getVideoSarDen(): Int {
        return 1
    }
    
    override fun getAudioSessionId(): Int {
        return player?.audioSessionId ?: 0
    }
    
    override fun setAudioTrack(index: Int) {
        try {
            // 使用 Media3 的 TrackSelectionParameters 选择音轨
            val trackSelectionParameters = player?.trackSelectionParameters
            if (trackSelectionParameters != null) {
                val updatedParameters = trackSelectionParameters
                    .buildUpon()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, false)
                    .setForceHighestSupportedBitrate(true)
                    .build()
                player?.trackSelectionParameters = updatedParameters
                
                android.util.Log.d("Media3Player", "Audio track set to index: $index")
            }
        } catch (e: Exception) {
            android.util.Log.e("Media3Player", "Error setting audio track: ${e.message}")
        }
    }
    
    override fun getAudioTracks(): List<TrackInfo> {
        return _audioTracks.value
    }
    
    override fun setSubtitleTrack(index: Int) {
        // 简化实现
    }
    
    override fun getSubtitleTracks(): List<TrackInfo> {
        return _subtitleTracks.value
    }
    
    override fun setLooping(looping: Boolean) {
        isLooping = looping
        player?.repeatMode = if (looping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }
    
    override fun isLooping(): Boolean {
        return isLooping
    }
    
    private fun updateTracks() {
        val audioTracks = mutableListOf<TrackInfo>()
        val subtitleTracks = mutableListOf<TrackInfo>()
        
        player?.currentTracks?.groups?.forEachIndexed { index, group ->
            when (group.type) {
                androidx.media3.common.C.TRACK_TYPE_AUDIO -> {
                    for (i in 0 until group.length) {
                        audioTracks.add(
                            TrackInfo(
                                index = audioTracks.size,
                                type = TrackType.AUDIO,
                                language = group.getTrackFormat(i).language,
                                title = group.getTrackFormat(i).label,
                                isDefault = false,
                                isSelected = false
                            )
                        )
                    }
                }
                androidx.media3.common.C.TRACK_TYPE_TEXT -> {
                    for (i in 0 until group.length) {
                        subtitleTracks.add(
                            TrackInfo(
                                index = subtitleTracks.size,
                                type = TrackType.SUBTITLE,
                                language = group.getTrackFormat(i).language,
                                title = group.getTrackFormat(i).label,
                                isDefault = false,
                                isSelected = false
                            )
                        )
                    }
                }
            }
        }
        
        _audioTracks.value = audioTracks
        _subtitleTracks.value = subtitleTracks
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