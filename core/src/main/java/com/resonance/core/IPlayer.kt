package com.resonance.core

import android.view.Surface

interface IPlayer {
    fun setSurface(surface: Surface?)
    fun setDataSource(url: String)
    fun setDataSource(path: String, headers: Map<String, String>)
    fun prepare()
    fun prepareAsync()
    fun start()
    fun pause()
    fun stop()
    fun release()
    fun reset()
    fun seekTo(position: Long)
    fun seekTo(position: Long, mode: Int)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun isPlaying(): Boolean
    fun isPrepared(): Boolean
    fun setVolume(volume: Float)
    fun setVolume(leftVolume: Float, rightVolume: Float)
    fun getVolume(): Float
    fun setSpeed(speed: Float)
    fun getSpeed(): Float
    fun getBufferedPercentage(): Int
    fun getBufferedPosition(): Long
    fun getVideoWidth(): Int
    fun getVideoHeight(): Int
    fun getVideoSarNum(): Int
    fun getVideoSarDen(): Int
    fun getAudioSessionId(): Int
    fun setAudioTrack(index: Int)
    fun getAudioTracks(): List<TrackInfo>
    fun setSubtitleTrack(index: Int)
    fun getSubtitleTracks(): List<TrackInfo>
    fun setLooping(looping: Boolean)
    fun isLooping(): Boolean
    
    fun setOnPreparedListener(listener: OnPreparedListener?)
    fun setOnCompletionListener(listener: OnCompletionListener?)
    fun setOnErrorListener(listener: OnErrorListener?)
    fun setOnInfoListener(listener: OnInfoListener?)
    fun setOnBufferingUpdateListener(listener: OnBufferingUpdateListener?)
    fun setOnSeekCompleteListener(listener: OnSeekCompleteListener?)
    fun setOnVideoSizeChangedListener(listener: OnVideoSizeChangedListener?)
    fun setOnTimedTextListener(listener: OnTimedTextListener?)
    
    interface OnPreparedListener {
        fun onPrepared()
    }
    
    interface OnCompletionListener {
        fun onCompletion()
    }
    
    interface OnErrorListener {
        fun onError(code: Int, message: String): Boolean
    }
    
    interface OnInfoListener {
        fun onInfo(code: Int, message: String)
    }
    
    interface OnBufferingUpdateListener {
        fun onBufferingUpdate(percent: Int)
    }
    
    interface OnSeekCompleteListener {
        fun onSeekComplete()
    }
    
    interface OnVideoSizeChangedListener {
        fun onVideoSizeChanged(width: Int, height: Int, sarNum: Int, sarDen: Int)
    }
    
    interface OnTimedTextListener {
        fun onTimedText(text: String?)
    }
}

data class TrackInfo(
    val index: Int,
    val type: TrackType,
    val language: String?,
    val title: String?,
    val isDefault: Boolean,
    val isSelected: Boolean
)

enum class TrackType {
    AUDIO,
    SUBTITLE,
    VIDEO
}