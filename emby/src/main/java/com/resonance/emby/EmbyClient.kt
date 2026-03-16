package com.resonance.emby

import android.content.Context
import com.resonance.emby.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class EmbyClient private constructor(private val context: Context) {
    private var api: EmbyApi? = null
    private var baseUrl: String = ""
    private var accessToken: String = ""
    private var userId: String = ""
    private var deviceId: String = ""
    private val isInitialized = AtomicBoolean(false)
    
    companion object {
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 60L
        private const val MAX_RETRIES = 3
        
        @Volatile
        private var instance: EmbyClient? = null

        fun getInstance(context: Context): EmbyClient {
            return instance ?: synchronized(this) {
                instance ?: EmbyClient(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    fun init(baseUrl: String, deviceId: String): Boolean {
        if (isInitialized.get()) {
            return true
        }
        
        this.baseUrl = baseUrl.trimEnd('/')
        this.deviceId = deviceId
        this.api = createApi()
        isInitialized.set(true)
        return true
    }
    
    fun isInitialized(): Boolean = isInitialized.get()

    private fun createApi(): EmbyApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(RetryInterceptor(MAX_RETRIES))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(EmbyApi::class.java)
    }
    
    private inner class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request().newBuilder()
                .addHeader("X-Emby-Authorization", "MediaBrowser Token=$accessToken")
                .addHeader("X-Emby-Device-Id", deviceId)
                .addHeader("X-Emby-Client", "EmbyPlayer")
                .addHeader("X-Emby-Client-Version", "1.0.0")
                .addHeader("X-Emby-Device-Name", android.os.Build.MODEL)
                .addHeader("X-Emby-Device-Manufacturer", android.os.Build.MANUFACTURER)
                .addHeader("X-Emby-Device-Model", android.os.Build.MODEL)
                .build()
            return chain.proceed(request)
        }
    }
    
    private inner class RetryInterceptor(private val maxRetries: Int) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            var response: Response? = null
            var exception: Exception? = null
            
            repeat(maxRetries) { attempt ->
                try {
                    response = chain.proceed(request)
                    if (response?.isSuccessful == true) {
                        return response!!
                    }
                } catch (e: Exception) {
                    exception = e
                }
                
                if (attempt < maxRetries - 1) {
                    Thread.sleep(1000L * (attempt + 1))
                }
            }
            
            return response ?: throw exception ?: Exception("Unknown error")
        }
    }

    suspend fun authenticate(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val request = AuthRequest(username, password)
                val response = currentApi.authenticate(request).execute()
                
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    this@EmbyClient.accessToken = authResponse.AccessToken
                    this@EmbyClient.userId = authResponse.User.Id
                    this@EmbyClient.api = createApi()
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception("Authentication failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Authentication error: ${e.message}"))
            }
        }
    }

    suspend fun getUserInfo(): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.getUserInfo(userId).execute()
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to get user info: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Get user info error: ${e.message}"))
            }
        }
    }

    suspend fun getMediaFolders(): Result<List<Item>> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.getMediaFolders(userId).execute()
                
                if (response.isSuccessful) {
                    Result.success(response.body()?.Items ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get media folders: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Get media folders error: ${e.message}"))
            }
        }
    }

    suspend fun getItems(parentId: String, recursive: Boolean = false, includeItemTypes: String? = null): Result<List<Item>> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.getItems(userId, parentId, recursive, includeItemTypes).execute()
                
                if (response.isSuccessful) {
                    Result.success(response.body()?.Items ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get items: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Get items error: ${e.message}"))
            }
        }
    }

    suspend fun getItemDetails(itemId: String): Result<ItemDetails> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.getItemDetails(userId, itemId).execute()
                
                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to get item details: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Get item details error: ${e.message}"))
            }
        }
    }

    suspend fun getPlaybackInfo(itemId: String, mediaSourceId: String? = null): Result<List<MediaSource>> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.getPlaybackInfo(itemId, userId, deviceId, mediaSourceId).execute()
                
                if (response.isSuccessful) {
                    Result.success(response.body()?.MediaSources ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get playback info: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Get playback info error: ${e.message}"))
            }
        }
    }

    suspend fun reportPlaying(playSessionId: String, itemId: String, mediaSourceId: String, positionTicks: Long, isPaused: Boolean, isMuted: Boolean, volumeLevel: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val request = PlaybackStatusRequest(
                    PlaySessionId = playSessionId,
                    ItemId = itemId,
                    SessionId = deviceId,
                    MediaSourceId = mediaSourceId,
                    PositionTicks = positionTicks,
                    IsPaused = isPaused,
                    IsMuted = isMuted,
                    VolumeLevel = volumeLevel
                )
                val response = currentApi.reportPlaying(request).execute()
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to report playing: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Report playing error: ${e.message}"))
            }
        }
    }

    suspend fun reportProgress(playSessionId: String, itemId: String, mediaSourceId: String, positionTicks: Long, isPaused: Boolean, volumeLevel: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val request = PlaybackProgressRequest(
                    PlaySessionId = playSessionId,
                    ItemId = itemId,
                    SessionId = deviceId,
                    MediaSourceId = mediaSourceId,
                    PositionTicks = positionTicks,
                    IsPaused = isPaused,
                    VolumeLevel = volumeLevel,
                    Brightness = null
                )
                val response = currentApi.reportProgress(request).execute()
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to report progress: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Report progress error: ${e.message}"))
            }
        }
    }

    suspend fun reportStopped(playSessionId: String, itemId: String, mediaSourceId: String, positionTicks: Long, isPaused: Boolean, isMuted: Boolean, volumeLevel: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val request = PlaybackStatusRequest(
                    PlaySessionId = playSessionId,
                    ItemId = itemId,
                    SessionId = deviceId,
                    MediaSourceId = mediaSourceId,
                    PositionTicks = positionTicks,
                    IsPaused = isPaused,
                    IsMuted = isMuted,
                    VolumeLevel = volumeLevel
                )
                val response = currentApi.reportStopped(request).execute()
                
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Failed to report stopped: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Report stopped error: ${e.message}"))
            }
        }
    }

    suspend fun search(searchTerm: String): Result<List<Item>> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.search(userId, searchTerm).execute()
                
                if (response.isSuccessful) {
                    Result.success(response.body()?.Items ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to search: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Search error: ${e.message}"))
            }
        }
    }

    suspend fun getSubtitles(itemId: String): Result<List<Subtitle>> {
        return withContext(Dispatchers.IO) {
            try {
                val currentApi = api ?: return@withContext Result.failure(Exception("Client not initialized"))
                val response = currentApi.getSubtitles(itemId, userId).execute()
                
                if (response.isSuccessful) {
                    Result.success(response.body()?.Subtitles ?: emptyList())
                } else {
                    Result.failure(Exception("Failed to get subtitles: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Get subtitles error: ${e.message}"))
            }
        }
    }
    
    fun getPlaybackUrl(itemId: String, mediaSourceId: String?, static: Boolean = false): String {
        val staticParam = if (static) "&Static=true" else ""
        val mediaSourceParam = mediaSourceId?.let { "&MediaSourceId=$it" } ?: ""
        return "$baseUrl/Videos/$itemId/stream?api_key=$accessToken$staticParam$mediaSourceParam"
    }

    fun getAccessToken(): String = accessToken
    fun getUserId(): String = userId
    fun getBaseUrl(): String = baseUrl
    
    fun logout() {
        accessToken = ""
        userId = ""
        api = createApi()
    }
}