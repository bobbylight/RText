#!/bin/bash
#
# Builds RText.app for OS X.
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
APP_DIR=${SCRIPT_DIR}/../build/install/RText.app

cd ${SCRIPT_DIR}

rm -fr ${APP_DIR}
mkdir ${APP_DIR}
cp ./RText ${APP_DIR}/
chmod 755 ${APP_DIR}/RText
cp -r ${APP_DIR}/../rtext ${APP_DIR}/app
