# GitHub Actions 编译监控

## 📊 编译状态

### 触发编译
✅ **Tag 已推送**: `v0.0.2`
- **仓库**: https://github.com/335459215/Resonance
- **Workflow**: Android Release Build

### 🔗 监控链接

#### 1. 查看实时编译进度
打开以下链接查看编译状态：
```
https://github.com/335459215/Resonance/actions
```

#### 2. 查看特定 workflow
```
https://github.com/335459215/Resonance/actions/workflows/android-release.yml
```

### 📝 编译步骤

GitHub Actions 将执行以下步骤：

1. ✅ **Checkout repository** - 拉取代码
2. ✅ **Set up JDK 17** - 配置 Java 环境
3. ✅ **Grant execute permission** - 授权 gradlew
4. ⏳ **Get version from tag** - 获取版本号
5. ⏳ **Create gradle.properties** - 创建签名配置
6. ⏳ **Decode Keystore** - 解码签名文件
7. ⏳ **Move Keystore** - 移动签名文件到根目录
8. 🔨 **Build Release APK** - 编译 Release 版本
9. ✅ **Verify APK** - 验证 APK
10. ✅ **Upload Release APK** - 上传产物
11. ✅ **Create GitHub Release** - 创建 Release
12. ✅ **Build Debug APK** - 编译 Debug 版本
13. ✅ **Upload Debug APK** - 上传产物

### ⚠️ 注意事项

**需要配置 GitHub Secrets**：

在 GitHub 仓库设置中添加以下 Secrets：
- `RELEASE_STORE_PASSWORD` - 签名密码
- `RELEASE_KEY_ALIAS` - 密钥别名
- `RELEASE_KEY_PASSWORD` - 密钥密码
- `RELEASE_KEYSTORE_BASE64` - Base64 编码的签名文件

**获取 Base64 编码的签名文件**：
```powershell
# 在本地运行
$keystorePath = "g:\project\audio player\release.keystore"
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keystorePath))
$base64 | Set-Clipboard
# 然后粘贴到 GitHub Secrets
```

### 🎯 预期结果

编译成功后：
- ✅ GitHub Release 页面将显示 v0.0.2
- ✅ APK 文件将自动上传到 Release
- ✅ 可以直接下载已签名的 APK

### 📱 下载链接

编译成功后，APK 将在：
```
https://github.com/335459215/Resonance/releases/tag/v0.0.2
```
