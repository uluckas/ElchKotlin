#!/bin/sh

SCRIPT_ROOT=$(dirname $0)

chmod a+x gradlew
./gradlew assembleDebug crashlyticsUploadDistributionDebug
#scp "$SCRIPT_ROOT/app/build/outputs/apk/app-debug.apk" rambrand:/home/uli/HockeyKit/server/php/public/com.example.elch.app
