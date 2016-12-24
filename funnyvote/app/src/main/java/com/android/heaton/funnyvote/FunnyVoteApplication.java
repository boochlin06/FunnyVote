package com.android.heaton.funnyvote;

import android.app.Application;

import com.android.heaton.funnyvote.database.DaoMaster;
import com.android.heaton.funnyvote.database.DaoSession;
import com.android.heaton.funnyvote.retrofit.Server;
import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

/**
 * Created by heaton on 2016/10/25.
 */

public class FunnyVoteApplication extends Application {

    public static final boolean ENCRYPTED = false;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "votes-db-encrypted" : "votes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession(IdentityScopeType.Session);
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
