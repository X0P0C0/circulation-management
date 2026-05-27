# ============================================================
# 配件流转管理系统 - 一键停止脚本
# 功能：停止所有后端/前端进程
# 用法：.\stop.ps1
# ============================================================

Write-Host ""
Write-Host "正在停止所有服务..." -ForegroundColor Yellow

# 停止 Java (Spring Boot)
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force
    Write-Host "  OK: 后端服务已停止" -ForegroundColor Green
} else {
    Write-Host "  后端服务未在运行" -ForegroundColor Gray
}

# 停止 Node (Vite dev server)
$nodeProcs = Get-Process -Name "node" -ErrorAction SilentlyContinue
if ($nodeProcs) {
    $nodeProcs | Stop-Process -Force
    Write-Host "  OK: 前端服务已停止" -ForegroundColor Green
} else {
    Write-Host "  前端服务未在运行" -ForegroundColor Gray
}

Write-Host ""
Write-Host "全部服务已停止。" -ForegroundColor Green
Write-Host ""