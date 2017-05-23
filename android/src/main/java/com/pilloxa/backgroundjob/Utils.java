package com.pilloxa.backgroundjob;

import android.content.Context;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;

public class Utils {
    private Utils() {

    }

    static boolean isAppInForeground(Context context) {
        final ReactInstanceManager reactInstanceManager =
                ((ReactApplication) context.getApplicationContext())
                        .getReactNativeHost()
                        .getReactInstanceManager();
        ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

        return (reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED);
    }
}
