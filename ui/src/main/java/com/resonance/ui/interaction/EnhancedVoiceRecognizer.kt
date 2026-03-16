package com.resonance.ui.interaction

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 增强的语音识别器
 * 支持错误重试、权限检查、智能退避
 */
class EnhancedVoiceRecognizer(private val context: Context) {
    
    companion object {
        private const val TAG = "EnhancedVoiceRecognizer"
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY = 1000L // 1 秒
        private const val MAX_RETRY_DELAY = 5000L // 5 秒
    }
    
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()
    
    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var retryCount = 0
    private var isDestroying = false
    
    private val scope: CoroutineScope = MainScope()
    
    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            setupSpeechRecognizer()
        } else {
            _error.value = "设备不支持语音识别"
        }
    }
    
    private fun setupSpeechRecognizer() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "准备就绪")
                _isListening.value = true
                _transcript.value = ""
                _error.value = null
            }
            
            override fun onBeginningOfSpeech() {
                Log.d(TAG, "开始说话")
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // 音量变化
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // 接收到音频数据
            }
            
            override fun onEndOfSpeech() {
                Log.d(TAG, "说话结束")
                _isListening.value = false
                retryCount = 0
            }
            
            override fun onError(error: Int) {
                _isListening.value = false
                handleError(error)
            }
            
            override fun onResults(results: Bundle?) {
                Log.d(TAG, "识别成功")
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _transcript.value = matches[0]
                    retryCount = 0
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
    
    private fun handleError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "音频录制错误"
            SpeechRecognizer.ERROR_CLIENT -> "客户端错误"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                requestPermission()
                "权限不足，已请求权限"
            }
            SpeechRecognizer.ERROR_NETWORK -> {
                // 网络错误，尝试重试
                retryWithBackoff()
                "网络错误，正在重试..."
            }
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                retryWithBackoff()
                "网络超时，正在重试..."
            }
            SpeechRecognizer.ERROR_NO_MATCH -> "未识别到语音"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别服务忙"
            SpeechRecognizer.ERROR_SERVER -> "服务器错误"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "未检测到语音，请重试"
            else -> "未知错误：$error"
        }
        
        Log.e(TAG, "错误：$errorMessage")
        _error.value = errorMessage
    }
    
    private fun retryWithBackoff() {
        if (retryCount >= MAX_RETRIES || isDestroying) return
        
        retryCount++
        val delay = (INITIAL_RETRY_DELAY * retryCount).coerceAtMost(MAX_RETRY_DELAY)
        
        Log.d(TAG, "将在 ${delay}ms 后重试 ($retryCount/$MAX_RETRIES)")
        
        scope.launch {
            delay(delay)
            if (!isDestroying) {
                startListening()
            }
        }
    }
    
    /**
     * 开始监听（带重试）
     */
    fun startListening(language: String = "zh-CN") {
        if (isDestroying) {
            Log.w(TAG, "识别器已销毁，无法开始监听")
            return
        }
        
        if (!hasPermission()) {
            requestPermission()
            _error.value = "缺少录音权限"
            return
        }
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            _error.value = null
        } catch (e: Exception) {
            Log.e(TAG, "启动语音识别失败", e)
            _error.value = "启动失败：${e.message}"
        }
    }
    
    /**
     * 停止监听
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }
    
    /**
     * 取消监听
     */
    fun cancel() {
        speechRecognizer?.cancel()
        _isListening.value = false
        retryCount = 0
    }
    
    /**
     * 销毁识别器
     */
    fun destroy() {
        isDestroying = true
        cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
    
    /**
     * 检查是否有录音权限
     */
    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * 请求录音权限
     */
    fun requestPermission() {
        // 实际使用时需要通过 Activity 或 Fragment 请求权限
        // 这里只是提示
        Log.w(TAG, "需要 RECORD_AUDIO 权限")
    }
    
    /**
     * 获取重试次数
     */
    fun getRetryCount(): Int = retryCount
    
    /**
     * 重置错误状态
     */
    fun resetError() {
        _error.value = null
        retryCount = 0
    }
}
