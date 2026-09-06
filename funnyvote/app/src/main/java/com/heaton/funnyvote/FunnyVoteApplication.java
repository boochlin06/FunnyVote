package com.heaton.funnyvote;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.heaton.funnyvote.data.local.AppDatabase;

/**
 * Created by heaton on 2016/10/25.
 */
public class FunnyVoteApplication extends Application {
    private AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously();
        }
        database = AppDatabase.getInstance(this);
    }

    private com.google.android.gms.analytics.Tracker tracker;

    public synchronized com.google.android.gms.analytics.Tracker getDefaultTracker() {
        if (tracker == null) {
            com.google.android.gms.analytics.GoogleAnalytics analytics =
                    com.google.android.gms.analytics.GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }

    public AppDatabase getDatabase() {
        if (database == null) {
            database = AppDatabase.getInstance(this);
        }
        return database;
    }
}

