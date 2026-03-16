package com.resonance.tv

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import com.resonance.core.PlayerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class TVManager private constructor(private val context: Context) {
    private val audioManager by lazy { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    private val handler = Handler(Looper.getMainLooper())
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private val keyEventBuffer = ConcurrentHashMap<Int, Long>()
    private val debounceThreshold = 300

    private val hardwareDecodeWhitelist = setOf(
        "Xiaomi",
        "Skyworth",
        "TCL",
        "Dangbei",
        "XGIMI"
    )

    private val keyMapping = mapOf(
        KeyEvent.KEYCODE_DPAD_UP to "UP",
        KeyEvent.KEYCODE_DPAD_DOWN to "DOWN",
        KeyEvent.KEYCODE_DPAD_LEFT to "LEFT",
        KeyEvent.KEYCODE_DPAD_RIGHT to "RIGHT",
        KeyEvent.KEYCODE_ENTER to "ENTER",
        KeyEvent.KEYCODE_BACK to "BACK",
        KeyEvent.KEYCODE_MENU to "MENU",
        KeyEvent.KEYCODE_INFO to "INFO",
        KeyEvent.KEYCODE_VOLUME_UP to "VOLUME_UP",
        KeyEvent.KEYCODE_VOLUME_DOWN to "VOLUME_DOWN",
        KeyEvent.KEYCODE_MUTE to "MUTE",
        KeyEvent.KEYCODE_CHANNEL_UP to "CHANNEL_UP",
        KeyEvent.KEYCODE_CHANNEL_DOWN to "CHANNEL_DOWN"
    )

    fun handleKeyEvent(event: KeyEvent, onKeyAction: (String) -> Unit): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        val keyCode = event.keyCode
        val currentTime = System.currentTimeMillis()
        val lastTime = keyEventBuffer[keyCode]

        if (lastTime != null && currentTime - lastTime < debounceThreshold) {
            return true
        }

        keyEventBuffer[keyCode] = currentTime

        val action = keyMapping[keyCode]
        if (action != null) {
            onKeyAction(action)
            return true
        }

        return false
    }

    fun isHardwareDecodeSupported(): Boolean {
        val manufacturer = Build.MANUFACTURER
        return hardwareDecodeWhitelist.contains(manufacturer)
    }

    fun enableHdmiAudioPassthrough() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun getRecommendedPlayerType(): PlayerManager.PlayerType {
        return if (isHardwareDecodeSupported()) {
            PlayerManager.PlayerType.MEDIA3
        } else {
            PlayerManager.PlayerType.IJK
        }
    }

    fun getOptimalBufferSize(): Int {
        val totalMemory = Runtime.getRuntime().totalMemory() / (1024 * 1024)
        return when {
            totalMemory <= 1024 -> 12 * 1024 * 1024
            totalMemory <= 2048 -> 24 * 1024 * 1024
            else -> 48 * 1024 * 1024
        }
    }

    fun getTvDeviceType(): TvDeviceType {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> TvDeviceType.XIAOMI
            manufacturer.contains("skyworth") -> TvDeviceType.SKYWORTH
            manufacturer.contains("tcl") -> TvDeviceType.TCL
            manufacturer.contains("dangbei") -> TvDeviceType.DANGBEI
            manufacturer.contains("xgimi") -> TvDeviceType.XGIMI
            else -> TvDeviceType.GENERIC
        }
    }

    fun applyDeviceSpecificOptimizations() {
        val deviceType = getTvDeviceType()
        when (deviceType) {
            TvDeviceType.XIAOMI -> applyXiaomiOptimizations()
            TvDeviceType.SKYWORTH -> applySkyworthOptimizations()
            TvDeviceType.TCL -> applyTclOptimizations()
            TvDeviceType.DANGBEI -> applyDangbeiOptimizations()
            TvDeviceType.XGIMI -> applyXgimiOptimizations()
            else -> applyGenericOptimizations()
        }
    }

    private fun applyXiaomiOptimizations() {}
    private fun applySkyworthOptimizations() {}
    private fun applyTclOptimizations() {}
    private fun applyDangbeiOptimizations() {}
    private fun applyXgimiOptimizations() {}
    private fun applyGenericOptimizations() {}

    fun setupFocusManagement(view: View) {
        view.isFocusable = true
        view.isFocusableInTouchMode = true
    }

    fun cleanup() {
        executor.shutdown()
    }

    enum class TvDeviceType {
        XIAOMI,
        SKYWORTH,
        TCL,
        DANGBEI,
        XGIMI,
        GENERIC
    }

    companion object {
        private const val CACHE_KEY_PREFIX = "tv_setting_"

        @Volatile
        private var instance: TVManager? = null

        fun getInstance(context: Context): TVManager {
            return instance ?: synchronized(this) {
                instance ?: TVManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
