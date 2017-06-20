package com.pilloxa.backgroundjob;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import java.util.Set;

public class ExactJob extends IntentService {
  public ExactJob() {
    super(ExactJob.class.getSimpleName());
  }

  @Override protected void onHandleIntent(@Nullable Intent intent) {
    if (intent != null) {
      Set<String> scheduledJobs = ExactJobDispatcher.getScheduledExactJobs(this);
      Bundle extras = intent.getExtras();
      String jobKey = extras.getString("jobKey");
      // Check if the job has been canceled meanwhile
      if (scheduledJobs.contains(jobKey)) {
        new ReactNativeEventStarter(this).trigger(extras);
        long period = extras.getLong("period", 2000);
        boolean override = extras.getBoolean("override", false);
        // Re-schedule the job with the same parameters
        ExactJobDispatcher.schedule(this, jobKey, period, override, extras);
      }
    }
  }
}
