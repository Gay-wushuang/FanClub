# FanClub 项目初始化脚本 (PowerShell)

Write-Host "🚀 FanClub 项目初始化脚本" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# 检查 Node.js 版本
Write-Host "📦 检查 Node.js 版本..." -ForegroundColor Yellow
$nodeVersion = (node -v).Substring(1).Split('.')[0]
if ([int]$nodeVersion -lt 20) {
    Write-Host "❌ 需要 Node.js 20.x 或更高版本" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Node.js 版本: $(node -v)" -ForegroundColor Green

# 检查 pnpm
Write-Host "📦 检查 pnpm..." -ForegroundColor Yellow
if (-not (Get-Command pnpm -ErrorAction SilentlyContinue)) {
    Write-Host "❌ 未找到 pnpm，请先安装: npm install -g pnpm" -ForegroundColor Red
    exit 1
}
Write-Host "✅ pnpm 版本: $(pnpm -v)" -ForegroundColor Green

# 安装依赖
Write-Host "📦 安装依赖..." -ForegroundColor Yellow
pnpm install

# 启动数据库
Write-Host "🐘 启动 PostgreSQL..." -ForegroundColor Yellow
pnpm db:up

# 等待数据库就绪
Write-Host "⏳ 等待数据库就绪..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 运行迁移
Write-Host "🔄 运行数据库迁移..." -ForegroundColor Yellow
pnpm db:migrate

# 填充种子数据
Write-Host "🌱 填充种子数据..." -ForegroundColor Yellow
pnpm db:seed

Write-Host ""
Write-Host "✅ 初始化完成！" -ForegroundColor Green
Write-Host ""
Write-Host "运行以下命令启动开发服务器：" -ForegroundColor Cyan
Write-Host "  pnpm dev" -ForegroundColor White
Write-Host ""


