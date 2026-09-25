#!/usr/bin/env bash
set -e

echo "=========================================="
echo " Building Origin OS APK (com.autonavi.minimap) "
echo "=========================================="

# Ensure output directory exists
mkdir -p build-outputs

# Ensure debug keystore exists
if [ ! -f "debug.keystore" ] && [ -f "debug.keystore.base64" ]; then
    base64 -d debug.keystore.base64 > debug.keystore
fi

# Execute Gradle build
if [ -x "$(command -v gradle)" ]; then
    gradle :app:assembleDebug
elif [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    ./gradlew :app:assembleDebug
else
    echo "Error: Neither gradle nor ./gradlew found."
    exit 1
fi

# Locate built APK and copy to root output
APK_PATH=$(find app/build/outputs/apk/debug -name "*.apk" | head -n 1)

if [ -f "$APK_PATH" ]; then
    cp "$APK_PATH" "build-outputs/Origin_OS.apk"
    echo "=========================================="
    echo " Success! APK built successfully:"
    echo " Location: build-outputs/Origin_OS.apk"
    echo " Package ID: com.autonavi.minimap"
    echo " Application: Origin OS"
    echo "=========================================="
else
    echo "Error: Could not find generated APK in app/build/outputs/apk/"
    exit 1
fi
