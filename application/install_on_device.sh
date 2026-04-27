#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
export PATH="${ANDROID_HOME:-/workspace/android-sdk}/platform-tools:$PATH"
APK="${ROOT}/app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -f "$APK" ]]; then
  echo "Сначала собери APK: cd \"$ROOT\" && ./gradlew assembleDebug" >&2
  exit 1
fi
adb devices
adb install -r "$APK"
echo "Готово: приложение установлено на подключённое устройство или эмулятор."
