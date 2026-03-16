package com.resonance.ui.adaptation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.ExperimentalAnimationApi

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ForwardEliteApp(
    onBackClick: () -> Unit = {}
) {
    var view by remember { mutableStateOf(UIView.DISCOVERY) }
    var isAiActive by remember { mutableStateOf(true) }
    
    AnimatedContent(
        targetState = view,
        transitionSpec = {
            when {
                UIView.PLAYER isTransitioningTo UIView.DISCOVERY -> {
                    (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith
                    (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                }
                else -> {
                    (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith
                    (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                }
            }.using(
                SizeTransform(clip = false)
            )
        }
    ) {currentView ->
        when (currentView) {
            UIView.DISCOVERY -> {
                DiscoveryView(
                    onPlay = { view = UIView.PLAYER },
                    onBackClick = onBackClick
                )
            }
            UIView.PLAYER -> {
                PlayerView(
                    isAiActive = isAiActive,
                    toggleAi = { isAiActive = !isAiActive },
                    onBack = { view = UIView.DISCOVERY },
                    onBackToHome = onBackClick
                )
            }
        }
    }
}



// --- 视图类型枚举 ---
enum class UIView {
    DISCOVERY,
    PLAYER
}
