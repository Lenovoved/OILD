#!/usr/bin/env bash
# Gradle wrapper fallback
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "gradle not found in PATH. Please install Gradle 8.11+ or run via 'gradle' command."
    exit 1
fi
