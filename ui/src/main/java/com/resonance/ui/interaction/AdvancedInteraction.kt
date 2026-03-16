package com.resonance.ui.interaction

import android.content.Context
import android.content.Intent
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 高级交互工具类
 * 提供手势操作、语音控制、智能提示等功能
 */
object AdvancedInteraction {
    
    // ==================== 语音识别 ====================
    
    /**
     * 语音识别管理器
     */
    class VoiceRecognizer(private val context: Context) {
        
        private var speechRecognizer: SpeechRecognizer? = null
        private val _isListening = MutableStateFlow(false)
        val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
        
        private val _transcript = MutableStateFlow("")
        val transcript: StateFlow<String> = _transcript.asStateFlow()
        
        init {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                setupSpeechRecognizer()
            }
        }
        
        private fun setupSpeechRecognizer() {
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _transcript.value = ""
                }
                
                override fun onBeginningOfSpeech() {
                    // 开始说话
                }
                
                override fun onRmsChanged(rmsdB: Float) {
                    // 音量变化
                }
                
                override fun onBufferReceived(buffer: ByteArray?) {
                    // 接收到音频数据
                }
                
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }
                
                override fun onError(error: Int) {
                    _isListening.value = false
                    // 处理错误
                }
                
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _transcript.value = matches[0]
                    }
                    _isListening.value = false
                }
                
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        _transcript.value = matches[0]
                    }
                }
                
                override fun onEvent(eventType: Int, params: Bundle?) {
                    // 保留事件
                }
            })
        }
        
        fun startListening(language: String = "zh-CN") {
            if (speechRecognizer == null) return
            
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            
            try {
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                // 处理异常
            }
        }
        
        fun stopListening() {
            speechRecognizer?.stopListening()
            _isListening.value = false
        }
        
        fun cancel() {
            speechRecognizer?.cancel()
            _isListening.value = false
        }
        
        fun destroy() {
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
    }
    
    // ==================== 智能提示系统 ====================
    
    /**
     * 智能提示管理器
     */
    class SmartTipsManager {
        
        private val tipHistory = mutableListOf<String>()
        private val userPreferences = mutableMapOf<String, Boolean>()
        
        /**
         * 显示智能提示
         */
        fun showTip(
            context: Context,
            tipId: String,
            message: String,
            maxShowCount: Int = 3
        ): Boolean {
            // 检查是否已显示过多次
            val showCount = tipHistory.count { it == tipId }
            if (showCount >= maxShowCount) {
                return false
            }
            
            // 检查用户是否已禁用
            if (userPreferences[tipId] == false) {
                return false
            }
            
            // 记录显示历史
            tipHistory.add(tipId)
            
            // 显示提示（实际使用时替换为 Toast 或其他 UI）
            // InteractionOptimizer.showInfoToast(context, message)
            
            return true
        }
        
        /**
         * 禁用某个提示
         */
        fun disableTip(tipId: String) {
            userPreferences[tipId] = false
        }
        
        /**
         * 重置提示历史
         */
        fun resetHistory() {
            tipHistory.clear()
        }
        
        /**
         * 获取提示显示次数
         */
        fun getTipShowCount(tipId: String): Int {
            return tipHistory.count { it == tipId }
        }
    }
    
    // ==================== 高级手势操作 ====================
    
    /**
     * 双指缩放和旋转手势
     */
    fun Modifier.zoomAndRotateGesture(
        onZoom: (scale: Float) -> Unit = {},
        onRotate: (rotation: Float) -> Unit = {},
        onPan: (offset: androidx.compose.ui.geometry.Offset) -> Unit = {}
    ): Modifier = composed {
        var lastZoom = 1f
        var lastRotation = 0f
        var lastPanOffset = androidx.compose.ui.geometry.Offset.Zero
        
        this.pointerInput(Unit) {
            detectTransformGestures { centroid, pan, zoom, rotation ->
                lastZoom *= zoom
                lastRotation += rotation
                lastPanOffset = pan
                
                onZoom(lastZoom)
                onRotate(lastRotation)
                onPan(lastPanOffset)
            }
        }
    }
    
    /**
     * 长按拖动快捷菜单
     */
    fun Modifier.longPressDragMenu(
        onLongPress: () -> Unit,
        onDrag: (offset: androidx.compose.ui.geometry.Offset) -> Unit,
        onRelease: () -> Unit
    ): Modifier = composed {
        this.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    onLongPress()
                },
                onTap = { }
            )
        }.pointerInput(Unit) {
            detectDragGestures(
                onDrag = { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                },
                onDragEnd = {
                    onRelease()
                }
            )
        }
    }
    
    // ==================== 触觉反馈增强 ====================
    
    /**
     * 连续触觉反馈（用于滚动选择）
     */
    fun performContinuousHaptic(view: View, count: Int, delay: Long = 50L) {
        MainScope().launch {
            repeat(count) {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                delay(delay)
            }
        }
    }
}

/**
 * Composable 函数：获取语音识别器
 */
@Composable
fun rememberVoiceRecognizer(): AdvancedInteraction.VoiceRecognizer {
    val context = LocalContext.current
    return remember { AdvancedInteraction.VoiceRecognizer(context) }
}

/**
 * Composable 函数：获取智能提示管理器
 */
@Composable
fun rememberSmartTipsManager(): AdvancedInteraction.SmartTipsManager {
    return remember { AdvancedInteraction.SmartTipsManager() }
}
