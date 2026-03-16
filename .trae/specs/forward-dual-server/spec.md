# 影视 + 音乐双服务器 Forward 风格 App - 需求规格说明书

## 1. 项目概述

### 1.1 产品定位
- **双服务器完全隔离**：影视服务器 + 音乐服务器独立运行
- **Forward 风格 UI**：100% 复刻 iOS Forward 原生质感
- **全场景适配**：多屏幕/横竖屏/双平台响应式设计

### 1.2 核心特性
- 服务器切换联动全局 UI
- 动态底部导航栏
- 零割裂感布局
- 全场景响应式适配

## 2. 全局视觉规范

### 2.1 颜色系统
```kotlin
// 主色调 - Forward 标志性珊瑚红
Primary = Color(0xFFFF3A3B)

// 背景系统
DarkBackground = Color(0xFF000000) → Color(0xFF121212)  // 纯黑到深灰渐变
DarkSurface = Color(0xFF1C1C1E)
DarkSurfaceVariant = Color(0xFF2C2C2E)

// 文字颜色
OnBackground = Color(0xFFFFFFFF)      // 纯白标题
OnSurface = Color(0xFFE1E1E1)         // 次白正文
OnSurfaceVariant = Color(0xFF8A8A8A)  // 浅灰辅助
```

### 2.2 圆角规范
- **统一圆角**：20pt（图片、卡片、按钮、输入框、导航栏）
- **小元素圆角**：12pt（徽章、标签）
- **圆形元素**：50%（头像、控制按钮）

### 2.3 毛玻璃效果
```kotlin
// iOS: UIBlurEffect.systemUltraThinMaterialDark
// 安卓：RenderScript / BlurMaskFilter
val GlassMorphismDark = Color(0x80000000)
val GlassBorder = Color(0x40FFFFFF)
```

### 2.4 字体系统
```kotlin
// iOS: SF Pro, 安卓：Roboto
// 使用 Dynamic Type (iOS) / sp (安卓) 实现系统字体缩放

// 字号层级
DisplayLarge = 57.sp    // 超大标题
HeadlineLarge = 32.sp   // 大标题
TitleLarge = 22.sp      // 主标题
TitleMedium = 18.sp     // 次标题
BodyLarge = 16.sp       // 正文
BodySmall = 12.sp       // 辅助文字
```

### 2.5 间距系统
```kotlin
// 页面边距
PhoneMargin = 16.pt   // 手机
TabletMargin = 24.pt  // 平板

// 模块间距
ModuleSpacing = 20.pt  // 模块间上下间距

// 卡片间距
CardSpacingHorizontal = 8.pt  // 横向（手机）
CardSpacingVertical = 12.pt   // 纵向（手机）

// 列表行高
ListItemHeight = 72.pt  // 标准行高
ListItemHeightMin = 60.pt  // 最小行高（小屏）
```

## 3. 服务器管理体系

### 3.1 服务器类型
```kotlin
enum class ServerType {
    VIDEO,  // 影视服务器
    MUSIC   // 音乐服务器
}
```

### 3.2 服务器配置
```kotlin
data class ServerConfig(
    val id: String,
    val name: String,
    val url: String,
    val type: ServerType,
    val username: String = "",
    val password: String = "",
    val isConnected: Boolean = false,
    val isDefault: Boolean = false,
    val lastVisitedPage: String = "discovery"  // 记录上次访问页面
)
```

### 3.3 服务器管理流程
1. **入口**：我的页 → 服务器管理
2. **列表页**：分两组展示（音乐/影视）
3. **添加页**：表单填写 + 测试连接
4. **切换逻辑**：
   - 暂停当前播放
   - 全局 UI 淡隐（0.15s）
   - 切换底部导航
   - 刷新页面内容
   - 全局 UI 淡显（0.15s）

## 4. 动态底部导航

### 4.1 导航结构
```kotlin
// 高度：56pt（统一）
// 背景：毛玻璃半透
// 圆角：20pt

data class NavigationConfig(
    val coreTabs: List<TabItem>,      // 中间 3 个核心 Tab
    val leftButton: ButtonItem,       // 左侧辅助按钮
    val rightButton: ButtonItem       // 右侧辅助按钮
)
```

