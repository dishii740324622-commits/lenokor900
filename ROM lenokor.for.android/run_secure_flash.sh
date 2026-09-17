#!/bin/bash

FASTBOOT_CMD="./fastboot"

echo "--> Testing secure fastboot handshake protocol..."
$FASTBOOT_CMD devices

echo "--> Executing full system layout overwrite..."
$FASTBOOT_CMD flash boot karnil.file
$FASTBOOT_CMD flash system bootable_system.img
$FASTBOOT_CMD flash vendor bootable_vendor.img

echo "--> Wiping dynamic partition persistent cache..."
$FASTBOOT_CMD erase cache 2>/dev/null
$FASTBOOT_CMD -w

echo "--> Flashing sequence complete. Booting into secure LinoCore environment..."
$FASTBOOT_CMD reboot
