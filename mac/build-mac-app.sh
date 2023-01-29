#!/bin/bash
#
# Builds RText.app for OS X.  This assumes you've already run
# ./gradlew clean build installDist.  In fact, you usually don't
# run this directly but rather just run
# ./gradlew clean build installDist generateJre generateMacApp
#

# The version of RText you're building.  This appears in the generated
# .dmg file name.
APP_VERSION=6.0.1

#
# You probably don't want to change anything below this line.
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "${SCRIPT_DIR}/../build/install" || exit

jpackage --input rtext \
  --icon "${SCRIPT_DIR}/RText.icns" \
  --name RText \
  --app-version "${APP_VERSION}" \
  --main-class org.fife.rtext.Main \
  --main-jar RText.jar
