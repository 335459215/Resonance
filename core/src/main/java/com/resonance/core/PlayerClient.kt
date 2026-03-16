package com.resonance.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.view.Surface
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

@UnstableApi
class PlayerClient(private val context: Context) {
    private var serviceMessenger: Messenger? = null
    private val clientMessenger = Messenger(object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            handleServiceMessage(msg)
        }
    })

    private val _statusFlow = MutableSharedFlow<PlayerService.PlayerStatus>()
    val statusFlow: SharedFlow<PlayerService.PlayerStatus> = _statusFlow

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow: SharedFlow<String> = _errorFlow

    private var isBound = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceMessenger = null
            isBound = false
        }
    }

    fun bindService() {
        if (!isBound) {
            val intent = Intent(context, PlayerService::class.java)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbindService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }

    suspend fun initPlayer(playerType: PlayerManager.PlayerType = PlayerManager.PlayerType.MEDIA3): Boolean {
        return sendMessageWithResponse(
            what = PlayerService.MSG_INIT,
            bundle = Bundle().apply {
                putInt(PlayerService.KEY_PLAYER_TYPE, playerType.ordinal)
            }
        )
    }

    fun setSurface(surface: Surface?) {
        sendMessage(
            what = PlayerService.MSG_SET_SURFACE,
            bundle = Bundle().apply {
                putParcelable(PlayerService.KEY_SURFACE, surface)
            }
        )
    }

    fun setDataSource(url: String) {
        sendMessage(
            what = PlayerService.MSG_SET_DATA_SOURCE,
            bundle = Bundle().apply {
                putString(PlayerService.KEY_DATA_SOURCE, url)
            }
        )
    }

    suspend fun prepare(): Boolean {
        return sendMessageWithResponse(PlayerService.MSG_PREPARE)
    }

    fun start() {
        sendMessage(PlayerService.MSG_START)
    }

    fun pause() {
        sendMessage(PlayerService.MSG_PAUSE)
    }

    fun stop() {
        sendMessage(PlayerService.MSG_STOP)
    }

    fun seekTo(position: Long) {
        sendMessage(
            what = PlayerService.MSG_SEEK_TO,
            bundle = Bundle().apply {
                putLong(PlayerService.KEY_POSITION, position)
            }
        )
    }

    fun setVolume(volume: Float) {
        sendMessage(
            what = PlayerService.MSG_SET_VOLUME,
            bundle = Bundle().apply {
                putFloat(PlayerService.KEY_VOLUME, volume)
            }
        )
    }

    fun setSpeed(speed: Float) {
        sendMessage(
            what = PlayerService.MSG_SET_SPEED,
            bundle = Bundle().apply {
                putFloat(PlayerService.KEY_SPEED, speed)
            }
        )
    }

    fun release() {
        sendMessage(PlayerService.MSG_RELEASE)
    }

    suspend fun getStatus(): PlayerService.PlayerStatus? {
        val response = CompletableDeferred<PlayerService.PlayerStatus?>()
        val message = Message.obtain(null, PlayerService.MSG_GET_STATUS)
        message.replyTo = Messenger(object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val bundle = msg.data
                val status = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(PlayerService.KEY_STATUS, PlayerService.PlayerStatus::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelable(PlayerService.KEY_STATUS)
                }
                response.complete(status)
            }
        })
        serviceMessenger?.send(message)
        return response.await()
    }

    private fun sendMessage(what: Int, bundle: Bundle? = null) {
        if (isBound && serviceMessenger != null) {
            val message = Message.obtain(null, what)
            bundle?.let { message.data = it }
            message.replyTo = clientMessenger
            try {
                serviceMessenger?.send(message)
            } catch (e: RemoteException) {
                // 服务可能已崩溃，尝试重新绑定
                unbindService()
                bindService()
            }
        }
    }

    private suspend fun sendMessageWithResponse(what: Int, bundle: Bundle? = null): Boolean {
        val response = CompletableDeferred<Boolean>()
        val message = Message.obtain(null, what)
        bundle?.let { message.data = it }
        message.replyTo = Messenger(object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val bundle = msg.data
                val success = bundle.getBoolean(PlayerService.KEY_SUCCESS, false)
                val errorMessage = bundle.getString(PlayerService.KEY_MESSAGE, "")
                if (!success && errorMessage.isNotEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        _errorFlow.emit(errorMessage)
                    }
                }
                response.complete(success)
            }
        })
        serviceMessenger?.send(message)
        return response.await()
    }

    private fun handleServiceMessage(msg: Message) {
        when (msg.what) {
            PlayerService.MSG_GET_STATUS -> {
                val bundle = msg.data
                val status = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(PlayerService.KEY_STATUS, PlayerService.PlayerStatus::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelable(PlayerService.KEY_STATUS)
                }
                status?.let {
                    CoroutineScope(Dispatchers.Main).launch {
                        _statusFlow.emit(it)
                    }
                }
            }
        }
    }

    companion object {
        @Volatile
        private var instance: PlayerClient? = null

        fun getInstance(context: Context): PlayerClient {
            return instance ?: synchronized(this) {
                instance ?: PlayerClient(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}