package com.heaton.funnyvote.notification;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.heaton.funnyvote.MainActivity;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.data.local.AppDatabase;
import com.heaton.funnyvote.data.local.dao.VoteDataDao;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.ui.personal.UserActivity;
import com.heaton.funnyvote.utils.AppExecutors;

import java.util.Calendar;

/**
 * Created by heaton on 2017/4/29.
 */
public class VoteNotificationManager {
    public static int NOTIFICATION_EVERY_DAY_HOUR = 19;
    public static int NOTIFICATION_EVERY_DAY_MINUTE = 30;
    public static String ACTION_NOTIFICATION_USER_ACTIVITY_START = "com.heaton.notification.send";
    private static final String CHANNEL_ID = "funnyvote_channel";
    private static final String CHANNEL_NAME = "FunnyVote Notifications";

    private Context context;
    private static VoteNotificationManager INSTANCE = null;

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
        this.context = context.getApplicationContext();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public void startNotificationAlarm() {
        int flags = PendingIntent.FLAG_NO_CREATE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class),
                flags) != null);

        if (!alarmUp) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, NOTIFICATION_EVERY_DAY_HOUR);
            calendar.set(Calendar.MINUTE, NOTIFICATION_EVERY_DAY_MINUTE);
            calendar.set(Calendar.SECOND, 10);

            int broadcastFlags = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                broadcastFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, broadcastFlags);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);
            }
        }
    }

    public void sendNotification() {
        UserDataRepository userDataRepository = Injection.provideUserRepository(context);
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                if (((int) (Math.random() * 4)) % 4 == 1) {
                    sendMainNotification();
                } else {
                    sendUserVoteChange(user != null ? user.getUserCode() : "");
                }
            }

            @Override
            public void onFailure() {
                sendMainNotification();
            }
        }, false);
    }

    public void sendUserVoteChange(final String authorCode) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                VoteDataDao voteDataDao = AppDatabase.getInstance(context).voteDataDao();
                long count = voteDataDao.countUserVoteChanges(authorCode, System.currentTimeMillis());
                if (count > 0) {
                    Intent resultIntent = new Intent(context, UserActivity.class);
                    resultIntent.setAction(ACTION_NOTIFICATION_USER_ACTIVITY_START);
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags |= PendingIntent.FLAG_IMMUTABLE;
                    }
                    PendingIntent resultPendingIntent = PendingIntent.getActivity(
                            context,
                            0,
                            resultIntent,
                            flags
                    );

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setContentTitle(context.getString(R.string.notification_title))
                                    .setContentText(context.getString(R.string.notification_content_updated))
                                    .setContentIntent(resultPendingIntent)
                                    .setAutoCancel(true);

                    int mNotificationId = 1;
                    NotificationManager mNotifyMgr =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (mNotifyMgr != null) {
                        mNotifyMgr.notify(mNotificationId, mBuilder.build());
                    }
                } else {
                    sendMainNotification();
                }
            }
        });
    }

    public void sendMainNotification() {
        Intent resultIntent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                context,
                0,
                resultIntent,
                flags
        );
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(context.getString(R.string.notification_content_nothing))
                        .setContentIntent(resultPendingIntent)
                        .setAutoCancel(true);

        int mNotificationId = 1;
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotifyMgr != null) {
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}
