package com.pilloxa.backgroundjob;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class BackgroundHeadlessService extends AbstractHeadlessService {
    public static void start(Context context, Bundle jobBundle) {
        Intent starter = new Intent(context, BackgroundHeadlessService.class);
        starter.putExtras(jobBundle);
        context.startService(starter);
    }
}
