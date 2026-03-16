# Resonance 项目重命名脚本
# 将所有 com.embyplayer 替换为 com.resonance

$projectPath = "g:\project\audio player"
$oldPackage = "com.embyplayer"
$newPackage = "com.resonance"

Write-Host "开始更新包名..." -ForegroundColor Green
Write-Host "从：$oldPackage" -ForegroundColor Yellow
Write-Host "到：$newPackage" -ForegroundColor Yellow
Write-Host ""

# 获取所有 Kotlin 和 Java 文件
$files = Get-ChildItem -Path $projectPath -Include *.kt,*.java,*.kts -Recurse -File

$count = 0
$total = $files.Count

foreach ($file in $files) {
    $count++
    $percent = [math]::Round(($count / $total) * 100, 2)
    Write-Progress -Activity "更新文件中..." -Status "$percent% 完成 ($count/$total)" -PercentComplete $percent
    
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        
        if ($content -match [regex]::Escape($oldPackage)) {
            $newContent = $content -replace [regex]::Escape($oldPackage), $newPackage
            Set-Content -Path $file.FullName -Value $newContent -Encoding UTF8 -NoNewline
            Write-Host "  ✓ 更新：$($file.Name)" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "  ✗ 错误：$($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "包名更新完成！" -ForegroundColor Green
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Cyan
Write-Host "1. 在文件资源管理器中手动重命名文件夹：'audio player' -> 'Resonance'" -ForegroundColor Yellow
Write-Host "2. 打开 IDE 并重新导入项目" -ForegroundColor Yellow
Write-Host "3. 运行 ./gradlew clean 清理项目" -ForegroundColor Yellow
Write-Host "4. 运行 ./gradlew assembleDebug 编译项目" -ForegroundColor Yellow
