# Resonance 版本管理脚本 (PowerShell 版本)
# 自动递增版本号并创建 release tag

$ErrorActionPreference = "Stop"

Write-Host "🎵 Resonance 版本管理工具" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

# 获取当前版本号
function Get-CurrentVersion {
    if (Test-Path "app/build.gradle.kts") {
        $content = Get-Content "app/build.gradle.kts" -Raw
        $match = [regex]::Match($content, 'versionName = "([^"]+)"')
        if ($match.Success) {
            return $match.Groups[1].Value
        }
    }
    return "0.0.0"
}

# 递增版本号
function Increment-Version {
    param(
        [string]$Version,
        [ValidateSet("major", "minor", "patch")]
        [string]$Type = "patch"
    )
    
    $parts = $Version.Split('.')
    $major = [int]($parts[0] ?? 0)
    $minor = [int]($parts[1] ?? 0)
    $patch = [int]($parts[2] ?? 0)
    
    switch ($Type) {
        "major" {
            $major++
            $minor = 0
            $patch = 0
        }
        "minor" {
            $minor++
            $patch = 0
        }
        "patch" {
            $patch++
        }
    }
    
    return "${major}.${minor}.${patch}"
}

# 主函数
function Main {
    param(
        [string]$BumpType = "patch"
    )
    
    $currentVersion = Get-CurrentVersion
    $newVersion = Increment-Version -Version $currentVersion -Type $BumpType
    $timestamp = Get-Date -Format "yyyyMMddHH"
    
    Write-Host "`n当前版本：$currentVersion" -ForegroundColor Yellow
    Write-Host "新版本：$newVersion" -ForegroundColor Yellow
    Write-Host ""
    
    # 询问确认
    $confirmation = Read-Host "确认更新版本到 v${newVersion}? (y/n)"
    if ($confirmation -ne 'y' -and $confirmation -ne 'Y') {
        Write-Host "操作已取消" -ForegroundColor Red
        exit 1
    }
    
    # 更新 build.gradle.kts
    Write-Host "`n更新版本号..." -ForegroundColor Green
    $gradleFile = "app/build.gradle.kts"
    $content = Get-Content $gradleFile -Raw
    
    $content = $content -replace 'versionName = ".*"', "versionName = `"$newVersion`""
    $content = $content -replace 'versionCode = \d+', "versionCode = $timestamp"
    
    Set-Content -Path $gradleFile -Value $content -Encoding UTF8 -NoNewline
    
    Write-Host "✅ 版本号已更新" -ForegroundColor Green
    
    # 检查 git 状态
    $gitStatus = git status --porcelain
    if ($gitStatus) {
        Write-Host "`n提交版本更新..." -ForegroundColor Green
        git add $gradleFile
        git commit -m "chore: bump version to $newVersion"
        
        Write-Host "`n创建 release tag..." -ForegroundColor Green
        git tag -a "v$newVersion" -m "Release v$newVersion"
        
        Write-Host "`n✅ 版本更新成功！" -ForegroundColor Green
        Write-Host "`n下一步操作:" -ForegroundColor Cyan
        Write-Host "1. 推送代码和 tag 到 GitHub:"
        Write-Host "   git push && git push origin v$newVersion" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "2. GitHub Actions 将自动构建并发布"
        Write-Host ""
    } else {
        Write-Host "⚠️  没有需要提交的更改" -ForegroundColor Yellow
    }
}

# 参数处理
$bumpType = "patch"
if ($args.Count -gt 0) {
    $bumpType = $args[0].ToLower()
}

if ($args[0] -eq "-h" -or $args[0] -eq "--help") {
    Write-Host "用法：.\bump-version.ps1 [类型]"
    Write-Host ""
    Write-Host "类型选项:"
    Write-Host "  major  主版本号递增 (1.0.0 -> 2.0.0)"
    Write-Host "  minor  次版本号递增 (1.0.0 -> 1.1.0)"
    Write-Host "  patch  修订号递增 (1.0.0 -> 1.0.1) [默认]"
    Write-Host ""
    Write-Host "示例:"
    Write-Host "  .\bump-version.ps1        # 默认 patch"
    Write-Host "  .\bump-version.ps1 minor  # minor 版本"
    Write-Host "  .\bump-version.ps1 major  # major 版本"
    exit 0
}

Main -BumpType $bumpType
