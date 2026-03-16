package com.resonance.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

/**
 * Coil 图片加载扩展函数
 * 提供优化的图片加载方法
 */

/**
 * 加载带缓存优化的图片
 */
@Composable
fun AsyncImageWithCache(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderResId: Int? = null,
    errorResId: Int? = null,
    crossfadeEnabled: Boolean = true
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(crossfadeEnabled)
            .size(Size.ORIGINAL)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = if (placeholderResId != null) {
            painterResource(placeholderResId)
        } else null,
        error = if (errorResId != null) {
            painterResource(errorResId)
        } else null,
        imageLoader = ImageCacheManager.createOptimizedImageLoader(context)
    )
}

/**
 * 加载缩略图（带缓存优化）
 */
@Composable
fun AsyncThumbnail(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Int = 200
) {
    val context = LocalContext.current
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .size(size)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        imageLoader = ImageCacheManager.createOptimizedImageLoader(context)
    )
}

/**
 * 加载带模糊背景的图片（用于专辑封面等）
 */
@Composable
fun AsyncImageWithBlurredBackground(
    model: Any?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    blurRadius: Int = 20
) {
    val context = LocalContext.current
    
    // 模糊背景
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .build(),
        contentDescription = null,
        modifier = modifier
            .blur(radius = blurRadius.dp)
            .alpha(0.5f),
        contentScale = ContentScale.Crop,
        imageLoader = ImageCacheManager.createOptimizedImageLoader(context)
    )
    
    // 清晰前景
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .size(Size.ORIGINAL)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        imageLoader = ImageCacheManager.createOptimizedImageLoader(context)
    )
}
