#!/bin/bash

echo "========================================"
echo " Weasis Measure Enhance Plugin Launcher"
echo "========================================"
echo

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Find Weasis executable
if [ -x "/usr/bin/weasis" ]; then
    WEASIS_EXE="/usr/bin/weasis"
elif [ -x "/usr/local/bin/weasis" ]; then
    WEASIS_EXE="/usr/local/bin/weasis"
elif [ -x "$HOME/Applications/Weasis.app/Contents/MacOS/Weasis" ]; then
    WEASIS_EXE="$HOME/Applications/Weasis.app/Contents/MacOS/Weasis"
elif [ -x "/Applications/Weasis.app/Contents/MacOS/Weasis" ]; then
    WEASIS_EXE="/Applications/Weasis.app/Contents/MacOS/Weasis"
else
    echo "ERROR: Weasis not found!"
    echo "Please install Weasis from: https://weasis.org/"
    exit 1
fi

# Check if plugin JAR exists
PLUGIN_JAR="$PROJECT_DIR/target/weasis-measure-enhance-1.0.0-SNAPSHOT.jar"
if [ ! -f "$PLUGIN_JAR" ]; then
    echo "Plugin JAR not found. Building..."
    echo
    
    if command -v mvn &> /dev/null; then
        cd "$PROJECT_DIR"
        mvn clean package -DskipTests
        if [ $? -ne 0 ]; then
            echo "Build failed!"
            exit 1
        fi
    else
        echo "ERROR: Maven not found and plugin JAR does not exist!"
        echo "Please build the plugin first or install Maven."
        exit 1
    fi
fi

# Create temporary config file
CONFIG_FILE="/tmp/weasis-measure-enhance-config.json"
cat > "$CONFIG_FILE" << EOF
{
  "weasisPreferences": [
    {
      "code": "felix.auto.start.13",
      "value": "file://$PLUGIN_JAR",
      "description": "Weasis Measure Enhance Plugin"
    }
  ]
}
EOF

echo "Starting Weasis with Measure Enhance Plugin..."
echo "Plugin: $PLUGIN_JAR"
echo

"$WEASIS_EXE" '$weasis:config pro="felix.extended.config.properties file://'"$CONFIG_FILE"'"' &

echo "Weasis started with plugin loaded."
