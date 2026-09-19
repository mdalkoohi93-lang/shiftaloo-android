#!/usr/bin/env bash
set -euo pipefail
mkdir -p qa-output
adb shell wm size 1080x2400
adb shell wm density 440
adb shell settings put system font_scale 1.0
adb shell settings put global airplane_mode_on 1
adb shell svc wifi disable
adb shell svc data disable
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.shiftaloo.app.test/com.shiftaloo.app.SmokeRunner | tee qa-output/test-results.txt
adb pull /sdcard/Android/data/com.shiftaloo.app/files/screenshots qa-output/ || true
adb logcat -d -s AndroidRuntime:E > qa-output/crashes.txt
rg 'SHIFTALOO_OK' qa-output/test-results.txt
