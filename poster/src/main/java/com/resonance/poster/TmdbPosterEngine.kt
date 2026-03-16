package com.resonance.poster

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.resonance.poster.api.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class TmdbPosterEngine private constructor(private val context: Context) {
    private lateinit var tmdbApi: TmdbApi
    private var apiKey: String = ""
    private var imageBaseUrl: String = ""
    private val cacheDir by lazy { File(context.cacheDir, "posters") }

    fun init(apiKey: String) {
        this.apiKey = apiKey
        this.tmdbApi = createApi()
        cacheDir.mkdirs()
        fetchConfiguration()
    }

    private fun createApi(): TmdbApi {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(TmdbApi::class.java)
    }

    private fun fetchConfiguration() {
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = tmdbApi.getConfiguration(apiKey).execute()
                if (response.isSuccessful) {
                    response.body()?.let {
                        imageBaseUrl = it.images.secure_base_url
                    }
                }
            } catch (e: Exception) {
                imageBaseUrl = "https://image.tmdb.org/t/p/"
            }
        }
    }

    suspend fun getPosterUrl(title: String, year: Int?, mediaType: String): String? {
        val cacheKey = generateCacheKey(title, year, mediaType)
        val cachedUrl = getFromCache(cacheKey)
        if (cachedUrl != null) {
            return cachedUrl
        }

        val posterUrl = withContext(Dispatchers.IO) {
            try {
                when (mediaType.lowercase()) {
                    "movie" -> searchMoviePoster(title, year)
                    "tv" -> searchTvPoster(title, year)
                    else -> null
                }
            } catch (e: Exception) {
                null
            }
        }

        posterUrl?.let {
            saveToCache(cacheKey, it)
        }

        return posterUrl
    }

    private suspend fun searchMoviePoster(title: String, year: Int?): String? {
        val response = tmdbApi.searchMovie(title, apiKey, year = year).execute()
        if (response.isSuccessful) {
            val results = response.body()?.results
            if (!results.isNullOrEmpty()) {
                val movieId = results[0].id
                val movieDetails = tmdbApi.getMovieDetails(movieId, apiKey).execute()
                if (movieDetails.isSuccessful) {
                    val posterPath = movieDetails.body()?.poster_path
                    if (!posterPath.isNullOrEmpty()) {
                        return buildPosterUrl(posterPath)
                    }
                }
            }
        }
        return null
    }

    private suspend fun searchTvPoster(title: String, year: Int?): String? {
        val response = tmdbApi.searchTv(title, apiKey, year = year).execute()
        if (response.isSuccessful) {
            val results = response.body()?.results
            if (!results.isNullOrEmpty()) {
                val tvId = results[0].id
                val tvDetails = tmdbApi.getTvDetails(tvId, apiKey).execute()
                if (tvDetails.isSuccessful) {
                    val posterPath = tvDetails.body()?.poster_path
                    if (!posterPath.isNullOrEmpty()) {
                        return buildPosterUrl(posterPath)
                    }
                }
            }
        }
        return null
    }

    private fun buildPosterUrl(posterPath: String): String {
        return "${imageBaseUrl}w500$posterPath"
    }

    private fun generateCacheKey(title: String, year: Int?, mediaType: String): String {
        return "${CACHE_KEY_PREFIX}${title}_${year}_${mediaType}"
    }

    private fun getFromCache(key: String): String? {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        return mmkv.decodeString(key)
    }

    private fun saveToCache(key: String, url: String) {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        mmkv.encode(key, url)
        val allKeys = mmkv.allKeys()
        allKeys?.let {
            if (it.size > MAX_CACHE_SIZE) {
                for (i in 0 until it.size - MAX_CACHE_SIZE) {
                    mmkv.removeValueForKey(it[i])
                }
            }
        }
    }

    fun clearCache() {
        val mmkv = com.tencent.mmkv.MMKV.defaultMMKV()
        val allKeys = mmkv.allKeys()
        allKeys?.forEach {
            if (it.startsWith(CACHE_KEY_PREFIX)) {
                mmkv.removeValueForKey(it)
            }
        }
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    @Suppress("DEPRECATION")
    private fun isNetworkConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    @Suppress("DEPRECATION")
    fun getNetworkQuality(): NetworkQuality {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return NetworkQuality.NO_NETWORK
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.NO_NETWORK
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkQuality.HIGH
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    NetworkQuality.MEDIUM
                }
                else -> NetworkQuality.LOW
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return NetworkQuality.NO_NETWORK
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkQuality.HIGH
                ConnectivityManager.TYPE_MOBILE -> NetworkQuality.MEDIUM
                else -> NetworkQuality.LOW
            }
        }
    }

    enum class NetworkQuality {
        NO_NETWORK,
        LOW,
        MEDIUM,
        HIGH
    }

    companion object {
        private const val CACHE_KEY_PREFIX = "tmdb_poster_"
        private const val MAX_CACHE_SIZE = 500
        private const val BASE_URL = "https://api.themoviedb.org"

        @Volatile
        private var instance: TmdbPosterEngine? = null

        fun getInstance(context: Context): TmdbPosterEngine {
            return instance ?: synchronized(this) {
                instance ?: TmdbPosterEngine(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
