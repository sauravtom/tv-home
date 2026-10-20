#!/usr/bin/env bash
# Re-enable only the optional extras from our OnePlus debloat list.
set -euo pipefail
cd "$(dirname "$0")/.."
ADB="${ADB:-adb}"
: "${TV_SERIAL:?Set TV_SERIAL to the TV ADB address}"
while IFS= read -r package; do
  [[ -z "$package" || "$package" == \#* ]] && continue
  "$ADB" -s "$TV_SERIAL" shell pm enable --user 0 "$package"
done < examples/oneplus-y-series/debloat.txt
