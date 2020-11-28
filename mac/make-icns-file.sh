#!/bin/bash
#
# Creates the "RText.icns" file needed for the OS X app bundle.
# Note that this file is already generated, but this script is
# here for posterity, and/or for when we decide to change the
# icon for the application.
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "${SCRIPT_DIR}" || exit 1
iconutil -c icns ./rtext.iconset
