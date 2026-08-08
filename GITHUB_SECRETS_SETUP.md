# 🔐 GitHub Actions 签名配置指南

## ✅ 已完成 (2026-08-08 安全整改)

- [x] 创建 GitHub Actions workflow
- [x] 生成全新签名 keystore (旧 keystore 已丢失, 旧签名身份作废)
- [x] 4 个 Secrets 已配置: `RELEASE_KEYSTORE_BASE64` / `RELEASE_STORE_PASSWORD` / `RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD`
- [x] 密码不再硬编码在仓库中 (gradle.properties / build.gradle.kts / 本文档均无明文)

## 📝 已配置的 GitHub Secrets

| Secret | 说明 |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | 签名 keystore 文件的 Base64 编码 |
| `RELEASE_STORE_PASSWORD` | keystore 存储密码 |
| `RELEASE_KEY_ALIAS` | 密钥别名 (`resonance`) |
| `RELEASE_KEY_PASSWORD` | 密钥密码 |

> ⚠️ Secret 值仅保存在 GitHub 仓库设置与本地 `.release-credentials` (权限 600)。
> 修改方法: 仓库 → Settings → Secrets and variables → Actions。

## 🔍 触发构建

推送 tag 自动触发, 或手动: Actions → Android Release Build → Run workflow

## 📱 下载 APK

构建成功后到 Releases 页面下载 `app-release.apk`。
