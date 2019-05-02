#!/bin/bash
#
# Creates the "RText.icns" file needed for the OS X app bundle.
# After running "./gradlew generateMacApp", right-click the
# generated RText.app folder, click "", then drag-and-drop
# this "RText.icns" file to the top-left icon in the modal.
# This will set the icons used for the app bundle.
# Not sure if there's a way to programmatically do this.
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd ${SCRIPT_DIR}
iconutil -c icns ./rtext.iconset
