package com.heaton.funnyvote.notification;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.user.LocalUserDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteDataDao;
import com.heaton.funnyvote.ui.mainactivity.MainActivity;
import com.heaton.funnyvote.ui.personal.UserActivity;

import java.util.Calendar;

import javax.inject.Inject;

/**
 * Created by heaton on 2017/4/29.
 */

public class VoteNotificationManager {
    public static int NOTIFICATION_EVERY_DAY_HOUR = 19;
    public static int NOTIFICATION_EVERY_DAY_MINUTE = 30;
    public static String ACTION_NOTIFICATION_USER_ACTIVITY_START = "com.heaton.notification.send";
    private Context context;
    private static VoteNotificationManager INSTANCE = null;
    @Inject
    public UserDataRepository userDataRepository;

    public static VoteNotificationManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (VoteNotificationManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VoteNotificationManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public VoteNotificationManager(Context context) {
        this.context = context;
    }

    public void startNotificationAlarm() {
        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, NOTIFICATION_EVERY_DAY_HOUR);
            calendar.set(Calendar.MINUTE, NOTIFICATION_EVERY_DAY_MINUTE);
            calendar.set(Calendar.SECOND, 10);
            PendingIntent alarmIntent;
            Intent intent = new Intent(context, AlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public void sendNotification() {
        LocalUserDataSource userDataRepository = LocalUserDataSource.getInstance(context);
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                if (((int) (Math.random() * 4)) % 4 == 1) {
                    sendMainNotification();
                } else {
                    sendUserVoteChange(user.getUserCode());
                }
            }

            @Override
            public void onFailure() {
                sendMainNotification();
            }
        }, false);
    }

    public void sendUserVoteChange(String authorCode) {
        VoteDataDao voteDataDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getVoteDataDao();
        long count = voteDataDao.queryBuilder().whereOr(VoteDataDao.Properties.IsPolled.eq(true)
                , VoteDataDao.Properties.AuthorCode.eq(authorCode))
                .where(VoteDataDao.Properties.StartTime.le(System.currentTimeMillis())).count();
        if (count > 0) {

            Intent resultIntent = new Intent(context, UserActivity.class);
            resultIntent.setAction(ACTION_NOTIFICATION_USER_ACTIVITY_START);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(context.getString(R.string.notification_title))
                            .setContentText(context.getString(R.string.notification_content_updated))
                            .setContentIntent(resultPendingIntent)
                            .setAutoCancel(true);
            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        } else {
            sendMainNotification();
        }
    }

    public void sendMainNotification() {
        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_content_nothing))
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true);
        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

}
