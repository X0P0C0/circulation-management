#!/bin/bash
# CM 后端服务重启脚本
# 用法: ./restart.sh

echo "=============================="
echo "  CM Backend Restart Script"
echo "=============================="

echo "[1/3] 正在停止服务..."
sudo systemctl stop cm

echo "[2/3] 等待 2 秒..."
sleep 2

echo "[3/3] 正在启动服务..."
sudo systemctl start cm

echo "=============================="
echo "  服务状态："
sudo systemctl status cm --no-pager | head -10
echo ""
echo "  查看实时日志: journalctl -u cm -f"
echo "=============================="
