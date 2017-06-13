package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import javax.annotation.Nullable;

class ReactNativeEventStarter {
  private static final String LOG_TAG = ReactNativeEventStarter.class.getSimpleName();
  private final ReactInstanceManager reactInstanceManager;
  private final Context context;

  ReactNativeEventStarter(@NonNull Context context) {
    this.context = context;
    reactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost()
        .getReactInstanceManager();
  }

  public void trigger(@NonNull Bundle jobBundle) {
    Log.d(LOG_TAG, "trigger() called with: jobBundle = [" + jobBundle + "]");
    boolean appInForeground = isAppInForeground();
    boolean allowExecutionInForeground = jobBundle.getBoolean("allowExecutionInForeground", false);
    if (!appInForeground || allowExecutionInForeground) {
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
    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
    return reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED;
  }

  @Nullable private ReactContext getReactContext() {
    return reactInstanceManager.getCurrentReactContext();
  }
}
