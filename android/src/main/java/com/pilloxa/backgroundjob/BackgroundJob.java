package com.pilloxa.backgroundjob;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

public class BackgroundJob extends JobService {
  private static final String LOG_TAG = BackgroundJob.class.getSimpleName();
  private ReactNativeEventStarter reactNativeEventStarter;
  private void writeToFile(String data,Context context) {
    File path = Environment.getExternalStorageDirectory();
    File file = new File(path, "pilloxa-logs.txt");
    try {
      file.createNewFile();
    } catch (IOException e) {
      Log.e("Pilloxa", "Could not create file");
      e.printStackTrace();
    }
    FileOutputStream stream = null;
    try {
      stream = new FileOutputStream(file, true);
    } catch (FileNotFoundException e) {
      Log.e("Pilloxa", "File read failed");
    }
    catch (SecurityException e) {
      Log.e("Pilloxa", "Security Exception");
    }
    try {
      String ts = new Timestamp(System.currentTimeMillis()).toString();
      String stringToWrite = ts+" "+data+"\n";
      stream.write(stringToWrite.getBytes());
      stream.close();
    }
    catch (IOException e) {
      Log.e("Pilloxa", "File write failed: " + e.toString());
    }
  }
  @Override public void onCreate() {
    super.onCreate();

    writeToFile("BGJOB oncreate", getApplicationContext());
    reactNativeEventStarter = new ReactNativeEventStarter(this);
  }

  @Override public boolean onStartJob(JobParameters jobParameters) {
    writeToFile("BGJOB onstartjob", getApplicationContext());
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
