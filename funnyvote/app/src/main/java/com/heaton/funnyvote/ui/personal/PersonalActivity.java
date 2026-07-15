package com.heaton.funnyvote.ui.personal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.user.UserManager;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by heaton on 2017/1/24.
 */

public class PersonalActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = PersonalActivity.class.getSimpleName();

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    public static final String EXTRA_PERSONAL_CODE = "personal_code";
    public static final String EXTRA_PERSONAL_CODE_TYPE = "personal_code_type";
    public static final String EXTRA_PERSONAL_NAME = "personal_name";
    public static final String EXTRA_PERSONAL_ICON = "personal_icon";

    private String personalCode;
    private String personalCodeType;
    private String personalName;
    private String personalIcon;
    private boolean isAvatarShown = true;

    private CircleImageView imgUserIcon;
    private TextView txtUserName;
    private TextView txtSubTitle;
    private int maxScrollSize;
    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;

    private User targetUser;
    private User loginUser;

    private Tracker tracker;
    UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            PersonalActivity.this.loginUser = user;
            tabsAdapter = new TabsAdapter(getSupportFragmentManager());
            viewPager.setAdapter(tabsAdapter);
        }

        @Override
        public void onFailure() {
            tabsAdapter = new TabsAdapter(getSupportFragmentManager());
            viewPager.setAdapter(tabsAdapter);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        if (getIntent() != null) {
            personalCode = getIntent().getStringExtra(EXTRA_PERSONAL_CODE);
            personalCodeType = getIntent().getStringExtra(EXTRA_PERSONAL_CODE_TYPE);
            personalName = getIntent().getStringExtra(EXTRA_PERSONAL_NAME);
            personalIcon = getIntent().getStringExtra(EXTRA_PERSONAL_ICON);
            targetUser = new User();
            targetUser.setUserCode(personalCode);
            targetUser.personalTokenType = personalCodeType;
            targetUser.setUserName(personalName);
            targetUser.setUserIcon(personalIcon);
        } else {
            finish();
        }
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_PERSONAL)
                .setAction(AnalyzticsTag.ACTION_ENTER_PERSONAL_INFO)
                .setLabel(personalCode).build());
        txtSubTitle = (TextView) findViewById(R.id.txtSubTitle);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayoutPersonal);
        viewPager = (ViewPager) findViewById(R.id.vpMain);
        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.appBarMain);
        imgUserIcon = (CircleImageView) findViewById(R.id.imgUserIcon);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarSub);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        appbarLayout.addOnOffsetChangedListener(this);
        maxScrollSize = appbarLayout.getTotalScrollRange();

        tabLayout.setupWithViewPager(viewPager);
        setUpUser(targetUser);
        UserManager.getInstance(getApplicationContext()).getUser(getUserCallback, false);
        tracker.setScreenName(AnalyzticsTag.SCREEN_PERSONAL_CREATE);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_PERSONAL_CREATE);
                } else if (position == 1) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_PERSONAL_FAVORITE);
                }
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setUpUser(User user) {
        txtUserName.setText(user.getUserName());
        txtSubTitle.setText(user.personalTokenType);
        if (user.getUserIcon() == null || user.getUserIcon().isEmpty()) {
            imgUserIcon.setImageResource(R.drawable.user_avatar);
        } else {
            Glide.with(this)
                    .load(user.getUserIcon())
                    .override((int) getResources().getDimension(R.dimen.personal_image_width)
                            , (int) getResources().getDimension(R.dimen.personal_image_high))
                    .dontAnimate()
                    .fitCenter()
                    .crossFade()
                    .into(imgUserIcon);
        }
    }

    public static void start(Context c) {
        c.startActivity(new Intent(c, PersonalActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_PERSONAL);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (maxScrollSize == 0)
            maxScrollSize = appBarLayout.getTotalScrollRange();

        int percentage = (Math.abs(i)) * 100 / maxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && isAvatarShown) {
            isAvatarShown = false;
            imgUserIcon.animate().scaleY(0).scaleX(0).setDuration(200).start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !isAvatarShown) {
            isAvatarShown = true;

            imgUserIcon.animate()
                    .scaleY(1).scaleX(1)
                    .start();
        }
    }

    private class TabsAdapter extends FragmentStatePagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE, loginUser, targetUser);
                case 1:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_FAVORITE, loginUser, targetUser);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.personal_tab_create);
                case 1:
                    return getString(R.string.personal_tab_favorite);
            }
            return "";
        }
    }

}
