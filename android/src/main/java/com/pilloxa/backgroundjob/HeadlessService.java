package com.pilloxa.backgroundjob;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by viktor on 2016-12-13.
 */

import com.facebook.react.ReactApplication;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Timer;
import java.util.TimerTask;

public class HeadlessService extends HeadlessJsTaskService {

    private String LOG_TAG = "backgroundjob";
    private Timer mTimer;
    private Notification mNotification;
    private ReactInstanceManager reactInstanceManager;
    private ReactContext mReactContext;
    static final int NOTIFICATION_ID = 1094979487;


    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    private void sendEvent() {
        if (isAppInForeground()) {
//            Log.d(LOG_TAG, "APP IS IN FOREGROUND");
            cancelTimer();
            stopSelf();
        } else {
//            Log.d(LOG_TAG, "TIMER RAN!");
            mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("RNBackgroundJob", null);
        }
    }

    @Override
    public void onCreate() {
//        Log.d(LOG_TAG, "On create");
        super.onCreate();
    }

    public Class getMainActivityClass() {
        String packageName = mReactContext.getPackageName();
        Intent launchIntent = mReactContext.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void showNotification(Intent intent) {
        if (mTimer == null) {
            mTimer = new Timer();
//            Log.d(LOG_TAG, "CREATING NEW TIMER");
            int period = intent.getIntExtra("period", 15000);
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendEvent();
                }

            }, period, period);
        }
        if (mNotification == null) {
            Intent notificationIntent = new Intent(mReactContext, getMainActivityClass());
            PendingIntent pendingIntent = PendingIntent.getActivity(mReactContext, 0, notificationIntent, 0);
            String packageName = mReactContext.getPackageName();
            Log.d(LOG_TAG, packageName);
            String iconId = intent.getStringExtra("notificationIcon");
            String title = intent.getStringExtra("notificationTitle");
            String text = intent.getStringExtra("notificationText");
            String type = iconId != null ? "drawable" : "mipmap";
            iconId = iconId != null ? iconId : "ic_launcher";
            int smallIconResId = mReactContext
                    .getResources()
                    .getIdentifier(iconId, type, packageName);


            mNotification = new Notification.Builder(mReactContext)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(smallIconResId)
                    .setOngoing(true)
                    .build();

            startForeground(NOTIFICATION_ID, mNotification);

        }

    }

    private void setReactContext(ReactContext reactContext) {
        reactInstanceManager = getReactNativeHost().getReactInstanceManager();
        mReactContext = reactContext != null ? reactContext : reactInstanceManager.getCurrentReactContext();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (null == intent) {
            Log.e(LOG_TAG, "intent was null, flags=" + flags + " bits=" + Integer.toBinaryString(flags));
            return START_STICKY;
        }
        if (mReactContext == null) {
            setReactContext(null);
        }

//        Log.d(LOG_TAG, "onStartCommand");

        if (isAppInForeground()) {
//            Log.d(LOG_TAG, "APP IS IN FOREGROUND");
            cancelTimer();
            stopSelf();
            return Service.START_REDELIVER_INTENT;
        }

        boolean alwaysRunning = intent.getIntExtra("alwaysRunning", 0) == 1;

        HeadlessJsTaskConfig taskConfig = getTaskConfig(intent);
        if (taskConfig != null) {
            if (mReactContext == null || !alwaysRunning) {
                if (!isAppInForeground()) {
//                    Log.d(LOG_TAG, "Starting task!");
                    startTask(taskConfig);
                } else {
//                    Log.d(LOG_TAG, "Not starting task, still in bg");
                }
            }
        } else {
//            Log.d(LOG_TAG, "NOT STARTING TASK");
            return START_REDELIVER_INTENT;
        }

        if (alwaysRunning) {
            if (mReactContext == null) {
//                Log.d(LOG_TAG, "mReactContext == null");
                reactInstanceManager
                        .addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                            @Override
                            public void onReactContextInitialized(ReactContext reactContext) {
                                setReactContext(reactContext);
                                showNotification(intent);
                                reactInstanceManager.removeReactInstanceEventListener(this);
                            }
                        });
                if (!reactInstanceManager.hasStartedCreatingInitialContext()) {
//                    Log.d(LOG_TAG, "Creating React Context In Background");
                    reactInstanceManager.createReactContextInBackground();
                }
            } else {
//                Log.d(LOG_TAG, "mReactContext != null");
                showNotification(intent);
            }
            return START_STICKY;
        } else {
            return START_REDELIVER_INTENT;
        }


    }

    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
//        Log.d(LOG_TAG, "GETTING TASK CONFIG");
        Bundle extras = intent.getExtras();
        String jobKey = extras.getString("jobKey");
        int allowedInForeground = extras.getInt("allowedInForeground", 0);
        int timeout = extras.getInt("timeout");
        return new HeadlessJsTaskConfig(jobKey, Arguments.fromBundle(extras), timeout, allowedInForeground == 1);
    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) {
//        Log.d(LOG_TAG, "TASK FINISHED");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.i(LOG_TAG, "In destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isAppInForeground() {
        final ReactInstanceManager reactInstanceManager =
                ((ReactApplication) getApplication())
                        .getReactNativeHost()
                        .getReactInstanceManager();
        ReactContext reactContext =
                reactInstanceManager.getCurrentReactContext();

        return (reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED);
    }
}
