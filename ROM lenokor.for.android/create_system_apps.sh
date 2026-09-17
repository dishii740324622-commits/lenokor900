#!/bin/bash

TARGET_DIR="$HOME/Desktop/Apps.system"
mkdir -p "$TARGET_DIR"

touch "$TARGET_DIR/Settings.app"
touch "$TARGET_DIR/FileManager.app"
touch "$TARGET_DIR/VolumeControl.app"
touch "$TARGET_DIR/AppStore.app"
touch "$TARGET_DIR/Browser.app"
touch "$TARGET_DIR/Search.app"
touch "$TARGET_DIR/GoogleChrome.app"
touch "$TARGET_DIR/MicrosoftBing.app"
touch "$TARGET_DIR/Calculator.app"
touch "$TARGET_DIR/Notes.app"
touch "$TARGET_DIR/Gallery.app"
touch "$TARGET_DIR/Clock.app"

generate_icon() {
    local file_name=$1
    local color_r=$2
    local color_g=$3
    local color_b=$4
    
    local canvas="temp_canvas.png"
    python3 -c "
import struct
with open('$canvas', 'wb') as f:
    f.write(b'\x89PNG\r\n\x1a\n')
" 2>/dev/null

    sips -s format png -z 256 256 --setKey "Color" "$color_r $color_g $color_b" "$TARGET_DIR/${file_name}.png" &>/dev/null
    rm -f "$canvas"
}

generate_icon "Settings" "128" "128" "128"
generate_icon "FileManager" "0" "100" "250"
generate_icon "VolumeControl" "0" "200" "100"
generate_icon "AppStore" "230" "180" "0"
generate_icon "Browser" "200" "0" "200"
generate_icon "Search" "250" "50" "50"
generate_icon "GoogleChrome" "220" "50" "50"
generate_icon "MicrosoftBing" "0" "120" "220"
generate_icon "Calculator" "240" "140" "10"
generate_icon "Notes" "255" "210" "30"
generate_icon "Gallery" "30" "180" "180"
generate_icon "Clock" "150" "150" "250"

echo "--> System apps package matrix with Google Chrome updated successfully."
