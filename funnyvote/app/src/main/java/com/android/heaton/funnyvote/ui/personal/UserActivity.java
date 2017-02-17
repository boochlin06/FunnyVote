package com.android.heaton.funnyvote.ui.personal;

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

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.bumptech.glide.Glide;

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
