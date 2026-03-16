package com.resonance.core

import android.content.Context
import android.view.Surface
import androidx.media3.common.util.UnstableApi
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * 播放器异常类
 */
class PlayerException(message: String, cause: Throwable? = null) : Exception(message, cause)

@UnstableApi
class PlayerManager private constructor(private val context: Context) {
    enum class PlayerType {
        MEDIA3,
        IJK,
        VLC
    }

    private val lock = ReentrantLock()
    private val currentPlayerRef = AtomicReference<IPlayer?>(null)
    private val currentTypeRef = AtomicReference<PlayerType?>(null)
    
    private var _currentPlayer: IPlayer?
        get() = currentPlayerRef.get()
        set(value) { currentPlayerRef.set(value) }
    
    private var _currentType: PlayerType?
        get() = currentTypeRef.get()
        set(value) { currentTypeRef.set(value) }

    fun createPlayer(type: PlayerType): IPlayer {
        return lock.withLock {
            releaseCurrentPlayerInternal()
            val player = when (type) {
                PlayerType.MEDIA3 -> Media3Player(context)
                PlayerType.IJK -> IjkPlayerImpl()
                PlayerType.VLC -> LibVLCPlayer(context)
            }
            _currentPlayer = player
            _currentType = type
            player
        }
    }

    fun getCurrentPlayer(): IPlayer? {
        return _currentPlayer
    }
    
    fun getCurrentPlayerOrThrow(): IPlayer {
        return _currentPlayer ?: throw PlayerException("Player not initialized. Call createPlayer() first.")
    }

    fun getCurrentType(): PlayerType? {
        return _currentType
    }

    fun releaseCurrentPlayer() {
        lock.withLock {
            releaseCurrentPlayerInternal()
        }
    }
    
    private fun releaseCurrentPlayerInternal() {
        _currentPlayer?.let {
            try {
                it.release()
            } catch (e: Exception) {
                // 忽略释放时的异常
            }
        }
        _currentPlayer = null
        _currentType = null
    }

    fun switchPlayer(type: PlayerType, surface: Surface?, dataSource: String?, headers: Map<String, String>? = null): IPlayer {
        return lock.withLock {
            releaseCurrentPlayerInternal()
            val newPlayer = when (type) {
                PlayerType.MEDIA3 -> Media3Player(context)
                PlayerType.IJK -> IjkPlayerImpl()
                PlayerType.VLC -> LibVLCPlayer(context)
            }
            _currentPlayer = newPlayer
            _currentType = type
            
            surface?.let { newPlayer.setSurface(it) }
            if (dataSource != null) {
                if (headers != null && headers.isNotEmpty()) {
                    newPlayer.setDataSource(dataSource, headers)
                } else {
                    newPlayer.setDataSource(dataSource)
                }
            }
            newPlayer
        }
    }
    
    fun switchToNextAvailablePlayer(surface: Surface?, dataSource: String?): IPlayer? {
        val current = _currentType ?: return null
        val types = PlayerType.values()
        val currentIndex = types.indexOf(current)
        
        for (i in 1..types.size) {
            val nextIndex = (currentIndex + i) % types.size
            val nextType = types[nextIndex]
            if (nextType != current) {
                return try {
                    switchPlayer(nextType, surface, dataSource)
                } catch (e: Exception) {
                    continue
                }
            }
        }
        return null
    }
    
    fun isPlayerAvailable(): Boolean {
        return _currentPlayer != null
    }
    
    fun getPlayerState(): PlayerState {
        val player = _currentPlayer ?: return PlayerState.IDLE
        return when {
            player.isPlaying() -> PlayerState.PLAYING
            player.isPrepared() -> PlayerState.PREPARED
            else -> PlayerState.IDLE
        }
    }

    enum class PlayerState {
        IDLE,
        PREPARING,
        PREPARED,
        PLAYING,
        PAUSED,
        COMPLETED,
        ERROR
    }

    companion object {
        @Volatile
        private var instance: PlayerManager? = null

        fun getInstance(context: Context): PlayerManager {
            return instance ?: synchronized(this) {
                instance ?: PlayerManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
        
        fun destroyInstance() {
            instance?.let {
                it.releaseCurrentPlayer()
            }
            instance = null
        }
    }
}