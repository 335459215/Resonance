#!/bin/bash

# Resonance 版本管理脚本
# 自动递增版本号并创建 release tag

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🎵 Resonance 版本管理工具${NC}"
echo "=============================="

# 获取当前版本号
get_current_version() {
    if [ -f "app/build.gradle.kts" ]; then
        grep "versionName" app/build.gradle.kts | head -1 | sed 's/.*"\(.*\)".*/\1/'
    else
        echo "0.0.0"
    fi
}

# 递增版本号
increment_version() {
    local version=$1
    local type=${2:-patch}  # major, minor, patch
    
    IFS='.' read -r -a VERSION_ARRAY <<< "$version"
    MAJOR=${VERSION_ARRAY[0]:-0}
    MINOR=${VERSION_ARRAY[1]:-0}
    PATCH=${VERSION_ARRAY[2]:-0}
    
    case $type in
        major)
            MAJOR=$((MAJOR + 1))
            MINOR=0
            PATCH=0
            ;;
        minor)
            MINOR=$((MINOR + 1))
            PATCH=0
            ;;
        patch)
            PATCH=$((PATCH + 1))
            ;;
    esac
    
    echo "${MAJOR}.${MINOR}.${PATCH}"
}

# 主函数
main() {
    local bump_type=${1:-patch}
    
    CURRENT_VERSION=$(get_current_version)
    NEW_VERSION=$(increment_version "$CURRENT_VERSION" "$bump_type")
    
    echo -e "${YELLOW}当前版本:${NC} $CURRENT_VERSION"
    echo -e "${YELLOW}新版本:${NC} $NEW_VERSION"
    echo ""
    
    # 询问确认
    read -p "确认更新版本到 v${NEW_VERSION}? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo -e "${RED}操作已取消${NC}"
        exit 1
    fi
    
    # 更新 build.gradle.kts
    echo -e "${GREEN}更新版本号...${NC}"
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/versionName = \".*\"/versionName = \"${NEW_VERSION}\"/" app/build.gradle.kts
        sed -i '' "s/versionCode = [0-9]*/versionCode = $(date +%Y%m%d%H)/" app/build.gradle.kts
    else
        # Linux
        sed -i "s/versionName = \".*\"/versionName = \"${NEW_VERSION}\"/" app/build.gradle.kts
        sed -i "s/versionCode = [0-9]*/versionCode = $(date +%Y%m%d%H)/" app/build.gradle.kts
    fi
    
    # 提交更改
    echo -e "${GREEN}提交版本更新...${NC}"
    git add app/build.gradle.kts
    git commit -m "chore: bump version to ${NEW_VERSION}"
    
    # 创建 tag
    echo -e "${GREEN}创建 release tag...${NC}"
    git tag -a "v${NEW_VERSION}" -m "Release v${NEW_VERSION}"
    
    echo ""
    echo -e "${GREEN}✅ 版本更新成功！${NC}"
    echo ""
    echo "下一步操作:"
    echo "1. 推送代码和 tag 到 GitHub:"
    echo -e "   ${YELLOW}git push && git push origin v${NEW_VERSION}${NC}"
    echo ""
    echo "2. 或者手动推送到远程仓库"
    echo ""
}

# 显示帮助
show_help() {
    echo "用法：./bump-version.sh [类型]"
    echo ""
    echo "类型选项:"
    echo "  major  主版本号递增 (1.0.0 -> 2.0.0)"
    echo "  minor  次版本号递增 (1.0.0 -> 1.1.0)"
    echo "  patch  修订号递增 (1.0.0 -> 1.0.1) [默认]"
    echo ""
    echo "示例:"
    echo "  ./bump-version.sh        # 默认 patch"
    echo "  ./bump-version.sh minor  # minor 版本"
    echo "  ./bump-version.sh major  # major 版本"
}

# 参数检查
if [ "$1" == "-h" ] || [ "$1" == "--help" ]; then
    show_help
    exit 0
fi

main "$@"
