#!/usr/bin/env bash
# Restore the OnePlus Y Series stock Home handler; leave the custom app installed.
set -euo pipefail
ADB="${ADB:-adb}"
: "${TV_SERIAL:?Set TV_SERIAL to the TV ADB address}"
"$ADB" -s "$TV_SERIAL" shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
"$ADB" -s "$TV_SERIAL" shell input keyevent KEYCODE_HOME
