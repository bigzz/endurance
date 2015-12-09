#/bin/sh

adb root 
sleep 5
adb shell "chmod 444 /sys/kernel/debug/mmc0/mmc0:0001/ext_csd"
adb shell setenforce 0

