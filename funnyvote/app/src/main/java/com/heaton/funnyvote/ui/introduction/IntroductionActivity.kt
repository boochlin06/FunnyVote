package com.heaton.funnyvote.ui.introduction

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat

import com.github.paolorotolo.appintro.AppIntro2
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FirstTimePref
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.MainActivity
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection

/**
 * Created by heaton on 2017/2/25.
 */

class IntroductionActivity : AppIntro2() {

    private var tracker: Tracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as FunnyVoteApplication
        tracker = application.defaultTracker
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_funnyvote), getString(R.string.intro_desc_funnyvote), R.drawable.intro_image_funnyvote, ContextCompat.getColor(this, R.color.md_blue_300)))
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_quickpoll), getString(R.string.intro_desc_quickpoll), R.drawable.intro_image_quickvote, ContextCompat.getColor(this, R.color.md_blue_300)))
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_no_comment), getString(R.string.intro_desc_no_comment), R.drawable.intro_image_no_comment, ContextCompat.getColor(this, R.color.md_blue_300)))
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_bigdata), getString(R.string.intro_desc_bigdata), R.drawable.intro_image_big_data, ContextCompat.getColor(this, R.color.md_red_300)))
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_sharefin), getString(R.string.intro_desc_sharefin), R.drawable.intro_image_sharefin, ContextCompat.getColor(this, R.color.md_red_300)))
        addSlide(IntroductionFragment.newInstance(getString(R.string.intro_title_come), getString(R.string.intro_desc_come), R.drawable.intro_image_come, ContextCompat.getColor(this, R.color.md_red_300)))
        setFadeAnimation()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_FUNNYVOTE)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onResume() {
        super.onResume()
        tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO)
        tracker!!.send(HitBuilders.ScreenViewBuilder().build())
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        val firstTime = Injection.provideFirstTimePref(this)
        if (firstTime.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
            firstTime.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, false).apply()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val firstTime = Injection.provideFirstTimePref(this)
        if (firstTime.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
            firstTime.edit().putBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, false).apply()
            startActivity(Intent(applicationContext, MainActivity::class.java))
        }
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        if (newFragment != null) {
            val title = newFragment.arguments!!.getString(IntroductionFragment.ARG_TITLE)
            when (title) {
                getString(R.string.intro_title_funnyvote) -> tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_FUNNYVOTE)
                getString(R.string.intro_title_quickpoll) -> tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_QUICKPOLL)
                getString(R.string.intro_title_no_comment) -> tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_NO_COMMENT)
                getString(R.string.intro_title_bigdata) -> tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_BIGDATA)
                getString(R.string.intro_title_sharefin) -> tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_SHARE_FIN)
                getString(R.string.intro_title_come) -> tracker!!.setScreenName(AnalyzticsTag.SCREEN_APP_INTRO_COME)
            }
            tracker!!.send(HitBuilders.ScreenViewBuilder().build())
        }
    }
}
