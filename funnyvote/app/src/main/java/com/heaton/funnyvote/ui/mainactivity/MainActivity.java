package com.heaton.funnyvote.ui.mainactivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.Slide;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.material.navigation.NavigationView;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.notification.VoteNotificationManager;
import com.heaton.funnyvote.ui.about.AboutFragment;
import com.heaton.funnyvote.ui.account.AccountFragment;
import com.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.heaton.funnyvote.ui.main.MainPageFragment;
import com.heaton.funnyvote.ui.personal.UserActivity;
import com.heaton.funnyvote.ui.search.SearchFragment;

import javax.inject.Inject;

import dagger.Lazy;
import dagger.android.support.DaggerAppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

@ActivityScoped
public class MainActivity extends DaggerAppCompatActivity implements MainActivityContract.View {

    private static final int ANIM_DURATION_TOOLBAR = 300;
    public static String TAG = MainActivity.class.getSimpleName();
    public static boolean ENABLE_ADMOB = false;
    @Inject
    public MainActivityPresenter presenter;
    boolean doubleBackToExitPressedOnce = false;
    @Inject
    Lazy<SearchFragment> searchFragmentProvider;
    @Inject
    Lazy<AboutFragment> aboutFragmentProvider;
    @Inject
    Lazy<AccountFragment> accountFragmentProvider;
    @Inject
    Lazy<MainPageFragment> mainPageFragmentProvider;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private static int currentPage;
    private SearchView searchView;
    private AdView adView;
    private Tracker tracker;
    final private SearchView.OnQueryTextListener queryListener =
            new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d(TAG, "onQueryTextSubmit:" + query + "  page:" + currentPage
                            + " search page:" + navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId());

                    if (currentPage != navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId()) {
                        switchFragment(navigationView.getMenu().findItem(R.id.navigation_item_search), query);
                        navigationView.getMenu().findItem(R.id.navigation_item_search).setChecked(true);
                    }
                    return true;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        toolbar = findViewById(R.id.toolbarMain);
        adView = findViewById(R.id.adView);
        toolbar.setTitle(getString(R.string.drawer_home));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,
                R.string.drawer_close);
        drawerToggle.syncState();

        ENABLE_ADMOB = getResources().getBoolean(R.bool.enable_main_admob);

        navigationView = findViewById(R.id.navigation_view);
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
        setUpAdmob();

        VoteNotificationManager.getInstance(getApplicationContext()).startNotificationAlarm();
        presenter.takeView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.dropView();
    }

    private void setUpAdmob() {
        if (ENABLE_ADMOB) {
            try {
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            } catch (Exception e) {
                adView.setVisibility(View.GONE);
            }
        } else {
            adView.setVisibility(View.GONE);
        }
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        drawerLayout.addDrawerListener(drawerToggle);
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

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(currentPage);
    }

    private void switchFragment(final MenuItem menuItem) {
        switchFragment(menuItem, "");
    }

    private void switchFragment(final MenuItem menuItem, final String searchKeyword) {
        final int menuId = menuItem.getItemId();
        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.color_primary));
                int itemId = menuItem.getItemId();
                if (itemId == R.id.navigation_item_main) {
                    presenter.IntentToMainPage();
                    currentPage = menuItem.getItemId();
                } else if (itemId == R.id.navigation_item_create_vote) {
                    presenter.IntentToCreatePage();
                } else if (itemId == R.id.navigation_item_list_my_box) {
                    presenter.IntentToUserPage();
                } else if (itemId == R.id.navigation_item_search) {
                    presenter.IntentToSearchPage(searchKeyword);
                    currentPage = menuItem.getItemId();
                } else if (itemId == R.id.navigation_item_account) {
                    presenter.IntentToAccountPage();
                    currentPage = menuItem.getItemId();
                } else if (itemId == R.id.navigation_item_about) {
                    presenter.IntentToAboutPage();
                    currentPage = menuItem.getItemId();
                }
                navigationView.setCheckedItem(currentPage);
                if (tracker != null) {
                    tracker.send(new HitBuilders.ScreenViewBuilder().build());
                }
            }
        }, 300);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.vote_detail_menu_search_hint));
                searchView.setSubmitButtonEnabled(true);
                searchView.setOnQueryTextListener(queryListener);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add) {
            presenter.IntentToCreatePage();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_content);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void showSearchPage(String searchKeyword) {
        SearchFragment fragment = searchFragmentProvider.get();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Slide slide = new Slide();
        slide.setDuration(400);
        slide.setSlideEdge(Gravity.RIGHT);
        if (tracker != null) {
            tracker.setScreenName(AnalyzticsTag.SCREEN_SEARCH);
        }
        Bundle searchArgument = new Bundle();
        searchArgument.putString(SearchFragment.KEY_SEARCH_KEYWORD, searchKeyword);
        fragment.setArguments(searchArgument);
        ft.replace(R.id.frame_content, fragment).commit();
        toolbar.setTitle(R.string.drawer_search);
    }

    @Override
    public void showCreatePage() {
        startActivity(new Intent(MainActivity.this, CreateVoteActivity.class));
    }

    @Override
    public void showUserPage() {
        startActivity(new Intent(MainActivity.this, UserActivity.class));
    }

    @Override
    public void showMainPage() {
        Fragment fragment = mainPageFragmentProvider.get();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_content, fragment).commit();
        toolbar.setTitle(getString(R.string.drawer_home));
        if (tracker != null) {
            tracker.setScreenName(AnalyzticsTag.SCREEN_MAIN);
        }
    }

    @Override
    public void showAboutPage() {
        Fragment fragment = aboutFragmentProvider.get();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_content, fragment).commit();
        toolbar.setTitle(R.string.drawer_about);
        if (tracker != null) {
            tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT);
        }
    }

    @Override
    public void showAccountPage() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (tracker != null) {
            tracker.setScreenName(AnalyzticsTag.SCREEN_ACCOUNT);
        }
        Fragment accountFragment = accountFragmentProvider.get();
        ft.replace(R.id.frame_content, accountFragment).commit();
        int bgColor = ContextCompat.getColor(getApplicationContext(), R.color.md_light_blue_100);
        toolbar.setBackgroundColor(bgColor);
        toolbar.setTitle(R.string.drawer_account);
    }

    @Override
    public void updateUserView(User user) {
        View header = navigationView.getHeaderView(0);
        CircleImageView icon = header.findViewById(R.id.imgUserIcon);
        TextView name = header.findViewById(R.id.txtUserName);
        name.setText(user.getUserName());
        Glide.with(MainActivity.this).load(user.getUserIcon()).dontAnimate()
                .override((int) getResources().getDimension(R.dimen.drawer_image_width)
                        , (int) getResources().getDimension(R.dimen.drawer_image_high))
                .placeholder(R.drawable.ic_action_account_circle).into(icon);
    }
}
