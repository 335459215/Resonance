package com.resonance.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.resonance.data.repository.EmbyRepository
import com.resonance.data.repository.CloudStorageManager
import com.resonance.ui.ServerConfig
import com.resonance.ui.ServerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerManagementScreen(
    servers: List<ServerConfig> = emptyList(),
    embyRepository: EmbyRepository? = null,
    cloudStorageManager: CloudStorageManager? = null,
    onServerAdd: () -> Unit = {},
    onServerEdit: (ServerConfig) -> Unit = {},
    onServerDelete: (ServerConfig) -> Unit = {},
    onServerSelect: (ServerConfig) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("服务器管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onServerAdd,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(servers) {
                ServerItem(
                    server = it,
                    onEdit = {
                        onServerEdit(it)
                    },
                    onDelete = {
                        onServerDelete(it)
                    },
                    onSelect = {
                        onServerSelect(it)
                    }
                )
            }
            
            if (servers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "无服务器",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "未配置服务器",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角加号按钮添加服务器",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerItem(
    server: ServerConfig,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onSelect()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (server.isDefault) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when (server.type) {
                            ServerType.EMBY -> Color(0xFF00A4DC)
                            ServerType.PLEX -> Color(0xFFE5A00D)
                            ServerType.CLOUD_115 -> Color(0xFFFF6B6B)
                            ServerType.CLOUD_123 -> Color(0xFF4ECDC4)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = when (server.type) {
                        ServerType.EMBY -> Icons.Default.VideoLibrary
                        ServerType.PLEX -> Icons.Default.Star
                        ServerType.CLOUD_115 -> Icons.Default.Cloud
                        ServerType.CLOUD_123 -> Icons.Default.Cloud
                        else -> Icons.Default.Dns
                    },
                    contentDescription = server.type.name,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (server.isDefault) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = server.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.type.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (server.isEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Enabled",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun AddServerDialog(
    onDismiss: () -> Unit,
    onConfirm: (ServerConfig) -> Unit
) {
    var serverName by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("") }
    var serverType by remember { mutableStateOf(ServerType.EMBY) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加服务器") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 服务器类型选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("服务器类型:")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ServerType.entries.forEach {
                            FilterChip(
                                selected = serverType == it,
                                onClick = { serverType = it },
                                label = { Text(it.name) }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("服务器名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { serverUrl = it },
                    label = { Text("服务器地址") },
                    placeholder = {
                        Text(
                            when (serverType) {
                                ServerType.EMBY -> "http://192.168.1.100:8096"
                                ServerType.PLEX -> "http://192.168.1.100:32400"
                                ServerType.CLOUD_115 -> "https://www.115.com"
                                ServerType.CLOUD_123 -> "https://www.123pan.com"
                                else -> "URL"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDefault,
                        onCheckedChange = { isDefault = it }
                    )
                    Text("设为默认服务器")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (serverName.isNotEmpty() && serverUrl.isNotEmpty()) {
                    onConfirm(
                        ServerConfig(
                            id = System.currentTimeMillis().toString(),
                            name = serverName,
                            url = serverUrl,
                            type = serverType,
                            username = username,
                            password = password,
                            isDefault = isDefault
                        )
                    )
                }
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}