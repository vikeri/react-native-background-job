package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/** Simple {@link JobService} that will start our {@link AbstractHeadlessService}. */
public class BackgroundJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Bundle bundle = new Bundle(jobParameters.getExtras());
        Context reactContext = getApplicationContext();
        Intent service = new Intent(reactContext, AbstractHeadlessService.class);
        service.putExtras(bundle);
        reactContext.startService(service);
        return false; // No more work going on in this service
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; // Yes, we should retry this job again
    }
}
