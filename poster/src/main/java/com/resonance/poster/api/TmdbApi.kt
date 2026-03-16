package com.resonance.poster.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    // 搜索电影
    @GET("/3/search/movie")
    fun searchMovie(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN",
        @Query("year") year: Int? = null,
        @Query("page") page: Int = 1
    ): Call<MovieSearchResponse>

    // 搜索电视剧
    @GET("/3/search/tv")
    fun searchTv(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN",
        @Query("first_air_date_year") year: Int? = null,
        @Query("page") page: Int = 1
    ): Call<TvSearchResponse>

    // 获取电影详情
    @GET("/3/movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN",
        @Query("append_to_response") appendToResponse: String = "images"
    ): Call<MovieDetails>

    // 获取电视剧详情
    @GET("/3/tv/{tv_id}")
    fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "zh-CN",
        @Query("append_to_response") appendToResponse: String = "images"
    ): Call<TvDetails>

    // 获取配置信息（用于构建图片URL）
    @GET("/3/configuration")
    fun getConfiguration(
        @Query("api_key") apiKey: String
    ): Call<ConfigurationResponse>
}

// 电影搜索响应
data class MovieSearchResponse(
    val page: Int,
    val results: List<MovieResult>,
    val total_pages: Int,
    val total_results: Int
)

// 电视剧搜索响应
data class TvSearchResponse(
    val page: Int,
    val results: List<TvResult>,
    val total_pages: Int,
    val total_results: Int
)

// 电影结果
data class MovieResult(
    val adult: Boolean,
    val backdrop_path: String?,
    val genre_ids: List<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

// 电视剧结果
data class TvResult(
    val backdrop_path: String?,
    val first_air_date: String,
    val genre_ids: List<Int>,
    val id: Int,
    val name: String,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val vote_average: Double,
    val vote_count: Int
)

// 电影详情
data class MovieDetails(
    val adult: Boolean,
    val backdrop_path: String?,
    val budget: Int,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val imdb_id: String?,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val release_date: String,
    val revenue: Long,
    val runtime: Int?,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int,
    val images: ImagesResponse?
)

// 电视剧详情
data class TvDetails(
    val backdrop_path: String?,
    val created_by: List<Creator>,
    val episode_run_time: List<Int>,
    val first_air_date: String,
    val genres: List<Genre>,
    val homepage: String?,
    val id: Int,
    val in_production: Boolean,
    val languages: List<String>,
    val last_air_date: String,
    val name: String,
    val next_episode_to_air: Any?,
    val networks: List<Network>,
    val number_of_episodes: Int,
    val number_of_seasons: Int,
    val origin_country: List<String>,
    val original_language: String,
    val original_name: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String?,
    val production_companies: List<ProductionCompany>,
    val status: String,
    val tagline: String,
    val type: String,
    val vote_average: Double,
    val vote_count: Int,
    val images: ImagesResponse?
)

// 类型
data class Genre(
    val id: Int,
    val name: String
)

// 创建者
data class Creator(
    val id: Int,
    val credit_id: String,
    val name: String,
    val gender: Int,
    val profile_path: String?
)

// 网络
data class Network(
    val id: Int,
    val logo_path: String?,
    val name: String,
    val origin_country: String
)

// 制作公司
data class ProductionCompany(
    val id: Int,
    val logo_path: String?,
    val name: String,
    val origin_country: String
)

// 图片响应
data class ImagesResponse(
    val backdrops: List<Image>,
    val posters: List<Image>,
    val logos: List<Image>
)

// 图片
data class Image(
    val aspect_ratio: Double,
    val height: Int,
    val iso_639_1: String?,
    val file_path: String,
    val vote_average: Double,
    val vote_count: Int,
    val width: Int
)

// 配置响应
data class ConfigurationResponse(
    val images: ImageConfiguration
)

// 图片配置
data class ImageConfiguration(
    val base_url: String,
    val secure_base_url: String,
    val backdrop_sizes: List<String>,
    val logo_sizes: List<String>,
    val poster_sizes: List<String>,
    val profile_sizes: List<String>,
    val still_sizes: List<String>
)