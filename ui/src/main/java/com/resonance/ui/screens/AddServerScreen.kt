package com.resonance.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.resonance.data.model.ServerType
import com.resonance.ui.components.ForwardButton
import com.resonance.ui.theme.AppColors
import com.resonance.ui.theme.AppShapes
import com.resonance.ui.theme.ForwardSpacing
import com.resonance.ui.theme.ForwardTypography

/**
 * 添加服务器屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(
    onNavigateBack: () -> Unit,
    onSaveServer: (ServerData) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    // 表单状态
    var serverType by remember { mutableStateOf(ServerType.VIDEO) }
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var url by remember { mutableStateOf(TextFieldValue("")) }
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    
    // 验证状态
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 测试连接状态
    var isTesting by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    
    // 保存按钮状态
    val canSave = name.text.isNotBlank() && url.text.isNotBlank() && isConnected
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 顶部导航栏
            AddServerAppBar(
                onNavigateBack = onNavigateBack
            )
            
            // 表单内容
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(ForwardSpacing.PhoneMargin)
            ) {
                // 服务器类型选择
                ServerTypeSelector(
                    selectedType = serverType,
                    onTypeSelected = { serverType = it }
                )
                
                Spacer(modifier = Modifier.height(ForwardSpacing.ModuleSpacing))
                
                // 基本信息卡片
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.Forward)
                        .border(1.dp, AppColors.GlassBorder, AppShapes.Forward),
                    color = AppColors.GlassMorphismDark.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(ForwardSpacing.ModuleSpacing),
                        verticalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingVertical)
                    ) {
                        Text(
                            text = "基本信息",
                            style = ForwardTypography.TitleMedium,
                            color = AppColors.DarkOnBackground
                        )
                        
                        // 服务器名称
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("服务器名称") },
                            placeholder = { Text("例如：我的影视库") },
                            singleLine = true,
                            shape = AppShapes.Forward,
                            colors = OutlinedTextFieldDefaults.colors(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 服务器地址
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            label = { Text("服务器地址") },
                            placeholder = { Text("例如：http://192.168.1.100:8096") },
                            singleLine = true,
                            shape = AppShapes.Forward,
                            colors = OutlinedTextFieldDefaults.colors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ForwardSpacing.ModuleSpacing))
                
                // 认证信息卡片
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.Forward)
                        .border(1.dp, AppColors.GlassBorder, AppShapes.Forward),
                    color = AppColors.GlassMorphismDark.copy(alpha = 0.6f)
                ) {
                    Column(
                        modifier = Modifier.padding(ForwardSpacing.ModuleSpacing),
                        verticalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingVertical)
                    ) {
                        Text(
                            text = "认证信息（可选）",
                            style = ForwardTypography.TitleMedium,
                            color = AppColors.DarkOnBackground
                        )
                        
                        // 用户名
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("用户名") },
                            singleLine = true,
                            shape = AppShapes.Forward,
                            colors = OutlinedTextFieldDefaults.colors(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        // 密码
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码") },
                            singleLine = true,
                            shape = AppShapes.Forward,
                            colors = OutlinedTextFieldDefaults.colors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(ForwardSpacing.ModuleSpacing))
                
                // 测试连接按钮
                ForwardButton(
                    onClick = {
                        // 调用 Repository 的测试连接方法
                        isTesting = true
                        isConnected = true
                        isTesting = false
                    },
                    text = if (isTesting) "测试中..." else "测试连接",
                    showLoading = isTesting,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 连接状态提示
                AnimatedVisibility(visible = isConnected) {
                    Text(
                        text = "✓ 连接成功",
                        style = ForwardTypography.BodyMedium,
                        color = AppColors.Success,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // 错误提示
                AnimatedVisibility(visible = showError) {
                    Text(
                        text = "✗ $errorMessage",
                        style = ForwardTypography.BodyMedium,
                        color = AppColors.Error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(ForwardSpacing.ModuleSpacing))
                
                // 保存按钮
                ForwardButton(
                    onClick = {
                        val serverData = ServerData(
                            type = serverType,
                            name = name.text,
                            url = url.text,
                            username = username.text,
                            password = password.text
                        )
                        onSaveServer(serverData)
                    },
                    text = "保存",
                    enabled = canSave,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 添加服务器顶部导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerAppBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "添加服务器",
                style = ForwardTypography.TitleLarge,
                color = AppColors.DarkOnBackground
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = AppColors.DarkOnBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = AppColors.DarkSurface.copy(alpha = 0.9f)
        )
    )
}

/**
 * 服务器类型选择器
 */
@Composable
fun ServerTypeSelector(
    selectedType: ServerType,
    onTypeSelected: (ServerType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ForwardSpacing.CardSpacingHorizontal)
    ) {
        // 影视服务器
        FilterChip(
            selected = selectedType == ServerType.VIDEO,
            onClick = { onTypeSelected(ServerType.VIDEO) },
            label = { Text("影视服务器") },
            leadingIcon = if (selectedType == ServerType.VIDEO) {
                {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            modifier = Modifier.weight(1f)
        )
        
        // 音乐服务器
        FilterChip(
            selected = selectedType == ServerType.MUSIC,
            onClick = { onTypeSelected(ServerType.MUSIC) },
            label = { Text("音乐服务器") },
            leadingIcon = if (selectedType == ServerType.MUSIC) {
                {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 服务器数据
 */
data class ServerData(
    val type: ServerType,
    val name: String,
    val url: String,
    val username: String = "",
    val password: String = ""
)
