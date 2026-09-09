# 安装 Android SDK
# 此脚本将安装 Android SDK Command-line Tools 和必要的组件

set -e

NDK_DIR="/d/666/android-ndk-r27d-windows"
SDK_DIR="$HOME/AppData/Local/Android/Sdk"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
CMDLINE_TOOLS_DIR="$SDK_DIR/cmdline-tools"

echo "=== 安装 Android SDK ==="
echo "SDK 目录: $SDK_DIR"

# 创建 SDK 目录
mkdir -p "$CMDLINE_TOOLS_DIR"

# 下载 command-line tools
echo "下载 command-line tools..."
curl -L "$CMDLINE_TOOLS_URL" -o /tmp/cmdline-tools.zip 2>&1 | tail -3

# 解压到临时位置
unzip -q /tmp/cmdline-tools.zip -d /tmp/cmdline-tools

# 移动到正确位置 (需要命名为 latest)
mv /tmp/cmdline-tools/cmdline-tools "$CMDLINE_TOOLS_DIR/latest"

echo "=== 安装必要的 SDK 组件 ==="
# 设置环境变量
export JAVA_HOME="/c/Program Files/Java/jdk-17.0.2"
export PATH="$SDK_DIR/cmdline-tools/latest/bin:$PATH"

# 接受许可证
yes | sdkmanager --licenses > /dev/null 2>&1 || true

# 安装必需的包
sdkmanager "platforms;android-34" "build-tools;34.0.0" "platform-tools" 2>&1

echo "=== 安装完成 ==="
echo "SDK 已安装到: $SDK_DIR"
echo ""
echo "下一步：运行构建脚本"
