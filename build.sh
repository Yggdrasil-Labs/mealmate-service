#!/bin/bash
# ============================================
# Build Script (Linux/Mac/CI)
# ============================================

set -e

TAG="${1:-yggdrasil-labs/midgard-backend-template:latest}"

# 检查认证信息
if [ -z "$GITHUB_ACTOR" ] || [ -z "$GITHUB_TOKEN" ]; then
    echo "请设置环境变量: GITHUB_ACTOR 和 GITHUB_TOKEN"
    echo "示例: export GITHUB_ACTOR=YoungerYang-Y"
    echo "      export GITHUB_TOKEN=ghp_xxx"
    exit 1
fi

echo "正在构建镜像: $TAG"

docker build \
    --build-arg GITHUB_ACTOR="$GITHUB_ACTOR" \
    --build-arg GITHUB_TOKEN="$GITHUB_TOKEN" \
    -t "$TAG" \
    .

echo ""
echo "构建成功！"
echo "运行命令: docker run -d --name <container-name> -p 8081:8081 -p 20880:20880 -e SPRING_PROFILES_ACTIVE=dev $TAG"

