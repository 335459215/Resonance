package com.resonance.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MediaCard(
    title: String,
    subtitle: String? = null,
    posterUrl: String? = null,
    progress: Float = 0f,
    isNew: Boolean = false,
    isFocused: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scale by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 0.dp,
        animationSpec = tween(200),
        label = "scale"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(200),
        label = "border"
    )
    
    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .offset(y = if (isFocused) -scale else 0.dp)
            .clickable(onClick = onClick)
    ) {
        PosterImage(
            url = posterUrl,
            modifier = Modifier.fillMaxSize()
        )
        
        if (isNew) {
            NewBadge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }
        
        if (progress > 0f) {
            ProgressBar(
                progress = progress,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }
        
        OverlayGradient(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(80.dp)
        )
        
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PosterImage(
    url: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (url != null) {
            coil.compose.AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun NewBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "NEW",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(3.dp)
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun OverlayGradient(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.8f)
                    )
                )
            )
    )
}

@Composable
fun ControlButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Int = 48
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val alpha = if (enabled) 1f else 0.4f
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                if (isPressed) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size((size * 0.5f).dp),
            tint = Color.White.copy(alpha = alpha)
        )
    }
}

@Composable
fun GlassMorphismCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Color.White.copy(alpha = 0.1f)
            )
            .then(
                Modifier
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
    ) {
        content()
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerAnim"
    )
    
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.LightGray.copy(alpha = 0.3f),
                        Color.LightGray.copy(alpha = 0.5f),
                        Color.LightGray.copy(alpha = 0.3f)
                    ),
                    start = Offset(translateAnim, 0f),
                    end = Offset(translateAnim + 300f, 0f)
                )
            )
    )
}

@Composable
fun Chip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) Color.White
                       else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}
