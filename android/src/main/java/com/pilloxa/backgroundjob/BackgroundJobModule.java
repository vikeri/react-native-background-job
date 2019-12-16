package com.pilloxa.backgroundjob;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import androidx.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.firebase.jobdispatcher.FirebaseJobDispatcher.CANCEL_RESULT_SUCCESS;
import static com.firebase.jobdispatcher.FirebaseJobDispatcher.SCHEDULE_RESULT_SUCCESS;

class BackgroundJobModule extends ReactContextBaseJavaModule {
  private static final String LOG_TAG = BackgroundJobModule.class.getSimpleName();

  private static final String NETWORK_TYPE_UNMETERED = "UNMETERED";
  private static final String NETWORK_TYPE_ANY = "ANY";

  private FirebaseJobDispatcher mJobDispatcher;

  @Override public void initialize() {
    super.initialize();
    Log.d(LOG_TAG, "Initializing BackgroundJob");
    if (mJobDispatcher == null) {
      mJobDispatcher =
          new FirebaseJobDispatcher(new GooglePlayDriver(getReactApplicationContext()));
    }
  }

  BackgroundJobModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @ReactMethod
  public void schedule(String jobKey, int timeout, int period, boolean persist, boolean override,
      int networkType, boolean requiresCharging, boolean requiresDeviceIdle, boolean exact,boolean allowWhileIdle,
      boolean allowExecutionInForeground, String notificationTitle, String notificationText, Callback callback) {
    final Bundle jobBundle = new Bundle();
    jobBundle.putString("jobKey", jobKey);
    jobBundle.putString("notificationTitle", notificationTitle);
    jobBundle.putString("notificationText", notificationText);
    jobBundle.putLong("timeout", timeout);
    jobBundle.putBoolean("persist", persist);
    jobBundle.putBoolean("override", override);
    jobBundle.putLong("period", period);
    jobBundle.putInt("networkType", networkType);
    jobBundle.putBoolean("allowWhileIdle",allowWhileIdle);
    jobBundle.putBoolean("requiresCharging", requiresCharging);
    jobBundle.putBoolean("requiresDeviceIdle", requiresDeviceIdle);
    jobBundle.putBoolean("allowExecutionInForeground", allowExecutionInForeground);

    Log.d(LOG_TAG, "Scheduling job with:" + jobBundle.toString());

    final boolean scheduled;
    if (exact) {
      scheduled = scheduleExactJob(jobKey, period, override, jobBundle);
    } else {
      scheduled =
          scheduleBackgroundJob(jobKey, period, persist, override, networkType, requiresCharging,
              jobBundle);
    }
    callback.invoke(scheduled);
  }

  /**
   * Schedule a new background job that will be triggered via {@link FirebaseJobDispatcher}.
   */
  private boolean scheduleBackgroundJob(String jobKey, int period, boolean persist,
      boolean override, int networkType, boolean requiresCharging, Bundle jobBundle) {
    int periodInSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(period);
    Job.Builder jobBuilder = mJobDispatcher.newJobBuilder()
        .setService(BackgroundJob.class)
        .setExtras(jobBundle)
        .setTag(jobKey)
        .setTrigger(Trigger.executionWindow(periodInSeconds, periodInSeconds))
        .setLifetime(persist ? Lifetime.FOREVER : Lifetime.UNTIL_NEXT_BOOT)
        .setRecurring(true)
        .setReplaceCurrent(override)
        .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR);
    if (requiresCharging) {
      jobBuilder.addConstraint(Constraint.DEVICE_CHARGING);
    }
    if (networkType == Constraint.ON_ANY_NETWORK || networkType == Constraint.ON_UNMETERED_NETWORK) {
      jobBuilder.addConstraint(networkType);
    }
    if (mJobDispatcher.schedule(jobBuilder.build()) == SCHEDULE_RESULT_SUCCESS) {
      Log.d(LOG_TAG, "Successfully scheduled: " + jobKey);
      return true;
    } else {
      Log.w(LOG_TAG, "Failed to schedule: " + jobKey);
      return false;
    }
  }

  /**
   * Similar to scheduleBackgroundJob but will use simple custom implementation that will trigger
   * the job in the exact manner.
   */
  private boolean scheduleExactJob(String jobKey, long period, boolean override, Bundle jobBundle) {
    return ExactJobDispatcher.schedule(getReactApplicationContext(), jobKey, period, override,
        jobBundle);
  }

  @ReactMethod public void cancel(String jobKey, Callback callback) {
    Log.d(LOG_TAG, "Cancelling job: " + jobKey);
    boolean canceled = ExactJobDispatcher.cancel(getReactApplicationContext(), jobKey);
    canceled = mJobDispatcher.cancel(jobKey) == CANCEL_RESULT_SUCCESS || canceled;
    callback.invoke(canceled);
  }

  @ReactMethod public void cancelAll(Callback callback) {
    Log.d(LOG_TAG, "Cancelling all jobs");
    final boolean exactCanceled = ExactJobDispatcher.cancelAll(getReactApplicationContext());
    final boolean allBackgroundCanceled = mJobDispatcher.cancelAll() == CANCEL_RESULT_SUCCESS;
    callback.invoke(exactCanceled && allBackgroundCanceled);
  }
  @ReactMethod public void isAppIgnoringBatteryOptimization(Callback callback){
    String packageName = getReactApplicationContext().getPackageName();
    PowerManager pm = (PowerManager) getReactApplicationContext().getSystemService(Context.POWER_SERVICE);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      callback.invoke(pm.isIgnoringBatteryOptimizations(packageName));
    }else{
      callback.invoke(true);
    }
  }
  @Override public String getName() {
    return "BackgroundJob";
  }

  @Nullable @Override public Map<String, Object> getConstants() {
    Log.d(LOG_TAG, "Getting constants");
    HashMap<String, Object> constants = new HashMap<>();
    constants.put(NETWORK_TYPE_UNMETERED, Constraint.ON_UNMETERED_NETWORK);
    constants.put(NETWORK_TYPE_ANY, Constraint.ON_ANY_NETWORK);
    return constants;
  }
}
