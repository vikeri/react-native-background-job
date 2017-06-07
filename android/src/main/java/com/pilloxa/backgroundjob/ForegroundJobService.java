package com.pilloxa.backgroundjob;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ForegroundJobService extends Service {
    private static final String LOG_TAG = ForegroundJobService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1094979487;
    private ReactNativeEventStarter reactNativeEventStarter;

    @Nullable
    private Timer mTimer;


    @Override
    public void onCreate() {
        super.onCreate();
        reactNativeEventStarter = new ReactNativeEventStarter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        Notification notification = buildNotification(intent.getExtras());
        scheduleTimedEvent(intent.getExtras());

        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private void scheduleTimedEvent(final Bundle jobBundle) {
        Log.d(LOG_TAG, "scheduleTimedEvent() called with: jobBundle = [" + jobBundle + "]");
        cancelTimedEvent();
        mTimer = new Timer();
        int period = jobBundle.getInt("period", 15000);
        final TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                reactNativeEventStarter.trigger(jobBundle);
            }
        };
        mTimer.schedule(mTimerTask, TimeUnit.SECONDS.toMillis(period), TimeUnit.SECONDS.toMillis(period));
    }

    private Notification buildNotification(Bundle jobBundle) {
        Intent notificationIntent = new Intent(this, getMainActivityClass());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        String iconId = jobBundle.getString("notificationIcon");
        String title = jobBundle.getString("notificationTitle");
        String text = jobBundle.getString("notificationText");
        String type = iconId != null ? "drawable" : "mipmap";
        iconId = iconId != null ? iconId : "ic_launcher";
        int smallIconResId = getResources().getIdentifier(iconId, type, getPackageName());


        return new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setSmallIcon(smallIconResId)
                .setOngoing(true)
                .build();
    }

    private void cancelTimedEvent() {
        if (mTimer != null) {
            Log.d(LOG_TAG, "cancelTimedEvent() called");
            mTimer.cancel();
            mTimer.purge();
        }
    }

    @Nullable
    private Class getMainActivityClass() {
        String packageName = getPackageName();
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("No class found for name: " + className);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean schedule(Context context, Bundle jobBundle) {
        final boolean override = jobBundle.getBoolean("override", true);
        final boolean isRunning = isRunning(context);

        final boolean shouldSchedule;
        if (override && isRunning) {
            stop(context);
            shouldSchedule = true;
        } else {
            shouldSchedule = !isRunning;
        }

        if (shouldSchedule) {
            Intent starter = new Intent(context, ForegroundJobService.class);
            starter.putExtras(jobBundle);
            return context.startService(starter) != null;
        }
        return false;
    }

    public static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ForegroundJobService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean stop(Context context) {
        return context.stopService(new Intent(context, ForegroundJobService.class));
    }
}
