package com.heaton.funnyvote.ui.introduction;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.heaton.funnyvote.MainActivity;
import com.github.paolorotolo.appintro.AppIntro2;
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
                , R.drawable.intro_image_funnyvote, getColor(R.color.md_blue_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_quickpoll)
                , getString(R.string.intro_desc_quickpoll)
                , R.drawable.intro_image_quickvote, getColor(R.color.md_blue_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_no_comment)
                , getString(R.string.intro_desc_no_comment)
                , R.drawable.intro_image_no_comment, getColor(R.color.md_blue_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_bigdata)
                , getString(R.string.intro_desc_bigdata)
                , R.drawable.intro_image_quickvote, getColor(R.color.md_red_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_sharefin)
                , getString(R.string.intro_desc_sharefin)
                , R.drawable.intro_image_sharefin, getColor(R.color.md_red_300)));
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_come)
                , getString(R.string.intro_desc_come)
                , R.drawable.intro_image_come, getColor(R.color.md_red_300)));
        setFadeAnimation();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
