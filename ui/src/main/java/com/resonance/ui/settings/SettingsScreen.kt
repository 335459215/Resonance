package com.resonance.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resonance.data.repository.EmbyRepository
import com.resonance.data.repository.CloudStorageManager
import com.resonance.ui.ServerConfig
import com.resonance.ui.ServerType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    embyRepository: EmbyRepository,
    cloudStorageManager: CloudStorageManager,
    onNavigateBack: () -> Unit,
    onNavigateToServerManagement: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    
    var serverUrl by remember { mutableStateOf(embyRepository.serverUrl ?: "") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var defaultPlayer by remember { mutableStateOf("Media3 (ExoPlayer)") }
    var hardwareDecode by remember { mutableStateOf(true) }
    var autoSwitchPlayer by remember { mutableStateOf(true) }
    var bufferStrategy by remember { mutableStateOf("Auto") }
    
    var theme by remember { mutableStateOf("System") }
    var posterQuality by remember { mutableStateOf("High") }
    var posterSource by remember { mutableStateOf("TMDB") }
    
    var showServerDialog by remember { mutableStateOf(false) }
    var showPlayerDialog by remember { mutableStateOf(false) }
    var showBufferDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPosterQualityDialog by remember { mutableStateOf(false) }
    var showPosterSourceDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 服务器设置
            SettingsSection(title = "Server") {
                SettingsItem(
                    icon = Icons.Default.Dns,
                    title = "Server Address",
                    subtitle = serverUrl.ifEmpty { "Not configured" },
                    onClick = { showServerDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Username",
                    subtitle = username.ifEmpty { "Not logged in" },
                    onClick = { showLoginDialog = true }
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            testResult = try {
                                val success = embyRepository.testConnection(serverUrl)
                                if (success) "Connection successful!"
                                else "Connection failed"
                            } catch (e: Exception) {
                                "Error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = serverUrl.isNotEmpty() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isLoading) "Testing..." else "Test Connection")
                }
                
                testResult?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (it.contains("successful")) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SettingsItem(
                    icon = Icons.Default.ManageAccounts,
                    title = "Server Management",
                    subtitle = "Manage multiple servers and cloud storage",
                    onClick = onNavigateToServerManagement
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 播放设置
            SettingsSection(title = "Playback") {
                SettingsItem(
                    icon = Icons.Default.PlayCircle,
                    title = "Default Player",
                    subtitle = defaultPlayer,
                    onClick = { showPlayerDialog = true }
                )
                
                SettingsSwitch(
                    icon = Icons.Default.Memory,
                    title = "Hardware Decode",
                    subtitle = "Use hardware acceleration when available",
                    checked = hardwareDecode,
                    onCheckedChange = { hardwareDecode = it }
                )
                
                SettingsSwitch(
                    icon = Icons.Default.SwapHoriz,
                    title = "Auto Switch Player",
                    subtitle = "Automatically switch to another player on error",
                    checked = autoSwitchPlayer,
                    onCheckedChange = { autoSwitchPlayer = it }
                )
                
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Buffer Strategy",
                    subtitle = bufferStrategy,
                    onClick = { showBufferDialog = true }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 外观设置
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = theme,
                    onClick = { showThemeDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.Image,
                    title = "Poster Quality",
                    subtitle = posterQuality,
                    onClick = { showPosterQualityDialog = true }
                )
                
                SettingsItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Poster Source",
                    subtitle = posterSource,
                    onClick = { showPosterSourceDialog = true }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 云存储设置
            SettingsSection(title = "Cloud Storage") {
                val is115Connected = cloudStorageManager.getServiceStatus(ServerType.CLOUD_115)
                val is123Connected = cloudStorageManager.getServiceStatus(ServerType.CLOUD_123)
                
                SettingsItem(
                    icon = Icons.Default.Cloud,
                    title = "115 Cloud Storage",
                    subtitle = if (is115Connected) "已连接" else "连接到 115 网盘",
                    onClick = {
                        if (!is115Connected) {
                            coroutineScope.launch {
                                cloudStorageManager.connectService(ServerType.CLOUD_115, "", "") { success ->
                                    // 处理连接结果
                                }
                            }
                        }
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.CloudQueue,
                    title = "123 Cloud Storage",
                    subtitle = if (is123Connected) "已连接" else "连接到 123 网盘",
                    onClick = {
                        if (!is123Connected) {
                            coroutineScope.launch {
                                cloudStorageManager.connectService(ServerType.CLOUD_123, "", "") { success ->
                                    // 处理连接结果
                                }
                            }
                        }
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Plex",
                    subtitle = "连接到 Plex 媒体服务器",
                    onClick = {
                        coroutineScope.launch {
                            cloudStorageManager.connectService(ServerType.PLEX, "", "") { success ->
                                // 处理连接结果
                            }
                        }
                    }
                )
                
                SettingsSwitch(
                    icon = Icons.Default.Sync,
                    title = "Auto Sync Cloud",
                    subtitle = "Automatically sync cloud storage",
                    checked = cloudStorageManager.autoSyncEnabled,
                    onCheckedChange = { cloudStorageManager.autoSyncEnabled = it }
                )
                
                SettingsSwitch(
                    icon = Icons.Default.Cached,
                    title = "Cloud Cache",
                    subtitle = "Cache cloud files locally",
                    checked = cloudStorageManager.cacheEnabled,
                    onCheckedChange = { cloudStorageManager.cacheEnabled = it }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 高级设置
            SettingsSection(title = "Advanced") {
                SettingsItem(
                    icon = Icons.Default.BugReport,
                    title = "Debug Logs",
                    subtitle = "View application logs",
                    onClick = { /* Navigate to logs */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear Cache",
                    subtitle = "Clear all cached data",
                    onClick = { /* Clear cache */ }
                )
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version 1.0.0",
                    onClick = { /* Show about */ }
                )
            }
        }
    }
    
    // 对话框
    if (showServerDialog) {
        ServerDialog(
                    currentUrl = serverUrl,
                    onDismiss = { showServerDialog = false },
                    onConfirm = {
                        serverUrl = it
                        coroutineScope.launch {
                            embyRepository.updateServerUrl(it)
                        }
                        showServerDialog = false
                    }
                )
    }
    
    if (showLoginDialog) {
        LoginDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = { user, pass ->
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val success = embyRepository.login(user, pass)
                        if (success) {
                            username = user
                            password = pass
                            testResult = "Login successful!"
                        } else {
                            testResult = "Login failed"
                        }
                    } catch (e: Exception) {
                        testResult = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                        showLoginDialog = false
                    }
                }
            }
        )
    }
    
    if (showPlayerDialog) {
        SelectionDialog(
            title = "Default Player",
            options = listOf("Media3 (ExoPlayer)", "IjkPlayer (FFmpeg)", "LibVLC"),
            selectedOption = defaultPlayer,
            onDismiss = { showPlayerDialog = false },
            onConfirm = { option ->
                defaultPlayer = option
                showPlayerDialog = false
            }
        )
    }
    
    if (showBufferDialog) {
        SelectionDialog(
            title = "Buffer Strategy",
            options = listOf("Auto", "Low Latency", "High Quality", "Custom"),
            selectedOption = bufferStrategy,
            onDismiss = { showBufferDialog = false },
            onConfirm = { option ->
                bufferStrategy = option
                showBufferDialog = false
            }
        )
    }
    
    if (showThemeDialog) {
        SelectionDialog(
            title = "Theme",
            options = listOf("System", "Light", "Dark"),
            selectedOption = theme,
            onDismiss = { showThemeDialog = false },
            onConfirm = { option ->
                theme = option
                showThemeDialog = false
            }
        )
    }
    
    if (showPosterQualityDialog) {
        SelectionDialog(
            title = "Poster Quality",
            options = listOf("Low", "Medium", "High", "Original"),
            selectedOption = posterQuality,
            onDismiss = { showPosterQualityDialog = false },
            onConfirm = { option ->
                posterQuality = option
                showPosterQualityDialog = false
            }
        )
    }
    
    if (showPosterSourceDialog) {
        SelectionDialog(
            title = "Poster Source",
            options = listOf("TMDB", "Emby", "Auto"),
            selectedOption = posterSource,
            onDismiss = { showPosterSourceDialog = false },
            onConfirm = { option ->
                posterSource = option
                showPosterSourceDialog = false
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Server Address") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                placeholder = { Text("http://192.168.1.100:8096") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(url) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selected by remember { mutableStateOf(selectedOption) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = option }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selected,
                            onClick = { selected = option }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Login") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (username.isNotEmpty() && password.isNotEmpty()) {
                    onLogin(username, password)
                }
            }) {
                Text("Login")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}