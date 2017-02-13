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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by heaton on 2017/1/24.
 */

public class PersonalActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

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
            Toast.makeText(getApplicationContext()
                    , R.string.toast_network_connect_error_get_list, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);
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
    }

    private void setUpUser(User user) {
        txtUserName.setText(user.getUserName());
        Log.d("test","setUpUser:"+user.personalTokenType);
        txtSubTitle.setText(user.personalTokenType);
        if (user.getUserIcon() == null || user.getUserIcon().isEmpty()) {
            imgUserIcon.setImageResource(R.drawable.user_avatar);
        } else {
            Glide.with(this)
                    .load(user.getUserIcon())
                    .override(160, 160)
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
            Bundle argument = new Bundle();
            argument.putParcelable(MainPageTabFragment.KEY_LOGIN_USER, loginUser);
            argument.putParcelable(MainPageTabFragment.KEY_TARGET_USER, targetUser);
            switch (i) {
                case 0:
                    MainPageTabFragment createFragment = MainPageTabFragment.newInstance();
                    argument.putString(MainPageTabFragment.KEY_TAB, MainPageTabFragment.TAB_CREATE);
                    createFragment.setArguments(argument);
                    return createFragment;
                case 1:
                    MainPageTabFragment favoriteFragment = MainPageTabFragment.newInstance();
                    argument.putString(MainPageTabFragment.KEY_TAB, MainPageTabFragment.TAB_FAVORITE);
                    favoriteFragment.setArguments(argument);
                    return favoriteFragment;
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
