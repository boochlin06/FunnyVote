package com.heaton.funnyvote.ui.personal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.heaton.funnyvote.ui.main.MainPageContract;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.utils.Util;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by heaton on 2017/1/24.
 */

public class PersonalActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener, PersonalContract.UserPageView {
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

    private MainPageContract.Presenter presenter;
    private MainPageTabFragment createFragment, favoriteFragment;

    private Tracker tracker;
    private AlertDialog passwordDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        User targetUser = null;
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
        tabsAdapter = new TabsAdapter(getSupportFragmentManager(), null, null);
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
        //setUpUser(targetUser);
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

        presenter = new UserPresenter(Injection.provideVoteDataRepository(getApplicationContext())
                , Injection.provideUserRepository(getApplicationContext()), this
                , Injection.provideSchedulerProvider());
        presenter.setTargetUser(targetUser);
        presenter.subscribe();
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

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_PERSONAL);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.unsubscribe();
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

    @Override
    public void setUpUserView(User user) {
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

    @Override
    public void showShareDialog(VoteData data) {
        Util.sendShareIntent(this, data);
    }

    @Override
    public void showAuthorDetail(VoteData data) {
        Util.sendPersonalDetailIntent(this, data);
    }

    @Override
    public void showCreateVote() {
        this.startActivity(new Intent(this, CreateVoteActivity.class));
    }

    @Override
    public void showVoteDetail(VoteData data) {
        Util.startActivityToVoteDetail(this, data.getVoteCode());
    }


    @Override
    public void showIntroductionDialog() {
        //nothing
    }

    @Override
    public void showLoadingCircle() {
//        circleLoad.setVisibility(View.VISIBLE);
//        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
//        circleLoad.spin();
    }

    @Override
    public void hideLoadingCircle() {
//        circleLoad.stopSpinning();
//        circleLoad.setVisibility(View.GONE);
    }

    @Override
    public void setupPromotionAdmob(List<Promotion> promotionList, User user) {
        //none
    }

    @Override
    public void setUpTabsAdapter(User user) {
        setUpTabsAdapter(user, null);
    }

    @Override
    public void setUpTabsAdapter(User user, User targetUser) {
        tabsAdapter = new TabsAdapter(PersonalActivity.this.getSupportFragmentManager(), user, targetUser);
        int currentItem = viewPager.getCurrentItem();
        viewPager.setAdapter(tabsAdapter);
        viewPager.setCurrentItem(currentItem);
    }

    @Override
    public void showHintToast(int res, long arg) {
        Toast.makeText(this, getString(res, arg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPollPasswordDialog(final VoteData data, final String optionCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(this.getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(this.getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(this.getString(R.string.vote_detail_dialog_password_title));
        passwordDialog = builder.create();

        passwordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final EditText password = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.edtEnterPassword);
                Button ok = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Log.d(TAG, "showPollPasswordDialog PW:");
                        presenter.pollVote(data, optionCode, password.getText().toString());
//                        tracker.send(new HitBuilders.EventBuilder()
//                                .setCategory(tab)
//                                .setAction(AnalyzticsTag.ACTION_QUICK_POLL_VOTE)
//                                .setLabel(data.getVoteCode())
//                                .build());
                    }
                });
            }
        });
        passwordDialog.show();
    }

    @Override
    public void hidePollPasswordDialog() {
        if (passwordDialog != null && passwordDialog.isShowing()) {
            passwordDialog.dismiss();
        }
    }

    @Override
    public void shakePollPasswordDialog() {
        if (passwordDialog != null && passwordDialog.isShowing()) {
            final EditText password = (EditText) passwordDialog.findViewById(R.id.edtEnterPassword);
            password.selectAll();
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.edittext_shake);
            password.startAnimation(shake);
        }
    }

    @Override
    public boolean isPasswordDialogShowing() {
        return passwordDialog != null && passwordDialog.isShowing();
    }

    @Override
    public void setPresenter(MainPageContract.Presenter presenter) {
        this.presenter = presenter;
    }

    private class TabsAdapter extends FragmentStatePagerAdapter {
        private User loginUser, targetUser;

        public TabsAdapter(FragmentManager fm, User loginUser, User targetUser) {
            super(fm);
            this.loginUser = loginUser;
            this.targetUser = targetUser;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    if (createFragment == null) {
                        createFragment
                                = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_CREATE, loginUser, targetUser);
                        createFragment.setPresenter(presenter);
                    }
                    return createFragment;
                case 1:
                    if (favoriteFragment == null) {
                        favoriteFragment
                                = MainPageTabFragment.newInstance(MainPageTabFragment.TAB_FAVORITE, loginUser, targetUser);
                        favoriteFragment.setPresenter(presenter);
                    }
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
