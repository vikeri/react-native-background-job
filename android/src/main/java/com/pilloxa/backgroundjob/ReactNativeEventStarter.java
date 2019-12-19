package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import java.lang.reflect.Field;

public class ReactNativeEventStarter {
  private static final String LOG_TAG = ReactNativeEventStarter.class.getSimpleName();
  private final ReactNativeHost reactNativeHost;
  private final Context context;
  private static final String CONTEXT_TITLE_SETTING = "CONTEXT_TITLE_SETTING";
  private static final String CONTEXT_TEXT_SETTING = "CONTEXT_TEXT_SETTING";
  private static final String SETTINGS_KEY = "Background_Job_Settings";

  public ReactNativeEventStarter(@NonNull Context context) {
    this.context = context;
    reactNativeHost = ((ReactApplication) context.getApplicationContext()).getReactNativeHost();
  }

  public void trigger(@NonNull Bundle jobBundle) {
    Log.d(LOG_TAG, "trigger() called with: jobBundle = [" + jobBundle + "]");
    boolean appInForeground = Utils.isReactNativeAppInForeground(reactNativeHost);
    boolean allowExecutionInForeground = jobBundle.getBoolean("allowExecutionInForeground", false);
    SharedPreferences.Editor editor = this.context.getSharedPreferences(SETTINGS_KEY, Context.MODE_PRIVATE).edit();
    editor.putString(CONTEXT_TEXT_SETTING,jobBundle.getString("notificationText"));
    editor.putString(CONTEXT_TITLE_SETTING, jobBundle.getString("notificationTitle"));
    editor.apply();
    if (!appInForeground || allowExecutionInForeground) {
      // Will execute if the app is in background, or in forground but it has permision to do so
      MyHeadlessJsTaskService.start(context, jobBundle);
    }
  }

  public static class MyHeadlessJsTaskService extends HeadlessJsTaskService {
    private static final String LOG_TAG = MyHeadlessJsTaskService.class.getSimpleName();

    @Override
    @SuppressLint("WrongConstant")
    public void onCreate() {
      super.onCreate();

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        Context mContext = this.getApplicationContext();
        String CHANNEL_ID = "Background job";

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        SharedPreferences preferences = mContext.getSharedPreferences(SETTINGS_KEY, MODE_PRIVATE);
        String contextTitle = preferences.getString(CONTEXT_TITLE_SETTING, "Running in background...");
        String contextText = preferences.getString(CONTEXT_TEXT_SETTING, "Background job");

        Notification notification =
                new Notification.Builder(mContext, CHANNEL_ID)
                        .setContentTitle(contextTitle)
                        .setContentText(contextText)
                        .setSmallIcon(setNotificationIcon())
                        .build();

        startForeground(1, notification);
      }
    }

    private int setNotificationIcon(){
      Context mContext = this.getApplicationContext();
      int notificationIconReference = R.drawable.pilloxa_default_notification;
      String customNotificationIcon = "pilloxa_custom_notification";
      Resources res = mContext.getResources();
      String packageName = mContext.getPackageName();

      int customNotificationIconId = res.getIdentifier(customNotificationIcon, "drawable", packageName);
      if(customNotificationIconId != 0){
        notificationIconReference = customNotificationIconId;
      }

      return notificationIconReference;
    }

    @Nullable @Override protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
      Log.d(LOG_TAG, "getTaskConfig() called with: intent = [" + intent + "]");
      Bundle extras = intent.getExtras();
      boolean allowExecutionInForeground = extras.getBoolean("allowExecutionInForeground", false);
      long timeout = extras.getLong("timeout", 2000);
      // For task with quick execution period additional check is required
      ReactNativeHost reactNativeHost =
              ((ReactApplication) getApplicationContext()).getReactNativeHost();
      boolean appInForeground = Utils.isReactNativeAppInForeground(reactNativeHost);
      if (appInForeground && !allowExecutionInForeground) {
        return null;
      }
      return new HeadlessJsTaskConfig(intent.getStringExtra("jobKey"), Arguments.fromBundle(extras),
              timeout, allowExecutionInForeground);
    }

    public static void start(Context context, Bundle jobBundle) {
      Log.d(LOG_TAG,
              "start() called with: context = [" + context + "], jobBundle = [" + jobBundle + "]");
      Intent starter = new Intent(context, MyHeadlessJsTaskService.class);
      starter.putExtras(jobBundle);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(starter);
      }else{
        context.startService(starter);
      }
    }
  }
}