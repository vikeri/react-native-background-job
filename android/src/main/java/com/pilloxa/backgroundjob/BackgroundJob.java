package com.pilloxa.backgroundjob;

import android.os.Bundle;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/** Simple {@link JobService} that will start our {@link AbstractHeadlessService}. */
public class BackgroundJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Bundle bundle = new Bundle(jobParameters.getExtras());
        BackgroundHeadlessService.start(this, bundle);
        return false; // No more work going on in this service
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; // Yes, we should retry this job again
    }
}
