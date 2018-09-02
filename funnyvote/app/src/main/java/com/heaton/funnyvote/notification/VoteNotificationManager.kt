package com.heaton.funnyvote.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteDataDao
import com.heaton.funnyvote.ui.personal.UserActivity
import java.util.*

/**
 * Created by heaton on 2017/4/29.
 */

class VoteNotificationManager(private val context: Context) {

    fun startNotificationAlarm() {
        val alarmUp = PendingIntent.getBroadcast(context, 0,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE) != null

        if (!alarmUp) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, NOTIFICATION_EVERY_DAY_HOUR)
            calendar.set(Calendar.MINUTE, NOTIFICATION_EVERY_DAY_MINUTE)
            calendar.set(Calendar.SECOND, 10)
            val alarmIntent: PendingIntent
            val intent = Intent(context, AlarmReceiver::class.java)
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY, alarmIntent)
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
        val voteDataDao = (context.applicationContext as FunnyVoteApplication)
                .daoSession.voteDataDao
        val count = voteDataDao.queryBuilder().whereOr(VoteDataDao.Properties.IsPolled.eq(true), VoteDataDao.Properties.AuthorCode.eq(authorCode))
                .where(VoteDataDao.Properties.StartTime.le(System.currentTimeMillis())).count()
        if (count > 0) {

            val resultIntent = Intent(context, UserActivity::class.java)
            resultIntent.action = ACTION_NOTIFICATION_USER_ACTIVITY_START
            val resultPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

            val mBuilder = NotificationCompat.Builder(context,"user_vote")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_content_updated))
                    .setContentIntent(resultPendingIntent)
                    .setAutoCancel(true)
            // Sets an ID for the notification
            val mNotificationId = 1
            // Gets an instance of the NotificationManager service
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
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder = NotificationCompat.Builder(context,"main")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_content_nothing))
                .setContentIntent(resultPendingIntent)
                .setAutoCancel(true)
        // Sets an ID for the notification
        val mNotificationId = 1
        // Gets an instance of the NotificationManager service
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
            return INSTANCE
                    ?: VoteNotificationManager(context)
                            .apply {
                                INSTANCE = this }
        }
        fun destroyInstance() {
            INSTANCE = null
        }
    }

}
