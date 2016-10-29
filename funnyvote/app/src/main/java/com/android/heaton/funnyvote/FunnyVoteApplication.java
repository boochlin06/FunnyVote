package com.android.heaton.funnyvote;

import android.app.Application;

import com.android.heaton.funnyvote.database.DaoMaster;
import com.android.heaton.funnyvote.database.DaoSession;
import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

/**
 * Created by heaton on 2016/10/25.
 */

public class FunnyVoteApplication extends Application {
    public static final String SHARED_PREF_USER = "user";
    public static final String KEY_NAME = "name";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_TYPE = "account_type";
    public static final String PROFILE_PICTURE_FILE = "profile_pic.png";

    public static final boolean ENCRYPTED = false;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "votes-db-encrypted" : "votes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession(IdentityScopeType.None);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
