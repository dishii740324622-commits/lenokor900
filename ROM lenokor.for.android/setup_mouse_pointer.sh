#!/bin/bash
POINTER_DIR="output_rom/system/framework/framework-res.apk/res/drawable"
mkdir -p "$POINTER_DIR"
cat <<EOT > "output_rom/system/build.prop"
ro.hardware.pointer_speed=0
persist.sys.pointer_speed=0
ro.min.pointer_acceleration=1
ro.max.pointer_acceleration=3
EOT
touch "$POINTER_DIR/pointer_arrow.png"
touch "$POINTER_DIR/pointer_icon_vector.xml"
