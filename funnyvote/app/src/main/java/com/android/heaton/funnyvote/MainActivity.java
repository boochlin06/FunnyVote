package com.android.heaton.funnyvote;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.ui.account.AccountFragment;
import com.android.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.android.heaton.funnyvote.ui.main.MainPageFragment;
import com.android.heaton.funnyvote.ui.personal.UserActivity;
import com.android.heaton.funnyvote.ui.search.SearchFragment;
import com.bumptech.glide.Glide;


import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final int ANIM_DURATION_TOOLBAR = 300;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private int mCurrentPage;
    boolean doubleBackToExitPressedOnce = false;
    private SearchView searchView;
    private SearchFragment searchFragment;
    private String searchKeyword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_content
                , new MainPageFragment()).commit();
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(getString(R.string.drawer_home));
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open,
                R.string.drawer_close);
        drawerToggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        mCurrentPage = R.id.navigation_item_main;

        setupDrawerContent(navigationView);
        setupDrawerHeader();
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
                        if (mCurrentPage != menuItem.getItemId()) {
                            switchFragment(menuItem);
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });

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
                        .override(92, 92).placeholder(R.drawable.ic_action_account_circle).into(icon);
            }

            @Override
            public void onFailure() {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(mCurrentPage);
    }

    private void switchFragment(final MenuItem menuItem) {
        final int menuId = menuItem.getItemId();
        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fragment_enter_from_left, 0);
                if (Build.VERSION.SDK_INT > 21) {
                    toolbar.setBackgroundColor(getColor(R.color.color_primary));
                } else {
                    toolbar.setBackgroundColor(getResources().getColor(R.color.color_primary));
                }
                switch (menuId) {
                    case R.id.navigation_item_main:
                        mCurrentPage = menuItem.getItemId();
                        ft.replace(R.id.frame_content, new MainPageFragment()).commit();
                        toolbar.setTitle(getString(R.string.drawer_home));
                        break;
                    case R.id.navigation_item_create_vote:
                        startActivity(new Intent(MainActivity.this, CreateVoteActivity.class));
                        break;
                    case R.id.navigation_item_list_my_box:
                        startActivity(new Intent(MainActivity.this, UserActivity.class));
                        break;
                    case R.id.navigation_item_search:
                        mCurrentPage = menuItem.getItemId();
                        searchFragment = new SearchFragment();
                        ft.replace(R.id.frame_content, searchFragment).commit();
                        toolbar.setTitle(R.string.drawer_search);
                        searchFragment.setQueryText(searchKeyword);
                        break;
                    case R.id.navigation_account:
                        mCurrentPage = menuItem.getItemId();
                        ft.replace(R.id.frame_content, new AccountFragment()).commit();
                        int bgColor;
                        if (Build.VERSION.SDK_INT >= 23) {
                            bgColor = getColor(R.color.md_light_blue_100);
                        } else {
                            bgColor = getResources().getColor(R.color.md_light_blue_100);
                        }
                        toolbar.setBackgroundColor(bgColor);
                        toolbar.setTitle(R.string.drawer_account);
                        break;
                }
            }
        }, 200);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);

        }
        if (mCurrentPage != navigationView.getMenu().getItem(0).getItemId()) {
            mCurrentPage = navigationView.getMenu().getItem(0).getItemId();
            navigationView.getMenu().getItem(0).setChecked(true);
            switchFragment(navigationView.getMenu().getItem(0));
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.Wall_item_toast_double_click_to_exit, Toast.LENGTH_SHORT).show();

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
        searchView.setQueryHint(getString(R.string.vote_detail_menu_search_hint));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(queryListener);
        return true;
    }

    final private SearchView.OnQueryTextListener queryListener =
            new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    searchKeyword = newText;
                    if (searchKeyword.length() == 0) {
                        if (mCurrentPage == navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId()) {
                            searchFragment.setQueryText(searchKeyword);
                        }
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchKeyword = query;
                    Log.d("test","onQueryTextSubmit:"+query + "  page:"+mCurrentPage
                            +" search page:"+navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId());
                    if (mCurrentPage != navigationView.getMenu().findItem(R.id.navigation_item_search).getItemId()) {
                        switchFragment(navigationView.getMenu().findItem(R.id.navigation_item_search));
                        navigationView.getMenu().findItem(R.id.navigation_item_search).setChecked(true);
                    } else {
                        searchFragment.setQueryText(searchKeyword);
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
