package com.pilloxa.backgroundjob;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import com.facebook.react.modules.appstate.AppStateModule;
import android.widget.Toast;

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
//    Message message = Message.obtain(mJobHandler, 1, params);
//    message.setData(bundle);
//    mJobHandler.sendMessage(message);
//    return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mJobHandler.removeMessages(1);
        return false;
    }

    private Handler mJobHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Context reactContext = getApplicationContext();
            Intent service = new Intent(reactContext, HeadlessService.class);
            service.putExtras(msg.peekData());
            reactContext.startService(service);

            Log.d("com.bletesting", "MESSAGE");
            Toast.makeText(getApplicationContext(),
                    "Pilloxa Rules!", Toast.LENGTH_SHORT)
                    .show();
            jobFinished((JobParameters) msg.obj, false);
            return true;
        }

    });

}