### 4.2 服务器模式配置
```kotlin
// 影视服务器
VideoNavigation = NavigationConfig(
    coreTabs = listOf(
        TabItem("discovery", "发现", Icons.Outlined.Home),
        TabItem("series", "追剧", Icons.Outlined.Movie),
        TabItem("settings", "设置", Icons.Outlined.Settings)
    ),
    leftButton = ButtonItem("server", Icons.Outlined.Dns),
    rightButton = ButtonItem("search", Icons.Outlined.Search)
)

// 音乐服务器
MusicNavigation = NavigationConfig(
    coreTabs = listOf(
        TabItem("discovery", "发现", Icons.Outlined.Home),
        TabItem("playlist", "歌单", Icons.Outlined.MusicNote),
        TabItem("settings", "设置", Icons.Outlined.Settings)
    ),
    leftButton = ButtonItem("server", Icons.Outlined.Dns),
    rightButton = ButtonItem("search", Icons.Outlined.Search)
)
```

### 4.3 联动动画
- **服务器切换**：第二个 Tab 平滑过渡（图标 + 文字渐变 + 缩放）
- **辅助按钮**：淡入淡出（0.3s 弹簧动画）
- **页面切换**：辅助按钮动态变化，核心 Tab 稳定

## 5. 核心页面布局

### 5.1 通用顶部结构
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 16.dp)
) {
    // 左上角：服务器名称（点击切换）
    ServerSelector()
    
    // 中部：搜索框（毛玻璃，圆角 20pt）
    SearchBar(modifier = Modifier.weight(1f))
    
    // 右上角：更多/扫码
    MoreButton()
}
```

### 5.2 轮播 Banner（发现页）
```kotlin
// 高度：screenWidth * 0.45
// 折叠动效：下滑缩小至 60pt，上滑放大
// 吸附规则：滚动停止时自动吸附

val bannerHeight by animateDpAsState(
    targetValue = if (expanded) screenWidth * 0.45 else 60.dp
)
```

### 5.3 影视服务器页面

#### 5.3.1 发现页
```kotlin
LazyColumn {
    // 轮播 Banner
    item { CarouselBanner() }
    
    // 继续观看（横向列表）
    item { ContinueWatchingSection() }
    
    // 媒体库（横向分类卡片）
    item { MediaLibrarySection() }
    
    // 分类内容模块组
    items(categories) { category ->
        CategorySection(
            title = category.name,
            layout = category.layout,  // horizontal/grid
            items = category.items
        )
    }
}
```

#### 5.3.2 追剧页
```kotlin
Column {
    // 顶部筛选栏
    FilterBar(
        filters = listOf("类型", "地区", "年份")
    )
    
    // 内容列表
    MediaGrid(
        columns = if (isTablet) 4 else 3,
        items = mediaItems
    )
}
```

#### 5.3.3 影视详情页
```kotlin
// 竖屏：顶部 16:9 海报 → 毛玻璃信息栏 → 剧集列表
// 横屏：左右分栏（左侧海报，右侧信息）

Column {
    PosterImage(
        aspectRatio = 16f / 9f,
        modifier = Modifier.fillMaxWidth()
    )
    
    GlassMorphismInfoCard()
    
    EpisodeList()
    
    RelatedContent()
}
```

#### 5.3.4 影视播放器
```kotlin
// 竖屏：全屏视频 + 上下控制栏
// 横屏：全屏视频 + 底部控制栏

Box {
    VideoPlayer()
    
    // 控制栏（点击显示，3 秒自动隐藏）
    if (showControls) {
        TopControls()
        BottomControls()
    }
}
```

### 5.4 音乐服务器页面

#### 5.4.1 发现页
```kotlin
LazyColumn {
    item { CarouselBanner() }  // 推荐歌单
    item { RecentlyPlayedSection() }
    item { RecommendedSection() }
    item { MyPlaylistsSection() }
}
```

#### 5.4.2 歌单页
```kotlin
Column {
    FilterBar(
        filters = listOf("风格", "歌手", "年份")
    )
    
    PlaylistGrid(
        columns = if (isTablet) 3 else 2,
        items = playlists
    )
}
```

#### 5.4.3 音乐详情页
```kotlin
// 竖屏：1:1 专辑封面 → 毛玻璃信息栏 → 歌曲列表
// 横屏：左右分栏

