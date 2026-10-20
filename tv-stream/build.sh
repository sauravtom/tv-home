#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_HOME:?Set ANDROID_HOME}"
BT="$SDK/build-tools/${BUILD_TOOLS_VERSION:-35.0.0}"
JAR="$SDK/platforms/android-34/android.jar"
for app in bookreels ramen; do
 B="build/$app"
 mkdir -p "$B/res/drawable" "$B/res/values" "$B/gen" "$B/classes" "$B/dex"
 label=BookReels
 color='#40B8A9'
 [ "$app" = ramen ] && label=Ramen && color='#F28160'
 cat > "$B/res/drawable/banner.xml" <<XML
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle"><solid android:color="$color"/><size android:width="320dp" android:height="180dp"/></shape>
XML
 cat > "$B/AndroidManifest.xml" <<XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="in.xagi.${app}.tv" android:versionCode="1" android:versionName="1.0"><uses-permission android:name="android.permission.INTERNET"/><uses-feature android:name="android.software.leanback" android:required="true"/><uses-feature android:name="android.hardware.touchscreen" android:required="false"/><application android:label="$label" android:icon="@drawable/banner" android:banner="@drawable/banner" android:allowBackup="false" android:theme="@android:style/Theme.Material.NoActionBar" android:usesCleartextTraffic="false"><activity android:name="in.xagi.tvstream.MainActivity" android:exported="true" android:screenOrientation="landscape"><intent-filter><action android:name="android.intent.action.MAIN"/><category android:name="android.intent.category.LEANBACK_LAUNCHER"/><category android:name="android.intent.category.LAUNCHER"/></intent-filter></activity></application></manifest>
XML
 "$BT/aapt2" compile --dir "$B/res" -o "$B/res.zip"
 "$BT/aapt2" link -o "$B/base.apk" -I "$JAR" --manifest "$B/AndroidManifest.xml" --java "$B/gen" --min-sdk-version 28 --target-sdk-version 28 "$B/res.zip"
 javac --release 11 -classpath "$JAR" -d "$B/classes" src/in/xagi/tvstream/MainActivity.java
 "$BT/d8" --min-api 28 --lib "$JAR" --output "$B/dex" $(find "$B/classes" -name '*.class')
 cp "$B/base.apk" "$B/unsigned.apk"
 (cd "$B/dex" && zip -q -u ../unsigned.apk classes.dex)
 "$BT/zipalign" -f 4 "$B/unsigned.apk" "$B/aligned.apk"
 "$BT/apksigner" sign --ks ../launcher/debug.keystore --ks-pass pass:android --out "$B/$app-tv.apk" "$B/aligned.apk"
 wc -c "$B/$app-tv.apk"
done
