#!/bin/bash
adb logcat -c
adb shell am force-stop com.backtest
adb shell am start -n com.backtest/com.backtest.MainActivity
sleep 4
adb shell am start -a android.intent.action.MAIN -c android.intent.category.HOME
sleep 1
#adb shell am force-stop com.backtest
adb shell am kill com.backtest
sleep 15
adb shell am start -n com.backtest/com.backtest.MainActivity
if [[ -z $(adb logcat -d | grep "Background Job fired!") ]]; then
  echo "Error: Task not started"
  exit 1
else
  echo "Task started"
fi
