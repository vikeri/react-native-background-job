
package com.pilloxa.backgroundjob;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;
import com.facebook.react.bridge.*;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.cxxbridge.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundJobModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private String LOG_TAG = "backgroundjob";

    private final ReactApplicationContext reactContext;

    private List<JobInfo> mJobs;

    private JobScheduler jobScheduler;

    private boolean mInitialized = false;

    @Override
    public void initialize() {
        Log.d(LOG_TAG, "Initializing");
        super.initialize();
        getReactApplicationContext().addLifecycleEventListener(this);
    }

    public BackgroundJobModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void schedule(String jobKey, int timeout, int period, boolean persist) {
        int taskId = jobKey.hashCode();
        Toast.makeText(getReactApplicationContext(), "SIKE!", Toast.LENGTH_SHORT).show();

        Log.v(LOG_TAG, "Scheduling: " + jobKey + " timeout: " + Integer.toString(timeout) + " period " + Integer.toString(period));

        int persistInt = persist ? 1 : 0;

        ComponentName componentName = new ComponentName(getReactApplicationContext(), BackgroundJob.class.getName());
        PersistableBundle jobExtras = new PersistableBundle();
        jobExtras.putString("jobKey", jobKey);
        jobExtras.putInt("timeout", timeout);
        jobExtras.putInt("persist", persistInt);
        jobExtras.putInt("period", period);
        JobInfo jobInfo = new JobInfo.Builder(taskId, componentName)
                .setPeriodic(period)
                .setExtras(jobExtras)
                .setPersisted(persist)
                .build();

        for (JobInfo iJobInfo : mJobs) {
            if (iJobInfo.getId() == taskId) {
                mJobs.remove(iJobInfo);
            }
        }
        mJobs.add(jobInfo);
    }


    @ReactMethod
    public void cancel(String jobKey) {
        int taskId = jobKey.hashCode();
        Log.d(LOG_TAG, "Cancelling job: " + jobKey + " (" + taskId + ")");
        jobScheduler.cancel(taskId);
        mJobs = jobScheduler.getAllPendingJobs();
    }

    @ReactMethod
    public void cancelAll() {
        Log.d(LOG_TAG, "Cancelling all jobs");
        jobScheduler.cancelAll();
        mJobs = jobScheduler.getAllPendingJobs();
    }

    private WritableArray _getAll() {
        Log.d(LOG_TAG, "GETTING ALL");
        WritableArray jobs = Arguments.createArray();
        for (JobInfo job : mJobs) {
            Log.d(LOG_TAG, "Fetching job " + job.getId());
            Bundle extras = new Bundle(job.getExtras());
            WritableMap jobMap = Arguments.fromBundle(extras);
            boolean persisted = extras.getInt("persist") == 1;
            jobMap.putBoolean("persist", persisted);
            jobs.pushMap(jobMap);
        }

        return jobs;
    }

    @ReactMethod
    public void getAll(Callback callback) {
        Log.d(LOG_TAG, "RN Getting aall");
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
        mJobs = jobScheduler.getAllPendingJobs();
        mInitialized = true;
        HashMap<String, Object> constants = new HashMap<>();
        constants.put("jobs", _getAll());
        return constants;
    }

    @Override
    public void onHostResume() {
        Log.d(LOG_TAG, "Woke up");
        mJobs = jobScheduler.getAllPendingJobs();
        jobScheduler.cancelAll();

    }

    @Override
    public void onHostPause() {
        Log.d(LOG_TAG, "Pausing");
        for (JobInfo job : mJobs) {
            Log.d(LOG_TAG, "Sceduling job " + job.getId());
            jobScheduler.cancel(job.getId());
            int result = jobScheduler.schedule(job);
            if (result == JobScheduler.RESULT_SUCCESS)
                Log.d(LOG_TAG, "Job (" + job.getId() + ") scheduled successfully!");
        }

    }

    @Override
    public void onHostDestroy() {
        Log.d(LOG_TAG, "Destroyed!");
    }
}