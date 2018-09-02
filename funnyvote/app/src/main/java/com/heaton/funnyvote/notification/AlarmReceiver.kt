package com.heaton.funnyvote.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by heaton on 2017/4/29.
 */

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            VoteNotificationManager.getInstance(context).startNotificationAlarm()
        } else {
            VoteNotificationManager.getInstance(context).sendNotification()
        }
    }
}
