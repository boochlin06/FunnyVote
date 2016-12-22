package com.android.heaton.funnyvote.ui.personal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 20;
    private boolean isAvatarShown = true;

    private CircleImageView imgUserIcon;
    TextView txtUserName;
    TextView txtSubTitle;
    private int maxScrollSize;

    User user = null;
    UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            PersonalActivity.this.user = user;
            setUpUser(user);
        }

        @Override
        public void onFailure() {

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        txtSubTitle = (TextView) findViewById(R.id.txtSubTitle);
        txtUserName = (TextView) findViewById(R.id.txtUserName);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayoutPersonal);
        ViewPager viewPager = (ViewPager) findViewById(R.id.vpMain);
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

        UserManager.getInstance(getApplicationContext()).getUser(getUserCallback);
        viewPager.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

    }
    private void setUpUser(User user) {
        txtUserName.setText(user.getUserName());
        txtSubTitle.setText(User.getUserTypeString(user.getType()) + ":" + user.getEmail());
        if (user.getUserIcon() == null || user.getUserIcon().isEmpty()) {
            imgUserIcon.setImageResource(R.drawable.user_avatar);
        } else {
            Glide.with(this)
                    .load(user.getUserIcon())
                    .override(120, 120)
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

    private class TabsAdapter extends FragmentPagerAdapter {
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
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE);
                case 1:
                    return MainPageTabFragment.newInstance(MainPageTabFragment.TAB_PARTICIPATE);
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
            }
            return "";
        }
    }

}
