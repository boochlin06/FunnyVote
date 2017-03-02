package com.heaton.funnyvote.ui.introduction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro2;
import com.heaton.funnyvote.FirstTimePref;
import com.heaton.funnyvote.MainActivity;
import com.heaton.funnyvote.R;

/**
 * Created by heaton on 2017/2/25.
 */

public class IntroductionActivity extends AppIntro2 {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                , R.drawable.intro_image_quickvote, ContextCompat.getColor(this, R.color.md_red_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_sharefin)
                , getString(R.string.intro_desc_sharefin)
                , R.drawable.intro_image_sharefin, ContextCompat.getColor(this, R.color.md_red_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_come)
                , getString(R.string.intro_desc_come)
                , R.drawable.intro_image_come, ContextCompat.getColor(this, R.color.md_red_300)));
        setFadeAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences firstTime = FirstTimePref.getInstance(this).getPreferences();
        if (firstTime.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
            firstTime.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, false).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences firstTime = FirstTimePref.getInstance(this).getPreferences();
        if (firstTime.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
            firstTime.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, false).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
        finish();
    }
}
