#!/bin/bash
adb logcat -c
adb shell am force-stop com.backtest
adb shell am start -n com.backtest/com.backtest.MainActivity
sleep 4
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
sleep 1
#adb shell am force-stop com.backtest
adb shell am kill com.backtest
sleep 4
adb shell am start -n com.backtest/com.backtest.MainActivity
if [[ -z $(adb logcat -d | grep "Starting task!") ]]; then
  echo "Task not started"
else
  echo "Error: Task started"
  exit 1
fi
