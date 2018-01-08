package com.pilloxa.backgroundjob;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

public class ExactJobDispatcher {
  private ExactJobDispatcher() {
    // No instance
  }

  public static boolean schedule(Context context, String jobKey, long period, boolean override,
      Bundle jobBundle) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    long triggerAt = System.currentTimeMillis() + period;
    Intent intent = new Intent(context, ExactJob.class);
    intent.putExtras(jobBundle);
    PendingIntent pendingIntent = PendingIntent.getService(context, jobKey.hashCode(), intent,
        override ? PendingIntent.FLAG_CANCEL_CURRENT : PendingIntent.FLAG_ONE_SHOT);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ) {
      if(jobBundle.getBoolean("allowWhileIdle") && Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,triggerAt,pendingIntent);
      }
      else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
      }
    } else {
      // Same was alarmManager.setExact on older versions
      alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
    }
    storeScheduledExactJob(context, jobKey);
    return true;
  }

  public static boolean cancel(Context context, String jobKey) {
    removeScheduledExactJob(context, jobKey);
    return true;
  }

  public static boolean cancelAll(Context context) {
    removeAllScheduledExactJobs(context);
    return true;
  }

  private static void removeScheduledExactJob(Context context, String jobKey) {
    SharedPreferences sharedPreferences = getSharedPreferences(context);
    Set<String> jobs = sharedPreferences.getStringSet("jobs", new HashSet<String>());
    jobs.remove(jobKey);
    sharedPreferences.edit().putStringSet("jobs", jobs).apply();
  }

  private static SharedPreferences getSharedPreferences(Context context) {
    return context.getSharedPreferences(ExactJobDispatcher.class.getSimpleName(),
        Context.MODE_PRIVATE);
  }

  private static void removeAllScheduledExactJobs(Context context) {
    SharedPreferences sharedPreferences = getSharedPreferences(context);
    sharedPreferences.edit().remove("jobs").apply();
  }

  private static void storeScheduledExactJob(Context context, String jobKey) {
    SharedPreferences sharedPreferences = getSharedPreferences(context);
    Set<String> jobs = sharedPreferences.getStringSet("jobs", new HashSet<String>());
    jobs.add(jobKey);
    sharedPreferences.edit().putStringSet("jobs", jobs).apply();
  }

  public static Set<String> getScheduledExactJobs(Context context) {
    SharedPreferences sharedPreferences = getSharedPreferences(context);
    return sharedPreferences.getStringSet("jobs", new HashSet<String>());
  }
}
