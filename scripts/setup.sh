#!/bin/bash

echo "🚀 FanClub 项目初始化脚本"
echo "================================"

# 检查 Node.js 版本
echo "📦 检查 Node.js 版本..."
node_version=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$node_version" -lt 20 ]; then
  echo "❌ 需要 Node.js 20.x 或更高版本"
  exit 1
fi
echo "✅ Node.js 版本: $(node -v)"

# 检查 pnpm
echo "📦 检查 pnpm..."
if ! command -v pnpm &> /dev/null; then
  echo "❌ 未找到 pnpm，请先安装: npm install -g pnpm"
  exit 1
fi
echo "✅ pnpm 版本: $(pnpm -v)"

# 安装依赖
echo "📦 安装依赖..."
pnpm install

# 启动数据库
echo "🐘 启动 PostgreSQL..."
pnpm db:up

# 等待数据库就绪
echo "⏳ 等待数据库就绪..."
sleep 5

# 运行迁移
echo "🔄 运行数据库迁移..."
pnpm db:migrate

# 填充种子数据
echo "🌱 填充种子数据..."
pnpm db:seed

echo ""
echo "✅ 初始化完成！"
echo ""
echo "运行以下命令启动开发服务器："
echo "  pnpm dev"
echo ""


