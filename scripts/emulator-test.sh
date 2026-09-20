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
adb shell pm grant com.shiftaloo.app android.permission.POST_NOTIFICATIONS
adb shell appops set com.shiftaloo.app SCHEDULE_EXACT_ALARM allow
adb shell am instrument -w com.shiftaloo.app.test/com.shiftaloo.app.SmokeRunner | tee qa-output/test-results.txt
adb pull /sdcard/Android/data/com.shiftaloo.app/files/screenshots qa-output/ || true
adb logcat -d -s AndroidRuntime:E > qa-output/crashes.txt
grep -q 'SHIFTALOO_OK' qa-output/test-results.txt
adb shell wm density 480
adb shell settings put system font_scale 1.3
adb shell am instrument -w -e mode large com.shiftaloo.app.test/com.shiftaloo.app.SmokeRunner | tee qa-output/large-font-results.txt
adb pull /sdcard/Android/data/com.shiftaloo.app/files/screenshots qa-output/ || true
grep -q 'SHIFTALOO_OK' qa-output/large-font-results.txt
