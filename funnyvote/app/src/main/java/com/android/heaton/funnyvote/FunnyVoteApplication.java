package com.android.heaton.funnyvote;

import android.app.Application;

import com.android.heaton.funnyvote.database.DaoMaster;
import com.android.heaton.funnyvote.database.DaoSession;
import com.android.heaton.funnyvote.retrofit.Server;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.stetho.Stetho;

import com.google.android.gms.ads.MobileAds;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

/**
 * Created by heaton on 2016/10/25.
 */

public class FunnyVoteApplication extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "x3l5s5Xo0Ns7B0woDW9Jg9fuD";
    private static final String TWITTER_SECRET = "F7MccZvZXRr9LFexSd2yKpqNiN6JckS697svkAHxWP9UbEqDRd";

    public static final boolean ENCRYPTED = false;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        Stetho.initializeWithDefaults(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "votes-db-encrypted" : "votes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession(IdentityScopeType.Session);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
