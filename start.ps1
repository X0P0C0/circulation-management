# ============================================================
# 配件流转管理系统 - 一键启动脚本
# 功能：同时启动后端 + PC前端 + H5前端
# 用法：在项目根目录执行 .\start.ps1
# 停止：按 Ctrl+C 或关闭所有窗口
# ============================================================

$projectRoot = $PSScriptRoot
$backendDir  = "$projectRoot\cm-backend"
$pcDir       = "$projectRoot\cm-pc"
$h5Dir       = "$projectRoot\cm-h5"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  配件流转管理系统 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# ---- 激活 fnm Node 版本 ----
Write-Host "[准备] 激活 Node.js ..." -ForegroundColor Yellow
fnm use 2>$null
$nodeVer = node -version 2>&1
Write-Host "  Node.js: $nodeVer" -ForegroundColor Green

# ---- 存储后台进程 ----
$processes = @()

try {
    # ---- 启动后端 ----
    Write-Host "[1/3] 启动后端 (Spring Boot) ..." -ForegroundColor Yellow
    $backendProc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$backendDir`" && mvn spring-boot:run" `
        -WorkingDirectory $backendDir `
        -PassThru `
        -WindowStyle Normal
    $processes += $backendProc
    Write-Host "  后端进程已启动 (PID: $($backendProc.Id))" -ForegroundColor Green
    Write-Host "  地址: http://localhost:8080" -ForegroundColor Gray

    # 等待后端启动几秒
    Start-Sleep -Seconds 3

    # ---- 启动 PC 前端 ----
    Write-Host "[2/3] 启动 PC 前端 (Vite) ..." -ForegroundColor Yellow
    $pcProc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$pcDir`" && fnm use 2>nul && npm run dev" `
        -WorkingDirectory $pcDir `
        -PassThru `
        -WindowStyle Normal
    $processes += $pcProc
    Write-Host "  PC 前端进程已启动 (PID: $($pcProc.Id))" -ForegroundColor Green
    Write-Host "  地址: http://localhost:5173" -ForegroundColor Gray

    # ---- 启动 H5 前端 ----
    Write-Host "[3/3] 启动 H5 前端 (Vite) ..." -ForegroundColor Yellow
    $h5Proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d `"$h5Dir`" && fnm use 2>nul && npm run dev" `
        -WorkingDirectory $h5Dir `
        -PassThru `
        -WindowStyle Normal
    $processes += $h5Proc
    Write-Host "  H5 前端进程已启动 (PID: $($h5Proc.Id))" -ForegroundColor Green
    Write-Host "  地址: http://localhost:5174" -ForegroundColor Gray

    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  全部服务已启动！" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "  后端:     http://localhost:8080" -ForegroundColor White
    Write-Host "  PC 前端:  http://localhost:5173" -ForegroundColor White
    Write-Host "  H5 前端:  http://localhost:5174" -ForegroundColor White
    Write-Host ""
    Write-Host "  默认账号: admin / admin123" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  按 Ctrl+C 停止监控（各服务在独立窗口中运行）" -ForegroundColor DarkGray
    Write-Host "  关闭各服务窗口即可停止对应服务" -ForegroundColor DarkGray
    Write-Host ""

    # ---- 监控进程状态 ----
    while ($true) {
        Start-Sleep -Seconds 5
        $allExited = $true
        foreach ($proc in $processes) {
            if (!$proc.HasExited) { $allExited = $false }
        }
        if ($allExited) {
            Write-Host "所有服务已停止。" -ForegroundColor Yellow
            break
        }
    }
} finally {
    Write-Host "清理完成。" -ForegroundColor DarkGray
}