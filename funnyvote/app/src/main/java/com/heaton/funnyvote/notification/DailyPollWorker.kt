package com.heaton.funnyvote.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R

class DailyPollWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val CHANNEL_ID = "funnyvote_daily_poll"
        const val NOTIFICATION_ID = 1001

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "每日熱門推薦"
                val descriptionText = "每日為您推薦最新、最火熱的社群投票活動"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override suspend fun doWork(): Result {
        return try {
            createNotificationChannel(appContext)

            val intent = Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                appContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("【FunnyVote】今日熱門推薦投票上線了！")
                .setContentText("大家都在熱烈討論，快打開 App 投下關鍵的一票！")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(appContext)
            try {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            } catch (_: SecurityException) {
                // Permission not granted on Android 13+, safely ignore
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
