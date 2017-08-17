package com.pilloxa.backgroundjob;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;
import com.facebook.react.bridge.*;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.common.LifecycleState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundJobModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private String LOG_TAG = "backgroundjob";
    private static final String NETWORK_TYPE_UNMETERED = "UNMETERED";
    private static final String NETWORK_TYPE_NONE = "NONE";
    private static final String NETWORK_TYPE_ANY = "ANY";

    private final ReactApplicationContext reactContext;

    private List<JobInfo> mJobs;
    private Bundle mJobBundle;

    private JobScheduler jobScheduler;

    private Intent mService;

    @Override
    public void initialize() {
        Log.d(LOG_TAG, "Initializing BackgroundJob");
        if (jobScheduler == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobScheduler = (JobScheduler) reactContext
                    .getSystemService(Context.JOB_SCHEDULER_SERVICE);
            mJobs = jobScheduler.getAllPendingJobs();
        }
        super.initialize();
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    public BackgroundJobModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void schedule(String jobKey,
                         int timeout,
                         int period,
                         boolean persist,
                         int networkType,
                         boolean requiresCharging,
                         boolean requiresDeviceIdle,
                         boolean alwaysRunning,
                         boolean allowedInForeground,
                         String title,
                         String icon,
                         String text) {
        int taskId = jobKey.hashCode();

        Log.v(LOG_TAG,
                String.format("Scheduling: %s, timeout: %s, period: %s, network type: %s, requiresCharging: %s, requiresDeviceIdle: %s, alwaysRunning: %s, allowedInForeground: %s, notificationTitle: %s, notificationText %s, notificationIcon: %s",
                        jobKey,
                        timeout,
                        period,
                        networkType,
                        requiresCharging,
                        requiresDeviceIdle,
                        alwaysRunning,
                        allowedInForeground,
                        title,
                        text,
                        icon));

        int persistInt = persist ? 1 : 0;

        ComponentName componentName = new ComponentName(getReactApplicationContext(), BackgroundJob.class.getName());
        PersistableBundle jobExtras = new PersistableBundle();
        jobExtras.putString("jobKey", jobKey);
        jobExtras.putString("notificationTitle", title);
        jobExtras.putString("notificationIcon", icon);
        jobExtras.putString("notificationText", text);
        jobExtras.putInt("timeout", timeout);
        jobExtras.putInt("persist", persistInt);
        jobExtras.putInt("period", period);
        jobExtras.putInt("networkType", networkType);
        jobExtras.putInt("requiresCharging", requiresCharging ? 1 : 0);
        jobExtras.putInt("requiresDeviceIdle", requiresDeviceIdle ? 1 : 0);
        jobExtras.putInt("alwaysRunning", alwaysRunning ? 1 : 0);
        jobExtras.putInt("allowedInForeground", allowedInForeground ? 1 : 0);
        if (alwaysRunning) {
            mJobBundle = new Bundle(jobExtras);
        } else {
            mJobBundle = null;
        }

        JobInfo jobInfo = new JobInfo.Builder(taskId, componentName)
                .setExtras(jobExtras)
                .setRequiresDeviceIdle(requiresDeviceIdle)
                .setRequiresCharging(requiresCharging)
                .setPersisted(persist)
                .setPeriodic(period)
                .setRequiredNetworkType(networkType)
                .build();

        for (JobInfo iJobInfo : mJobs) {
            if (iJobInfo.getId() == taskId) {
                mJobs.remove(iJobInfo);
            }
        }
        mJobs.add(jobInfo);

        if (!isAppInForeground()) {
            scheduleJobs();
            startForegroundJob();
        }

    }


    @ReactMethod
    public void cancel(String jobKey) {
        int taskId = jobKey.hashCode();
        Log.d(LOG_TAG, "Cancelling job: " + jobKey + " (" + taskId + ")");
        jobScheduler.cancel(taskId);
        mJobs = jobScheduler.getAllPendingJobs();
        if (mJobBundle != null && mJobBundle.getString("jobKey") == jobKey) {
            cancelService();
        }
    }

    @ReactMethod
    public void cancelAll() {
        Log.d(LOG_TAG, "Cancelling all jobs");
        jobScheduler.cancelAll();
        mJobs = jobScheduler.getAllPendingJobs();
        cancelService();
    }

    private WritableArray _getAll() {
        Log.d(LOG_TAG, "Getting all jobs");
        WritableArray jobs = Arguments.createArray();
        if (mJobs != null) {
            for (JobInfo job : mJobs) {
                Log.d(LOG_TAG, "Fetching job " + job.getId());
                Bundle extras = new Bundle(job.getExtras());
                WritableMap jobMap = Arguments.fromBundle(extras);
                jobMap.putBoolean("persist", extras.getInt("persist") == 1);
                jobMap.putBoolean("requiresCharging", extras.getInt("requiresCharging") == 1);
                jobMap.putBoolean("requiresDeviceIdle", extras.getInt("requiresDeviceIdle") == 1);
                jobMap.putBoolean("alwaysRunning", extras.getInt("alwaysRunning") == 1);
                jobs.pushMap(jobMap);
            }
        }

        return jobs;
    }

    @ReactMethod
    public void getAll(Callback callback) {
        WritableArray jobs = _getAll();
        callback.invoke(jobs);
    }

    @Override
    public String getName() {
        return "BackgroundJob";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Log.d(LOG_TAG, "Getting constants");
        jobScheduler = (JobScheduler) getReactApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            mJobs = jobScheduler.getAllPendingJobs();
        }
        HashMap<String, Object> constants = new HashMap<>();
        constants.put("jobs", _getAll());
        constants.put(NETWORK_TYPE_UNMETERED, JobInfo.NETWORK_TYPE_UNMETERED);
        constants.put(NETWORK_TYPE_ANY, JobInfo.NETWORK_TYPE_ANY);
        constants.put(NETWORK_TYPE_NONE, JobInfo.NETWORK_TYPE_NONE);
        return constants;
    }


    private void cancelService() {
        stopService();
        mJobBundle = null;
    }

    private void stopService() {
        if (mService != null) {
            Log.d(LOG_TAG, "Stopping Service");
            reactContext.stopService(mService);
            mService = null;
        }
    }


    @Override
    public void onHostResume() {
//        Log.d(LOG_TAG, "Woke up");
        stopService();
        if (jobScheduler != null) {
	    mJobs = jobScheduler.getAllPendingJobs();
	    jobScheduler.cancelAll();
	}

    }


    private void scheduleJobs() {
        if (mJobs == null) return;

        for (JobInfo job : mJobs) {
            Log.d(LOG_TAG, "Sceduling job " + job.getId());
            jobScheduler.cancel(job.getId());
            int result = jobScheduler.schedule(job);
            if (result == JobScheduler.RESULT_SUCCESS)
                Log.d(LOG_TAG, "Job (" + job.getId() + ") scheduled successfully!");
        }
    }

    private void startForegroundJob() {
        if (mJobBundle != null) {
            Intent service = new Intent(reactContext, HeadlessService.class);
            service.putExtras(mJobBundle);
            mService = service;
            reactContext.startService(service);
        }
    }

    @Override
    public void onHostPause() {
//        Log.d(LOG_TAG, "Pausing");
        startForegroundJob();
        scheduleJobs();
    }

    @Override
    public void onHostDestroy() {
        getReactApplicationContext().removeLifecycleEventListener(this);
//        Log.d(LOG_TAG, "Destroyed");
    }

    private boolean isAppInForeground() {
        return (reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED);
    }
}
