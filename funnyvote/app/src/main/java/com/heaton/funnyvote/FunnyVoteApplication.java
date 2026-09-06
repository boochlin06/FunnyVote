package com.heaton.funnyvote;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.FirebaseApp;
import com.heaton.funnyvote.data.VoteData.RemoteVoteDataSource;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.di.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

public class FunnyVoteApplication extends DaggerApplication {

    private Tracker tracker;

    @Inject
    public VoteDataRepository voteDataRepository;
    @Inject
    public UserDataRepository userDataRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }
}
