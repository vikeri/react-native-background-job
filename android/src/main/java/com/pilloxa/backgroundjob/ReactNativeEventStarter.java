package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.annotation.Nullable;

class ReactNativeEventStarter {
    private static final String LOG_TAG = ReactNativeEventStarter.class.getSimpleName();
    private final ReactInstanceManager reactInstanceManager;
    private final Context context;

    ReactNativeEventStarter(@NonNull Context context) {
        this.context = context;
        reactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
    }

    public void trigger(@NonNull Bundle jobBundle) {
        Log.d(LOG_TAG, "trigger() called with: jobBundle = [" + jobBundle + "]");
        if (isAppInForeground()) {
            emitJobEvent(jobBundle);
        } else {
            MyHeadlessJsTaskService.start(context, jobBundle);
        }
    }

    private void emitJobEvent(Bundle jobBundle) {
        Log.d(LOG_TAG, "emitJobEvent() called with: jobBundle = [" + jobBundle + "]");
        WritableMap map = Arguments.fromBundle(jobBundle);
        //noinspection ConstantConditions - nullability is checked in isAppInForeground
        getReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(jobBundle.getString("jobKey"), map);
    }

    public static class MyHeadlessJsTaskService extends HeadlessJsTaskService {
        private static final String LOG_TAG = MyHeadlessJsTaskService.class.getSimpleName();
        @Nullable
        @Override
        protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
            Log.d(LOG_TAG, "getTaskConfig() called with: intent = [" + intent + "]");
            return new HeadlessJsTaskConfig(intent.getStringExtra("jobKey"),
                    Arguments.fromBundle(intent.getExtras()));
        }

        public static void start(Context context, Bundle jobBundle) {
            Log.d(LOG_TAG, "start() called with: context = [" + context + "], jobBundle = [" + jobBundle + "]");
            Intent starter = new Intent(context, MyHeadlessJsTaskService.class);
            starter.putExtras(jobBundle);
            context.startService(starter);
        }
    }

    private boolean isAppInForeground() {
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
        return reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED;
    }

    @Nullable
    private ReactContext getReactContext() {
        return reactInstanceManager.getCurrentReactContext();
    }

}
