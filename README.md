# 🎵 Resonance

<div align="center">

**共鸣 · 与音乐产生深度连接**

[![Android CI/CD](https://github.com/yourusername/Resonance/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yourusername/Resonance/actions/workflows/android-ci.yml)
[![Release](https://img.shields.io/github/v/release/yourusername/Resonance)](https://github.com/yourusername/Resonance/releases)
[![License](https://img.shields.io/github/license/yourusername/Resonance)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android-blue)]()

[下载最新版本](https://github.com/yourusername/Resonance/releases/latest) · [功能特性](#-功能特性) · [使用指南](#-使用指南)

</div>

---

## 📱 应用简介

**Resonance** 是一款专为音乐爱好者打造的高品质音频播放器，支持与 Emby 媒体服务器无缝集成，提供流畅、优雅的播放体验。

### ✨ 核心亮点

- 🎵 **高品质音质** - 支持多种音频格式，还原音乐本真
- 🚀 **极速流畅** - 优化的性能，丝滑的播放体验
- 🎨 **现代设计** - Material You 设计语言，美观大方
- 🔗 **Emby 集成** - 完美对接 Emby 媒体库
- 📱 **多端适配** - 手机、平板、TV 全面支持

---

## 🎯 功能特性

### 播放功能
- ✅ 支持 MP3、FLAC、WAV、AAC 等多种音频格式
- ✅ 无损音质播放
- ✅ 播放队列管理
- ✅ 播放历史记录
- ✅ 睡眠定时器
- ✅ 均衡器调节

### 媒体库
- ✅ Emby 服务器连接
- ✅ 本地音乐扫描
- ✅ 专辑封面展示
- ✅ 歌手分类浏览
- ✅ 播放列表管理
- ✅ 收藏功能

### 界面设计
- ✅ Material You 动态主题
- ✅ 深色/浅色模式
- ✅ 自适应布局
- ✅ 流畅动画效果
- ✅ 手势操作支持

### 高级功能
- ✅ 歌词显示
- ✅ 音量平衡
- ✅ 跨设备同步
- ✅ 离线模式
- ✅ 后台播放
- ✅ 锁屏控制

---

## 📥 下载安装

### 方式一：GitHub Releases（推荐）
访问 [Releases 页面](https://github.com/yourusername/Resonance/releases) 下载最新 APK 文件

### 方式二：自动构建
每次代码更新后，GitHub Actions 会自动构建新版本，可在 [Actions](https://github.com/yourusername/Resonance/actions) 页面查看

### 方式三：手动编译
```bash
git clone https://github.com/yourusername/Resonance.git
cd Resonance
./gradlew assembleDebug
```

---

## 🚀 快速开始

### 1. 连接 Emby 服务器
1. 打开应用，进入设置
2. 添加 Emby 服务器地址
3. 输入用户名和密码
4. 测试连接并保存

### 2. 播放音乐
1. 浏览媒体库或搜索音乐
2. 点击歌曲即可播放
3. 使用底部播放控制栏控制播放

### 3. 创建播放列表
1. 长按歌曲添加到队列
2. 进入播放列表管理
3. 调整播放顺序或删除歌曲

---

## 🛠️ 开发指南

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17 或更高版本
- Android SDK 23+
- Gradle 8.0+

### 构建步骤

1. **克隆项目**
```bash
git clone https://github.com/yourusername/Resonance.git
cd Resonance
```

2. **配置 Emby API（可选）**
复制 `secrets.properties.example` 为 `secrets.properties` 并填写配置

3. **编译项目**
```bash
./gradlew assembleDebug
```

4. **运行应用**
在 Android Studio 中打开项目并运行

### 项目结构
```
Resonance/
├── app/              # 主应用模块
├── core/             # 核心播放器
├── data/             # 数据层
├── ui/               # UI 界面
├── emby/             # Emby API
└── build.gradle.kts  # 构建配置
```

---

## 📋 版本发布

### 自动版本管理
项目使用 GitHub Actions 实现自动版本递增：

- **触发条件**: 推送到 main 分支
- **版本规则**: 自动增加 patch 版本号 (0.0.x → 0.0.x+1)
- **自动发布**: 创建 Release 并上传 APK

### 手动版本管理

**Linux/macOS:**
```bash
chmod +x bump-version.sh
./bump-version.sh [major|minor|patch]
```

**Windows PowerShell:**
```powershell
.\bump-version.ps1 [major|minor|patch]
```

### 发布流程
1. 更新版本号
2. 提交代码并推送
3. GitHub Actions 自动构建
4. 创建 Release Tag
5. 发布新版本

---

## 🤝 贡献指南

我们欢迎各种形式的贡献！

### 贡献方式
- 🐛 报告 Bug
- 💡 提出新功能建议
- 📝 改进文档
- 🎨 提交代码

### 贡献步骤
1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源协议

---

## 🙏 致谢

感谢以下开源项目：

- [Emby](https://emby.media/) - 媒体服务器
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - 现代 UI 工具包
- [Coil](https://coil-kt.github.io/coil/) - 图片加载库
- [ExoPlayer](https://exoplayer.dev/) - 媒体播放器

---

## 📞 联系方式

- 📧 Email: your.email@example.com
- 💬 Issues: [GitHub Issues](https://github.com/yourusername/Resonance/issues)
- 🌐 Website: https://yourwebsite.com

---

## 📊 项目统计

![Star History Chart](https://api.star-history.com/svg?repos=yourusername/Resonance&type=Date)

---

<div align="center">

**Made with ❤️ by Resonance Team**

[⭐ Star this project](https://github.com/yourusername/Resonance/stargazers)

</div>
