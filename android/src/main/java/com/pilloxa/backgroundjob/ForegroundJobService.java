package com.pilloxa.backgroundjob;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class ForegroundJobService extends Service {
    private static final int NOTIFICATION_ID = 1094979487;
    private ReactNativeEventStarter reactNativeEventStarter;

    private final Timer mTimer = new Timer();


    @Override
    public void onCreate() {
        super.onCreate();
        reactNativeEventStarter = new ReactNativeEventStarter(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = buildNotification(intent.getExtras());
        scheduleTimedEvent(intent.getExtras());

        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private void scheduleTimedEvent(final Bundle jobBundle) {
        cancelTimedEvent();
        int period = jobBundle.getInt("period", 15000);
        final TimerTask mTimerTask = new TimerTask() {
            @Override
            public void run() {
                reactNativeEventStarter.trigger(jobBundle);
            }
        };
        mTimer.schedule(mTimerTask, period, period);
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
        mTimer.cancel();
        mTimer.purge();
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

    public static void start(Context context, Bundle jobBundle) {
        Intent starter = new Intent(context, ForegroundJobService.class);
        starter.putExtras(jobBundle);
        context.startService(starter);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ForegroundJobService.class));
    }
}
