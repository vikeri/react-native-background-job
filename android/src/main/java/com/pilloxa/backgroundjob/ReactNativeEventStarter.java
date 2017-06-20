package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;

class ReactNativeEventStarter {
  private static final String LOG_TAG = ReactNativeEventStarter.class.getSimpleName();
  private final ReactNativeHost reactNativeHost;
  private final Context context;

  ReactNativeEventStarter(@NonNull Context context) {
    this.context = context;
    reactNativeHost = ((ReactApplication) context.getApplicationContext()).getReactNativeHost();
  }

  public void trigger(@NonNull Bundle jobBundle) {
    Log.d(LOG_TAG, "trigger() called with: jobBundle = [" + jobBundle + "]");
    boolean appInForeground = isAppInForeground();
    boolean allowExecutionInForeground = jobBundle.getBoolean("allowExecutionInForeground", false);
    if (!appInForeground || allowExecutionInForeground) {
      // Will execute if the app is in background, or in forground but it has permision to do so
      MyHeadlessJsTaskService.start(context, jobBundle);
    }
  }

  public static class MyHeadlessJsTaskService extends HeadlessJsTaskService {
    private static final String LOG_TAG = MyHeadlessJsTaskService.class.getSimpleName();

    @Nullable @Override protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
      Log.d(LOG_TAG, "getTaskConfig() called with: intent = [" + intent + "]");
      Bundle extras = intent.getExtras();
      boolean allowExecutionInForeground = extras.getBoolean("allowExecutionInForeground", false);
      long timeout = extras.getLong("timeout", 2000);
      return new HeadlessJsTaskConfig(intent.getStringExtra("jobKey"), Arguments.fromBundle(extras),
          timeout, allowExecutionInForeground);
    }

    public static void start(Context context, Bundle jobBundle) {
      Log.d(LOG_TAG,
          "start() called with: context = [" + context + "], jobBundle = [" + jobBundle + "]");
      Intent starter = new Intent(context, MyHeadlessJsTaskService.class);
      starter.putExtras(jobBundle);
      context.startService(starter);
    }
  }

  private boolean isAppInForeground() {
    if (!reactNativeHost.hasInstance()) {
      // If the app was force-stopped the instace will be destroyed. The instance can't be created from a background thread.
      return false;
    }
    ReactContext reactContext = reactNativeHost.getReactInstanceManager().getCurrentReactContext();
    return reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED;
  }
}
