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
import com.heaton.funnyvote.MainActivity;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.user.UserManager;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.notification.VoteNotificationManager;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = UserActivity.class.getSimpleName();

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean isAvatarShown = true;

    private CircleImageView imgUserIcon;
    private TextView txtUserName;
    private TextView txtSubTitle;
    private int maxScrollSize;
    private TabsAdapter tabsAdapter;
    private ViewPager viewPager;
    private Tracker tracker;
    private boolean isMainActivityNeedRestart = false;

    User user = null;
    UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            UserActivity.this.user = user;
            setUpUser(user);
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

        tabsAdapter = new TabsAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAdapter);
        tabLayout.setupWithViewPager(viewPager);
        UserManager.getInstance(getApplicationContext()).getUser(getUserCallback, false);
        tracker.setScreenName(AnalyzticsTag.SCREEN_BOX_CREATE);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_BOX_CREATE);
                } else if (position == 1) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_BOX_PARTICIPATE);
                } else if (position == 2) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_BOX_FAVORITE);
                }
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (VoteNotificationManager.ACTION_NOTIFICATION_USER_ACTIVITY_START.equals(getIntent().getAction())) {
            isMainActivityNeedRestart = true;
        } else {
            isMainActivityNeedRestart = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (VoteNotificationManager.ACTION_NOTIFICATION_USER_ACTIVITY_START.equals(action)) {
            isMainActivityNeedRestart = true;
        } else {
            isMainActivityNeedRestart = false;
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_BOX);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onBackPressed() {
        if (isMainActivityNeedRestart) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        super.onBackPressed();
    }

    private void setUpUser(User user) {
        txtUserName.setText(user.getUserName());
        txtSubTitle.setText(User.getUserTypeString(user.getType()) + ":" + user.getEmail());
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
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_USER)
                .setAction(AnalyzticsTag.ACTION_ENTER_USER_INFO)
                .setLabel(user.getUserCode()).build());
    }

    public static void start(Context c) {
        c.startActivity(new Intent(c, UserActivity.class));
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
            return 3;
        }

        @Override
        public Fragment getItem(int i) {
            Bundle argument = new Bundle();
            argument.putParcelable(MainPageTabFragment.KEY_LOGIN_USER, user);
            switch (i) {
                case 0:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE, user);
                case 1:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_PARTICIPATE, user);
                case 2:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_FAVORITE, user);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.personal_tab_create);
                case 1:
                    return getString(R.string.personal_tab_participate);
                case 2:
                    return getString(R.string.personal_tab_favorite);
            }
            return "";
        }
    }

}
