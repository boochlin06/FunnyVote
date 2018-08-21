package com.heaton.funnyvote.ui.introduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FirstTimePref;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.ui.mainactivity.MainActivity;

/**
 * Created by heaton on 2017/2/25.
 */

public class IntroductionActivity extends AppIntro2 {

    private Tracker tracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_funnyvote)
                , getString(R.string.intro_desc_funnyvote)
                , R.drawable.intro_image_funnyvote, ContextCompat.getColor(this, R.color.md_blue_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_quickpoll)
                , getString(R.string.intro_desc_quickpoll)
                , R.drawable.intro_image_quickvote, ContextCompat.getColor(this, R.color.md_blue_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_no_comment)
                , getString(R.string.intro_desc_no_comment)
                , R.drawable.intro_image_no_comment, ContextCompat.getColor(this, R.color.md_blue_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_bigdata)
                , getString(R.string.intro_desc_bigdata)
                , R.drawable.intro_image_big_data, ContextCompat.getColor(this, R.color.md_red_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_sharefin)
                , getString(R.string.intro_desc_sharefin)
                , R.drawable.intro_image_sharefin, ContextCompat.getColor(this, R.color.md_red_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_come)
                , getString(R.string.intro_desc_come)
                , R.drawable.intro_image_come, ContextCompat.getColor(this, R.color.md_red_300)));
        setFadeAnimation();
        tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_FUNNYVOTE);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences firstTime = FirstTimePref.getInstance(getApplicationContext()).getPreferences();
        if (firstTime.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
            firstTime.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, false).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences firstTime = FirstTimePref.getInstance(getApplicationContext()).getPreferences();
        if (firstTime.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
            firstTime.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, false).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        if (newFragment != null) {
            String title = newFragment.getArguments().getString(IntroductionFragment.ARG_TITLE);
            if (getString(R.string.intro_title_funnyvote).equals(title)) {
                tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_FUNNYVOTE);
            } else if (getString(R.string.intro_title_quickpoll).equals(title)) {
                tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_QUICKPOLL);
            } else if (getString(R.string.intro_title_no_comment).equals(title)) {
                tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_NO_COMMENT);
            } else if (getString(R.string.intro_title_bigdata).equals(title)) {
                tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_BIGDATA);
            } else if (getString(R.string.intro_title_sharefin).equals(title)) {
                tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_SHARE_FIN);
            } else if (getString(R.string.intro_title_come).equals(title)) {
                tracker.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_COME);
            }
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }
}
