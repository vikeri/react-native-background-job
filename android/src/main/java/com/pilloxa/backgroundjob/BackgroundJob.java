package com.pilloxa.backgroundjob;

import android.os.Bundle;
import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class BackgroundJob extends JobService {
  private static final String LOG_TAG = BackgroundJob.class.getSimpleName();
  private ReactNativeEventStarter reactNativeEventStarter;

  @Override public void onCreate() {
    super.onCreate();
    reactNativeEventStarter = new ReactNativeEventStarter(this);
  }

  @Override public boolean onStartJob(JobParameters jobParameters) {
    Log.d(LOG_TAG, "onStartJob() called with: jobParameters = [" + jobParameters + "]");
    Bundle jobBundle = jobParameters.getExtras();
    if (jobBundle != null) {
      reactNativeEventStarter.trigger(jobBundle);
    } else {
      throw new RuntimeException("No job parameters provided for job:" + jobParameters.getTag());
    }
    return false; // No more work going on in this service
  }

  @Override public boolean onStopJob(JobParameters params) {
    Log.d(LOG_TAG, "onStopJob() called with: params = [" + params + "]");
    return true; // Yes, we should retry this job again
  }
}
