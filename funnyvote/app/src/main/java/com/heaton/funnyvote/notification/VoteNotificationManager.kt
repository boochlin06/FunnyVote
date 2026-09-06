package com.heaton.funnyvote.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.data.local.AppDatabase
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.ui.personal.UserActivity
import java.util.Calendar

class VoteNotificationManager(private val context: Context) {

    private val flags: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

    fun startNotificationAlarm() {
        val noCreateFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }
        val alarmUp = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, AlarmReceiver::class.java),
            noCreateFlags
        ) != null

        if (!alarmUp) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, NOTIFICATION_EVERY_DAY_HOUR)
            calendar.set(Calendar.MINUTE, NOTIFICATION_EVERY_DAY_MINUTE)
            calendar.set(Calendar.SECOND, 10)
            val intent = Intent(context, AlarmReceiver::class.java)
            val alarmIntent = PendingIntent.getBroadcast(context, 0, intent, flags)

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, alarmIntent
            )
        }
    }

    fun sendNotification() {
        val userDataRepository = Injection.provideUserRepository(context)
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                if ((Math.random() * 4).toInt() % 4 == 1) {
                    sendMainNotification()
                } else {
                    sendUserVoteChange(user.userCode)
                }
            }

            override fun onFailure() {
                sendMainNotification()
            }
        }, false)
    }

    fun sendUserVoteChange(authorCode: String) {
        val voteDataDao = AppDatabase.getInstance(context).voteDataDao()
        val count = voteDataDao.countUserVoteChanges(authorCode, System.currentTimeMillis())
        if (count > 0) {
            val resultIntent = Intent(context, UserActivity::class.java).apply {
                action = ACTION_NOTIFICATION_USER_ACTIVITY_START
            }
            val resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                flags
            )

            val mBuilder = NotificationCompat.Builder(context, "user_vote")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_content_updated))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)

            val mNotificationId = 1
            val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotifyMgr.notify(mNotificationId, mBuilder.build())
        } else {
            sendMainNotification()
        }
    }

    fun sendMainNotification() {
        val resultIntent = Intent(context, MainActivity::class.java)
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            0,
            resultIntent,
            flags
        )
        val mBuilder = NotificationCompat.Builder(context, "main")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content_nothing))
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)

        val mNotificationId = 1
        val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotifyMgr.notify(mNotificationId, mBuilder.build())
    }

    companion object {
        @JvmField
        var NOTIFICATION_EVERY_DAY_HOUR = 19
        var NOTIFICATION_EVERY_DAY_MINUTE = 30
        var ACTION_NOTIFICATION_USER_ACTIVITY_START = "com.heaton.notification.send"

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: VoteNotificationManager? = null

        @JvmStatic
        fun getInstance(context: Context): VoteNotificationManager {
            return INSTANCE ?: VoteNotificationManager(context).apply {
                INSTANCE = this
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
