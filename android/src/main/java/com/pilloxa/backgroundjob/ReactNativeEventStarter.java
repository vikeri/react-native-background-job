package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

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
    private final ReactInstanceManager reactInstanceManager;
    private final Context context;

    ReactNativeEventStarter(@NonNull Context context) {
        this.context = context;
        reactInstanceManager = ((ReactApplication) context.getApplicationContext()).getReactNativeHost().getReactInstanceManager();
    }

    public void trigger(@NonNull Bundle jobBundle) {
        if (isAppInForeground()) {
            emitJobEvent(jobBundle);
        } else {
            MyHeadlessJsTaskService.start(context, jobBundle);
        }
    }

    private void emitJobEvent(Bundle jobBundle) {
        WritableMap map = Arguments.fromBundle(jobBundle);
        //noinspection ConstantConditions - nullability is checked in isAppInForeground
        getReactContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(jobBundle.getString("jobKey"), map);
    }

    public static class MyHeadlessJsTaskService extends HeadlessJsTaskService {
        @Nullable
        @Override
        protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
            return new HeadlessJsTaskConfig(intent.getStringExtra("jobKey"),
                    Arguments.fromBundle(intent.getExtras()));
        }

        public static void start(Context context, Bundle jobBundle) {
            Intent starter = new Intent(context, MyHeadlessJsTaskService.class);
            starter.putExtras(jobBundle);
            context.startActivity(starter);
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
