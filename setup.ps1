# ============================================================
# 配件流转管理系统 - 首次环境初始化脚本
# 功能：检查环境、安装前端依赖
# 用法：在项目根目录执行 .\setup.ps1
# ============================================================

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  配件流转管理系统 - 环境初始化" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = $PSScriptRoot
$hasError = $false

# ---- 检查 Java ----
Write-Host "[1/5] 检查 Java 环境..." -ForegroundColor Yellow
try {
    $javaVer = java -version 2>&1 | Select-Object -First 1
    Write-Host "  OK: $javaVer" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: 未检测到 Java，请安装 JDK 17+" -ForegroundColor Red
    $hasError = $true
}

# ---- 检查 Maven ----
Write-Host "[2/5] 检查 Maven 环境..." -ForegroundColor Yellow
try {
    $mvnVer = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "  OK: $mvnVer" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: 未检测到 Maven" -ForegroundColor Red
    $hasError = $true
}

# ---- 检查 fnm + Node.js ----
Write-Host "[3/5] 检查 Node.js 环境 (fnm)..." -ForegroundColor Yellow
try {
    $nodeVer = node -version 2>&1
    Write-Host "  OK: Node.js $nodeVer" -ForegroundColor Green
} catch {
    Write-Host "  WARN: 当前 shell 未找到 node，尝试通过 fnm 激活..." -ForegroundColor DarkYellow
    fnm use --install-if-missing 2>$null
    $nodeVer = node -version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  OK: Node.js $nodeVer (via fnm)" -ForegroundColor Green
    } else {
        Write-Host "  ERROR: 无法激活 Node.js，请检查 fnm 配置" -ForegroundColor Red
        $hasError = $true
    }
}

# ---- 检查 MySQL ----
Write-Host "[4/5] 检查 MySQL 连接..." -ForegroundColor Yellow
try {
    $mysqlVer = mysql --version 2>&1
    Write-Host "  OK: $mysqlVer" -ForegroundColor Green
    Write-Host "  注意：请确保已执行 sql/init.sql 初始化数据库" -ForegroundColor DarkYellow
} catch {
    Write-Host "  WARN: 未检测到 mysql 命令行工具（不影响应用启动）" -ForegroundColor DarkYellow
}

# ---- 安装前端依赖 ----
if (-not $hasError) {
    Write-Host "[5/5] 安装前端依赖..." -ForegroundColor Yellow

    Write-Host "  -> cm-pc: npm install ..." -ForegroundColor Gray
    Push-Location "$projectRoot\cm-pc"
    npm install --silent 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { Write-Host "  OK: cm-pc 依赖安装完成" -ForegroundColor Green }
    else { Write-Host "  ERROR: cm-pc 依赖安装失败" -ForegroundColor Red; $hasError = $true }
    Pop-Location

    Write-Host "  -> cm-h5: npm install ..." -ForegroundColor Gray
    Push-Location "$projectRoot\cm-h5"
    npm install --silent 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) { Write-Host "  OK: cm-h5 依赖安装完成" -ForegroundColor Green }
    else { Write-Host "  ERROR: cm-h5 依赖安装失败" -ForegroundColor Red; $hasError = $true }
    Pop-Location
} else {
    Write-Host "[5/5] 跳过依赖安装（环境检查未通过）" -ForegroundColor Red
}

Write-Host ""
if ($hasError) {
    Write-Host "初始化存在错误，请修复后重新执行。" -ForegroundColor Red
} else {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  初始化完成！执行 .\start.ps1 启动系统" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
}
Write-Host ""