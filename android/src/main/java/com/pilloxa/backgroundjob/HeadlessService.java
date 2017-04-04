package com.pilloxa.backgroundjob;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by viktor on 2016-12-13.
 */

import com.facebook.react.bridge.Arguments;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;

public class HeadlessService  extends HeadlessJsTaskService {
    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent.getExtras();
        String jobKey = extras.getString("jobKey");
        int timeout = extras.getInt("timeout");

        if( isAppInForeground() ){
          stopSelf();
          return null;
        }
        return new HeadlessJsTaskConfig(
              jobKey,
              Arguments.fromBundle(extras),
              timeout
            );
    }

    private boolean isAppInForeground() {
        final ReactInstanceManager reactInstanceManager =
                ((ReactApplication) getApplication())
                        .getReactNativeHost()
                        .getReactInstanceManager();
        ReactContext reactContext =
                reactInstanceManager.getCurrentReactContext();

        return (reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED);
    }
}
