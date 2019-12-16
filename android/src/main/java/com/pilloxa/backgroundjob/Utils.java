package com.pilloxa.backgroundjob;

import androidx.annotation.NonNull;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.common.LifecycleState;

public class Utils {
  private Utils() {
    //no instance
  }

  /** Check whether on not the React Native application is in foreground. */
  public static boolean isReactNativeAppInForeground(@NonNull ReactNativeHost reactNativeHost) {
    if (!reactNativeHost.hasInstance()) {
      // If the app was force-stopped the instace will be destroyed. The instance can't be created from a background thread.
      return false;
    }
    ReactContext reactContext = reactNativeHost.getReactInstanceManager().getCurrentReactContext();
    return reactContext != null && reactContext.getLifecycleState() == LifecycleState.RESUMED;
  }
}
