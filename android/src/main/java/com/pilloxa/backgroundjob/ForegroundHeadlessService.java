package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ForegroundHeadlessService extends AbstractHeadlessService {
    public static void start(Context context, Bundle jobBundle) {
        Intent starter = new Intent(context, ForegroundHeadlessService.class);
        starter.putExtras(jobBundle);
        context.startService(starter);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ForegroundHeadlessService.class));
    }
}