Column {
    AlbumCover(
        aspectRatio = 1f,
        modifier = Modifier.fillMaxWidth()
    )
    
    GlassMorphismInfoCard()
    
    SongList()
}
```

#### 5.4.4 音乐播放器
```kotlin
// 背景：专辑封面模糊全屏
// 中间：1:1 大封面
// 歌词面板：毛玻璃，可上下滑动
// 进度条 + 控制栏

Box {
    BlurredAlbumBackground()
    
    Column {
        LargeAlbumCover()
        LyricsPanel()  // 可滑动
        SongInfo()
        ProgressBar()
        ControlButtons()
    }
}
```

### 5.5 我的页
```kotlin
LazyColumn {
    // 用户信息区
    item { UserProfileSection() }
    
    // 功能卡片组
    items(functionCards) { card ->
        FunctionCard(
            icon = card.icon,
            title = card.title,
            onClick = card.onClick
        )
    }
}
```

## 6. 响应式适配规则

### 6.1 多屏幕适配
```kotlin
// 手机（320pt-430pt）
if (screenWidth < 600.dp) {
    columns = if (isVideo) 3 else 2
    margin = 16.dp
}

// 平板（768pt+）
if (screenWidth >= 600.dp) {
    columns = if (isVideo) 4 else 3
    margin = 24.dp
}

// 最大宽度限制
maxContentWidth = 800.dp
```

### 6.2 横竖屏适配
```kotlin
// 检测屏幕方向
val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

// 竖屏布局
if (!isLandscape) {
    // 顶部导航 + 内容 + 底部导航
}

// 横屏布局
if (isLandscape) {
    // 发现/列表页：更宽网格
    // 播放器/详情页：左右分栏
    // 迷你播放条：自动隐藏
}
```

### 6.3 安全区适配
```kotlin
// iOS：刘海/灵动岛
val windowInsets = WindowInsetsCompatCompat.getSystemWindowInsets()
val topPadding = windowInsets.systemWindowInsetTop
val bottomPadding = windowInsets.systemWindowInsetBottom

// 安卓：挖孔/折叠屏
val imeInsets = WindowInsetsCompat.getSystemWindowInsets()
```

## 7. 状态管理

### 7.1 数据隔离
```kotlin
data class ServerState(
    val serverId: String,
    val currentPage: String,
    val scrollPosition: Float,
    val playbackState: PlaybackState?,
    val favorites: List<String>,
    val history: List<String>,
    val downloads: List<String>
)
```

### 7.2 状态保持
- 每个服务器独立记录页面状态
- 切换后自动恢复上次访问页
- 播放进度、滚动位置保持不变

### 7.3 错误处理
```kotlin
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: Data) : UiState()
    data class Error(val message: String, val retryAction: () -> Unit) : UiState()
    object Empty : UiState()
}
```

## 8. 性能优化

### 8.1 图片加载
```kotlin
// Coil 图片加载
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .build()
)
```

### 8.2 列表优化
```kotlin
// LazyColumn 性能优化
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    itemContentPadding = PaddingValues(4.dp)
) {
    items(items, key = { it.id }) { item ->
        MediaItem(item)
    }
}
```

### 8.3 动画优化
```kotlin
// 弹簧动画
val springSpec = spring<Float>(
    dampingRatio = 0.8f,
    stiffness = Spring.StiffnessMedium
)
```

## 9. 测试要求

### 9.1 单元测试
- 服务器管理逻辑
- 导航状态管理
- 数据隔离验证

### 9.2 UI 测试
- 响应式布局测试
- 横竖屏切换测试
- 服务器切换动画测试

### 9.3 性能测试
- 启动时间 < 2s
- 页面切换 < 300ms
- 内存占用 < 200MB

## 10. 交付标准

### 10.1 代码质量
- 0 警告 0 错误
- 代码覆盖率 > 80%
- 遵循 Kotlin 代码规范

### 10.2 用户体验
- 所有动画流畅（60fps）
- 无卡顿、无闪烁
- 符合 Forward 设计语言

### 10.3 兼容性
- Android 6.0+ (API 23+)
- 适配所有主流屏幕尺寸
- 支持横竖屏切换
