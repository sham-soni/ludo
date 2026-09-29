#!/bin/bash
# Builds the APK without Gradle, using the Debian/Ubuntu Android SDK packages:
#   apt-get install android-sdk-build-tools android-sdk-platform-23 dalvik-exchange apksigner zipalign
set -euo pipefail
cd "$(dirname "$0")"
SDK=${ANDROID_JAR:-/usr/lib/android-sdk/platforms/android-23/android.jar}
OUT=build
rm -rf "$OUT" && mkdir -p "$OUT/classes" "$OUT/res" "$OUT/gen"

# launcher icons
javac -d "$OUT/tools" tools/IconGen.java
java -Djava.awt.headless=true -cp "$OUT/tools" IconGen res
# sound effects
javac -d "$OUT/tools" tools/SfxGen.java
java -cp "$OUT/tools" SfxGen assets/sfx

# resources
aapt2 compile --dir res -o "$OUT/res.zip"
aapt2 link -o "$OUT/base.apk" -I "$SDK" --manifest AndroidManifest.xml \
  --min-sdk-version 21 --target-sdk-version 34 --java "$OUT/gen" -A assets -0 wav "$OUT/res.zip"

# code
javac -source 8 -target 8 -nowarn -Xlint:-options -bootclasspath "$SDK" -d "$OUT/classes" \
  $(find src "$OUT/gen" -name '*.java')
dalvik-exchange --dex --min-sdk-version=21 --output="$OUT/classes.dex" "$OUT/classes"

# package, align, sign
cp "$OUT/base.apk" "$OUT/unsigned.apk"
(cd "$OUT" && zip -q -j unsigned.apk classes.dex)
zipalign -f -p 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
KS=${KEYSTORE:-debug.keystore}
if [ ! -f "$KS" ]; then
  keytool -genkeypair -keystore "$KS" -storepass android -keypass android -alias ludo \
    -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Ludo Crown,O=Ludo Crown,C=US"
fi
apksigner sign --ks "$KS" --ks-pass pass:android --key-pass pass:android --ks-key-alias ludo \
  --out "$OUT/LudoCrown.apk" "$OUT/aligned.apk"
apksigner verify "$OUT/LudoCrown.apk"
echo "APK: $(pwd)/$OUT/LudoCrown.apk"
