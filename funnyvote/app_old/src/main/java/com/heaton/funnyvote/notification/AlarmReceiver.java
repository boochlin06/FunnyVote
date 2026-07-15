package com.heaton.funnyvote.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by heaton on 2017/4/29.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            VoteNotificationManager.getInstance(context).startNotificationAlarm();
        } else {
            VoteNotificationManager.getInstance(context).sendNotification();
        }
    }
}
