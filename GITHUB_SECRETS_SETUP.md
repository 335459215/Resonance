# 🔐 GitHub Actions 签名配置指南

## ✅ 已完成
- [x] 创建 GitHub Actions workflow
- [x] 推送 tag v0.0.2 触发编译
- [x] 生成签名文件 Base64 编码（已复制到剪贴板）

## 📝 需要配置的 GitHub Secrets

### 步骤 1: 打开 GitHub 仓库设置
1. 访问：https://github.com/335459215/Resonance/settings/secrets/actions
2. 点击 "New repository secret"

### 步骤 2: 添加 Secrets

#### 1. RELEASE_KEYSTORE_BASE64
- **Name**: `RELEASE_KEYSTORE_BASE64`
- **Value**: (剪贴板中的 Base64 字符串)
- **说明**: 签名文件的 Base64 编码

#### 2. RELEASE_STORE_PASSWORD
- **Name**: `RELEASE_STORE_PASSWORD`
- **Value**: `Resonance2026`
- **说明**: 签名密码

#### 3. RELEASE_KEY_ALIAS
- **Name**: `RELEASE_KEY_ALIAS`
- **Value**: `resonance`
- **说明**: 密钥别名

#### 4. RELEASE_KEY_PASSWORD
- **Name**: `RELEASE_KEY_PASSWORD`
- **Value**: `Resonance2026`
- **说明**: 密钥密码

### 步骤 3: 验证配置

添加完 Secrets 后，GitHub Actions 将自动开始编译（如果 tag 已推送）。

## 🔍 监控编译过程

### 实时查看编译状态
访问：https://github.com/335459215/Resonance/actions

### 编译预计耗时
- ⏱️ 首次编译：5-10 分钟
- ⏱️ 后续编译：3-5 分钟（有缓存）

### 编译成功标志
✅ GitHub Release 页面出现 v0.0.2
✅ APK 文件自动上传到 Release
✅ 可以直接下载已签名的 APK

## 📱 下载 APK

编译成功后：
1. 访问：https://github.com/335459215/Resonance/releases/tag/v0.0.2
2. 下载 `app-release.apk`
3. 安装到 Android 设备

## ⚠️ 故障排查

### 如果编译失败
1. 检查 Secrets 是否正确配置
2. 查看 Actions 日志了解详细错误
3. 确认签名文件 Base64 编码正确

### 手动触发编译
如果自动触发失败，可以：
1. 访问：https://github.com/335459215/Resonance/actions/workflows/android-release.yml
2. 点击 "Run workflow"
3. 选择分支和 tag
4. 点击 "Run workflow" 按钮

## 📊 当前状态

- **Tag**: v0.0.2 ✅ 已推送
- **Workflow**: android-release.yml ✅ 已创建
- **Secrets**: ⏳ 待配置
- **编译状态**: ⏳ 等待 Secrets 配置
