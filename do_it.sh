#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_


echo $_HOME_
cd $_HOME_

./gradlew clean
./gradlew assemble
ls -al app/build/outputs/apk/release/app-release-unsigned.apk
cp -av app/build/outputs/apk/release/app-release-unsigned.apk /tmp/aa.apk
