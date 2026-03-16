package com.resonance.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class NavigationItem(
    val icon: ImageVector,
    val label: String,
    val badge: Int = 0
)

@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badge > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (item.badge > 99) "99+" else item.badge.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun NavigationRailBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badge > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ) {
                                    Text(
                                        text = if (item.badge > 99) "99+" else item.badge.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索...",
    onSearch: () -> Unit = {}
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "搜索",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "清除",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    )
}

@Composable
fun FloatingActionMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    items: List<FloatingMenuItem>
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
            exit = fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        FloatingActionButton(
                            onClick = item.onClick,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            elevation = FloatingActionButtonDefaults.elevation(4.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "关闭" else "添加"
            )
        }
    }
}

data class FloatingMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsBar(
    modifier: Modifier = Modifier,
    actions: List<QuickAction>
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            actions.forEach { action ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = action.onClick)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.label,
                        tint = if (action.selected) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = action.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (action.selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean = false,
    val onClick: () -> Unit
)
