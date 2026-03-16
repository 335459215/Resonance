package com.resonance.core

import android.content.Context
import android.os.Build
import android.os.Process
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object AppLogger {
    private const val TAG = "EmbyPlayer"
    private const val MAX_LOG_FILES = 5
    private const val MAX_LOG_SIZE = 5 * 1024 * 1024 // 5MB
    
    private var logDir: File? = null
    private var currentLogFile: File? = null
    private var executor: ScheduledExecutorService? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    @Volatile
    private var isInitialized = false
    
    private enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARN, ERROR
    }
    
    fun init(context: Context) {
        if (isInitialized) return
        
        logDir = File(context.filesDir, "logs").apply {
            if (!exists()) mkdirs()
        }
        
        executor = Executors.newSingleThreadScheduledExecutor()
        
        // 设置未捕获异常处理器
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(context))
        
        // 创建新的日志文件
        createNewLogFile()
        
        // 定期清理旧日志
        executor?.scheduleAtFixedRate({
            cleanOldLogs()
        }, 1, 24, TimeUnit.HOURS)
        
        isInitialized = true
        
        info("AppLogger initialized")
    }
    
    fun v(message: String) {
        log(LogLevel.VERBOSE, message)
    }
    
    fun d(message: String) {
        log(LogLevel.DEBUG, message)
    }
    
    fun i(message: String) {
        log(LogLevel.INFO, message)
    }
    
    fun w(message: String) {
        log(LogLevel.WARN, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, message, throwable)
    }
    
    fun verbose(message: String) = v(message)
    fun debug(message: String) = d(message)
    fun info(message: String) = i(message)
    fun warn(message: String) = w(message)
    fun error(message: String, throwable: Throwable? = null) = e(message, throwable)
    
    private fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val threadName = Thread.currentThread().name
        val logMessage = buildString {
            append("$timestamp [$threadName] ${level.name}/$TAG: $message")
            throwable?.let {
                append("\n")
                append(getStackTraceString(it))
            }
        }
        
        // 输出到控制台
        when (level) {
            LogLevel.VERBOSE -> android.util.Log.v(TAG, message)
            LogLevel.DEBUG -> android.util.Log.d(TAG, message)
            LogLevel.INFO -> android.util.Log.i(TAG, message)
            LogLevel.WARN -> android.util.Log.w(TAG, message)
            LogLevel.ERROR -> android.util.Log.e(TAG, message, throwable)
        }
        
        // 写入文件
        writeToFile(logMessage)
    }
    
    private fun writeToFile(message: String) {
        executor?.execute {
            try {
                currentLogFile?.let { file ->
                    if (file.length() > MAX_LOG_SIZE) {
                        createNewLogFile()
                    }
                    file.appendText("$message\n")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to write log to file", e)
            }
        }
    }
    
    private fun createNewLogFile() {
        val fileName = "log_${fileDateFormat.format(Date())}.txt"
        currentLogFile = File(logDir, fileName)
    }
    
    private fun cleanOldLogs() {
        logDir?.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?.drop(MAX_LOG_FILES)
            ?.forEach { it.delete() }
    }
    
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
    
    fun getLogFiles(): List<File> {
        return logDir?.listFiles()?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
    }
    
    fun clearLogs() {
        logDir?.listFiles()?.forEach { it.delete() }
        createNewLogFile()
    }
    
    fun flush() {
        executor?.shutdown()
        executor = Executors.newSingleThreadScheduledExecutor()
    }
    
    private class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {
        private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            // 记录崩溃信息
            val crashInfo = buildString {
                append("=== CRASH REPORT ===\n")
                append("Time: ${dateFormat.format(Date())}\n")
                append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                append("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})\n")
                append("App Version: ${getAppVersion()}\n")
                append("Process: ${Process.myPid()}\n")
                append("Thread: ${thread.name}\n")
                append("\n=== STACK TRACE ===\n")
                append(getStackTraceString(throwable))
                append("\n=== END ===\n")
            }
            
            // 写入崩溃日志
            try {
                val crashFile = File(context.filesDir, "crash_${System.currentTimeMillis()}.log")
                crashFile.writeText(crashInfo)
            } catch (e: Exception) {
                // 忽略
            }
            
            // 调用默认处理器
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        private fun getAppVersion(): String {
            return try {
                val packageInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(context.packageName, 0)
                }
                "${packageInfo.versionName} (${packageInfo.longVersionCode})"
            } catch (e: Exception) {
                "Unknown"
            }
        }
        
        private fun getStackTraceString(throwable: Throwable): String {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            return sw.toString()
        }
    }
}