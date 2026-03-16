package com.resonance.core

import android.app.Service
import android.content.Intent
import android.os.*
import android.view.Surface
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@UnstableApi
class PlayerService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var player: IPlayer? = null
    private var playerManager: PlayerManager? = null
    private val deathRecipient = object : IBinder.DeathRecipient {
        override fun binderDied() {
            // 客户端进程死亡，清理资源
            cleanup()
        }
    }

    private val messenger = Messenger(object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_INIT -> handleInit(msg)
                MSG_SET_SURFACE -> handleSetSurface(msg)
                MSG_SET_DATA_SOURCE -> handleSetDataSource(msg)
                MSG_PREPARE -> handlePrepare(msg)
                MSG_START -> handleStart(msg)
                MSG_PAUSE -> handlePause(msg)
                MSG_STOP -> handleStop(msg)
                MSG_SEEK_TO -> handleSeekTo(msg)
                MSG_SET_VOLUME -> handleSetVolume(msg)
                MSG_SET_SPEED -> handleSetSpeed(msg)
                MSG_RELEASE -> handleRelease(msg)
                MSG_GET_STATUS -> handleGetStatus(msg)
            }
        }
    })

    override fun onCreate() {
        super.onCreate()
        playerManager = PlayerManager.getInstance(this)
        // 启动崩溃监控
        startCrashMonitor()
    }

    override fun onBind(intent: Intent): IBinder {
        return messenger.binder.apply {
            linkToDeath(deathRecipient, 0)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        messenger.binder.unlinkToDeath(deathRecipient, 0)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        cleanup()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun handleInit(msg: Message) {
        val bundle = msg.data
        val playerType = bundle.getInt(KEY_PLAYER_TYPE, PlayerManager.PlayerType.MEDIA3.ordinal)
        serviceScope.launch {
            try {
                val type = PlayerManager.PlayerType.values()[playerType]
                player = playerManager?.createPlayer(type)
                setupPlayerListeners()
                sendResponse(msg.replyTo, MSG_INIT, true, "Player initialized successfully")
            } catch (e: Exception) {
                sendResponse(msg.replyTo, MSG_INIT, false, "Failed to initialize player: ${e.message}")
            }
        }
    }

    private fun handleSetSurface(msg: Message) {
        val surface = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            msg.data.getParcelable(KEY_SURFACE, Surface::class.java)
        } else {
            @Suppress("DEPRECATION")
            msg.data.getParcelable(KEY_SURFACE)
        }
        player?.setSurface(surface)
        sendResponse(msg.replyTo, MSG_SET_SURFACE, true, "Surface set successfully")
    }

    private fun handleSetDataSource(msg: Message) {
        val url = msg.data.getString(KEY_DATA_SOURCE)
        if (url != null) {
            player?.setDataSource(url)
            sendResponse(msg.replyTo, MSG_SET_DATA_SOURCE, true, "Data source set successfully")
        } else {
            sendResponse(msg.replyTo, MSG_SET_DATA_SOURCE, false, "Invalid data source")
        }
    }

    private fun handlePrepare(msg: Message) {
        serviceScope.launch {
            try {
                player?.prepare()
                sendResponse(msg.replyTo, MSG_PREPARE, true, "Player prepared successfully")
            } catch (e: Exception) {
                sendResponse(msg.replyTo, MSG_PREPARE, false, "Failed to prepare player: ${e.message}")
            }
        }
    }

    private fun handleStart(msg: Message) {
        player?.start()
        sendResponse(msg.replyTo, MSG_START, true, "Player started")
    }

    private fun handlePause(msg: Message) {
        player?.pause()
        sendResponse(msg.replyTo, MSG_PAUSE, true, "Player paused")
    }

    private fun handleStop(msg: Message) {
        player?.stop()
        sendResponse(msg.replyTo, MSG_STOP, true, "Player stopped")
    }

    private fun handleSeekTo(msg: Message) {
        val position = msg.data.getLong(KEY_POSITION, 0)
        player?.seekTo(position)
        sendResponse(msg.replyTo, MSG_SEEK_TO, true, "Seek completed")
    }

    private fun handleSetVolume(msg: Message) {
        val volume = msg.data.getFloat(KEY_VOLUME, 1.0f)
        player?.setVolume(volume)
        sendResponse(msg.replyTo, MSG_SET_VOLUME, true, "Volume set")
    }

    private fun handleSetSpeed(msg: Message) {
        val speed = msg.data.getFloat(KEY_SPEED, 1.0f)
        player?.setSpeed(speed)
        sendResponse(msg.replyTo, MSG_SET_SPEED, true, "Speed set")
    }

    private fun handleRelease(msg: Message) {
        cleanup()
        sendResponse(msg.replyTo, MSG_RELEASE, true, "Player released")
    }

    private fun handleGetStatus(msg: Message) {
        val status = PlayerStatus(
            isPlaying = player?.isPlaying() ?: false,
            currentPosition = player?.getCurrentPosition() ?: 0,
            duration = player?.getDuration() ?: 0,
            bufferedPercentage = player?.getBufferedPercentage() ?: 0
        )
        val bundle = Bundle().apply {
            putParcelable(KEY_STATUS, status)
        }
        sendResponse(msg.replyTo, MSG_GET_STATUS, true, "Status retrieved", bundle)
    }

    private fun setupPlayerListeners() {
        player?.setOnPreparedListener(object : IPlayer.OnPreparedListener {
            override fun onPrepared() {
                // 发送准备完成消息
            }
        })
        player?.setOnErrorListener(object : IPlayer.OnErrorListener {
            override fun onError(code: Int, message: String): Boolean {
                // 处理错误，可能需要切换内核
                return true
            }
        })
        player?.setOnCompletionListener(object : IPlayer.OnCompletionListener {
            override fun onCompletion() {
                // 发送完成消息
            }
        })
    }

    private fun startCrashMonitor() {
        // 启动崩溃监控，实现内核崩溃自愈
        serviceScope.launch {
            while (isActive) {
                delay(1000) // 每秒检查一次
                // 这里可以添加崩溃检测逻辑
            }
        }
    }

    private fun cleanup() {
        player?.release()
        player = null
        playerManager?.releaseCurrentPlayer()
    }

    private fun sendResponse(replyTo: Messenger?, what: Int, success: Boolean, message: String, bundle: Bundle? = null) {
        replyTo?.let {
            val response = Message.obtain(null, what)
            val responseBundle = bundle ?: Bundle()
            responseBundle.putBoolean(KEY_SUCCESS, success)
            responseBundle.putString(KEY_MESSAGE, message)
            response.data = responseBundle
            try {
                it.send(response)
            } catch (e: RemoteException) {
                // 客户端可能已断开
            }
        }
    }

    companion object {
        const val MSG_INIT = 1
        const val MSG_SET_SURFACE = 2
        const val MSG_SET_DATA_SOURCE = 3
        const val MSG_PREPARE = 4
        const val MSG_START = 5
        const val MSG_PAUSE = 6
        const val MSG_STOP = 7
        const val MSG_SEEK_TO = 8
        const val MSG_SET_VOLUME = 9
        const val MSG_SET_SPEED = 10
        const val MSG_RELEASE = 11
        const val MSG_GET_STATUS = 12

        const val KEY_PLAYER_TYPE = "player_type"
        const val KEY_SURFACE = "surface"
        const val KEY_DATA_SOURCE = "data_source"
        const val KEY_POSITION = "position"
        const val KEY_VOLUME = "volume"
        const val KEY_SPEED = "speed"
        const val KEY_SUCCESS = "success"
        const val KEY_MESSAGE = "message"
        const val KEY_STATUS = "status"
    }

    data class PlayerStatus(
        val isPlaying: Boolean,
        val currentPosition: Long,
        val duration: Long,
        val bufferedPercentage: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeByte(if (isPlaying) 1 else 0)
            parcel.writeLong(currentPosition)
            parcel.writeLong(duration)
            parcel.writeInt(bufferedPercentage)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PlayerStatus> {
            override fun createFromParcel(parcel: Parcel): PlayerStatus {
                return PlayerStatus(parcel)
            }

            override fun newArray(size: Int): Array<PlayerStatus?> {
                return arrayOfNulls(size)
            }
        }
    }
}