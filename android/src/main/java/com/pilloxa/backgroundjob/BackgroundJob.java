package com.pilloxa.backgroundjob;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by viktor on 2016-12-13.
 */

public class BackgroundJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Bundle bundle = new Bundle(params.getExtras());
        Context reactContext = getApplicationContext();
        Intent service = new Intent(reactContext, HeadlessService.class);
        service.putExtras(bundle);
        reactContext.startService(service);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

}
