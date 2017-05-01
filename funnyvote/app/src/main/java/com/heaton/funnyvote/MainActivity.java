package com.heaton.funnyvote;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.user.UserManager;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.eventbus.EventBusManager;
import com.heaton.funnyvote.notification.VoteNotificationManager;
import com.heaton.funnyvote.ui.about.AboutFragment;
import com.heaton.funnyvote.ui.account.AccountFragment;
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.heaton.funnyvote.ui.main.MainPageFragment;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.ui.personal.UserActivity;
import com.heaton.funnyvote.ui.search.SearchFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    public static String TAG = MainPageTabFragment.class.getSimpleName();
    private static final int ANIM_DURATION_TOOLBAR = 300;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private int currentPage;
    boolean doubleBackToExitPressedOnce = false;
    private SearchView searchView;
    private String searchKeyword;
    public static boolean ENABLE_ADMOB = true;
    private AdView adView;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content
                , new MainPageFragment()).commit();
        toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        adView = (AdView) findViewById(R.id.adView);
        toolbar.setTitle(getString(R.string.drawer_home));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,
                R.string.drawer_close);
        drawerToggle.syncState();

        ENABLE_ADMOB = getResources().getBoolean(R.bool.enable_main_admob);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage != navigationView.getMenu().findItem(R.id.navigation_item_account).getItemId()) {
                    switchFragment(navigationView.getMenu().findItem(R.id.navigation_item_account));
                }
                drawerLayout.closeDrawers();
            }
        });
        currentPage = R.id.navigation_item_main;

        setupDrawerContent(navigationView);
        setupDrawerHeader();
        setUpAdmob();

        VoteNotificationManager.getInstance(getApplicationContext()).startNotificationAlarm();
    }

    private void setUpAdmob() {
        if (ENABLE_ADMOB) {
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        drawerLayout.addDrawerListener(drawerToggle);
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                setupDrawerHeader();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        if (currentPage != menuItem.getItemId()) {
                            switchFragment(menuItem);
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUIChange(EventBusManager.UIControlEvent event) {
        if (event.message.equals(EventBusManager.UIControlEvent.INTRO_TO_ACCOUNT)) {
            drawerLayout.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    private void setupDrawerHeader() {
        UserManager.getInstance(getApplicationContext()).getUser(new UserManager.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                View header = navigationView.getHeaderView(0);
                CircleImageView icon = (CircleImageView) header.findViewById(R.id.imgUserIcon);
                TextView name = (TextView) header.findViewById(R.id.txtUserName);
                name.setText(user.getUserName());
                Glide.with(MainActivity.this).load(user.getUserIcon()).dontAnimate()
                        .override((int) getResources().getDimension(R.dimen.drawer_image_width)
                                , (int) getResources().getDimension(R.dimen.drawer_image_high))
                        .placeholder(R.drawable.ic_action_account_circle).into(icon);
            }

            @Override
            public void onFailure() {
            }
        }, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(currentPage);
    }

    private void switchFragment(final MenuItem menuItem) {
        final int menuId = menuItem.getItemId();
        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                Fragment fragment;
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                Slide slide = new Slide();
                slide.setDuration(400);
                slide.setSlideEdge(Gravity.RIGHT);
                if (Build.VERSION.SDK_INT > 21) {
                    toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color_primary));
                } else {
                    toolbar.setBackgroundColor(getResources().getColor(R.color.color_primary));
                }
                switch (menuId) {
                    case R.id.navigation_item_main:
                        currentPage = menuItem.getItemId();
                        fragment = new MainPageFragment();
                        fragment.setEnterTransition(slide);
                        ft.replace(R.id.frame_content, fragment).commit();
                        toolbar.setTitle(getString(R.string.drawer_home));
                        tracker.setScreenName(AnalyzticsTag.SCREEN_MAIN);
                        break;
                    case R.id.navigation_item_create_vote:
                        startActivity(new Intent(MainActivity.this, CreateVoteActivity.class));
                        break;
                    case R.id.navigation_item_list_my_box:
                        startActivity(new Intent(MainActivity.this, UserActivity.class));
                        break;
                    case R.id.navigation_item_search:
                        tracker.setScreenName(AnalyzticsTag.SCREEN_SEARCH);
                        currentPage = menuItem.getItemId();
                        fragment = new SearchFragment();
                        fragment.setEnterTransition(slide);
                        ft.replace(R.id.frame_content, fragment).commit();
                        toolbar.setTitle(R.string.drawer_search);
                        Bundle argument = new Bundle();
                        argument.putString(SearchFragment.KEY_SEARCH_KEYWORD, searchKeyword);
                        fragment.setArguments(argument);
                        break;
                    case R.id.navigation_item_account:
                        tracker.setScreenName(AnalyzticsTag.SCREEN_ACCOUNT);
                        currentPage = menuItem.getItemId();
                        AccountFragment accountFragment = new AccountFragment();
                        accountFragment.setEnterTransition(slide);
                        ft.replace(R.id.frame_content, accountFragment).commit();
                        int bgColor = ContextCompat.getColor(getApplicationContext(), R.color.md_light_blue_100);
                        toolbar.setBackgroundColor(bgColor);
                        toolbar.setTitle(R.string.drawer_account);
                        break;
                    case R.id.navigation_item_about:
                        tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT);
                        currentPage = menuItem.getItemId();
                        AboutFragment aboutFragment = new AboutFragment();
                        aboutFragment.setEnterTransition(slide);
                        ft.replace(R.id.frame_content, aboutFragment).commit();
                        toolbar.setTitle(R.string.drawer_about);
                        break;
                }
                navigationView.setCheckedItem(currentPage);
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }
        }, 500);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (currentPage != navigationView.getMenu().getItem(0).getItemId()) {
            currentPage = navigationView.getMenu().getItem(0).getItemId();
            navigationView.getMenu().getItem(0).setChecked(true);
            switchFragment(navigationView.getMenu().getItem(0));
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.wall_item_toast_double_click_to_exit, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.vote_detail_menu_search_hint));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(queryListener);
        }
        return true;
    }

    final private SearchView.OnQueryTextListener queryListener =
            new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchKeyword = newText;
                    if (searchKeyword.length() == 0) {
                        if (currentPage == navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId()) {
                            EventBus.getDefault().post(new EventBusManager.UIControlEvent(
                                    EventBusManager.UIControlEvent.SEARCH_KEYWORD, ""));
                        }
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchKeyword = query;
                    Log.d(TAG, "onQueryTextSubmit:" + query + "  page:" + currentPage
                            + " search page:" + navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId());
                    if (currentPage != navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId()) {
                        switchFragment(navigationView.getMenu().findItem(R.id.navigation_item_search));
                        navigationView.getMenu().findItem(R.id.navigation_item_search).setChecked(true);
                    } else {
                        EventBus.getDefault().post(new EventBusManager.UIControlEvent(
                                EventBusManager.UIControlEvent.SEARCH_KEYWORD, searchKeyword));
                    }
                    return false;
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            startActivity(new Intent(MainActivity.this, CreateVoteActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_content);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
