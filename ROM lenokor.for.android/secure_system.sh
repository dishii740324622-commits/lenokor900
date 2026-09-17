#!/bin/bash

PROP_FILE="output_rom/system/build.prop"
LICENSE_FILE="output_rom/system/legal_notice.txt"

echo "--> Enforcing system security layout configurations..."

# 1. Inject software locks into build properties to prevent system inspection and debugging
if [ -f "$PROP_FILE" ]; then
    echo "ro.debuggable=0" >> $PROP_FILE
    echo "ro.secure=1" >> $PROP_FILE
    echo "persist.sys.usb.config=none" >> $PROP_FILE
    echo "persist.service.adb.enable=0" >> $PROP_FILE
    echo "ro.adb.secure=1" >> $PROP_FILE
    echo "--> System debugging ports closed permanently."
fi

# 2. Inject the strict legal proprietary ownership license statement
cat <<EOF > "$LICENSE_FILE"
===================================================================
                  LINOCORE OPERATING SYSTEM LICENSE
===================================================================
Copyright (c) 2026 LinoCore Project. All Rights Reserved.

UNAUTHORIZED INSPECTION, DECOMPILATION, MODIFICATION, DISTRIBUTING,
OR REVERSE ENGINEERING OF THIS SYSTEM TEXT, BINARIES, OR FILE MATRIX
IS STRICTLY PROHIBITED BY LAW.

THIS SOFTWARE IS LICENSED SOLELY FOR PRODUCTION RUNTIME EXECUTION
VIA THE OFFICIAL WEBSITE DISTRIBUTION PATHWAYS. ANY ATTEMPT TO 
ALTER OR INSPECT THESE FILE SYSTEMS CONSTITUTES A DIRECT VIOLATION 
OF INTELLECTUAL PROPERTY RIGHTS AND WILL FACE LEGAL ACTION.
===================================================================
EOF

chmod 444 "$LICENSE_FILE"
echo "--> Proprietary legal license successfully injected into system core."

# 3. Trigger immediate rebuild of the system partition images to apply locks
./repack_rom.sh
