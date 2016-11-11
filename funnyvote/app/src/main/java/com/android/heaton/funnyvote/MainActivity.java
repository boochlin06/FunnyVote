package com.android.heaton.funnyvote;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.android.heaton.funnyvote.ui.AccountFragment;
import com.android.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.android.heaton.funnyvote.ui.main.MainPageFragment;

public class MainActivity extends AppCompatActivity {

    private static final int ANIM_DURATION_TOOLBAR = 300;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    private int mCurrentPage;

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
        drawerLayout.setDrawerListener(drawerToggle);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        mCurrentPage = R.id.navigation_item_main;

        setupDrawerContent(navigationView);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        if (mCurrentPage != menuItem.getItemId()) {
                            mCurrentPage = menuItem.getItemId();
                            switchFragment(menuItem);
                            menuItem.setChecked(true);
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void switchFragment(MenuItem menuItem) {
        final int menuId = menuItem.getItemId();
        drawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.fragment_enter_from_left, 0);
                toolbar.setBackgroundColor(getColor(R.color.color_primary));
                switch (menuId) {
                    case R.id.navigation_item_main:
                        ft.replace(R.id.frame_content, new MainPageFragment()).commit();
                        toolbar.setTitle(getString(R.string.drawer_home));
                        break;
                    case R.id.navigation_item_create_vote:
                        startActivity(new Intent(MainActivity.this, CreateVoteActivity.class));
                        break;
                    case R.id.navigation_item_list_history:
                        ft.replace(R.id.frame_content, new HistoryFragment()).commit();
                        toolbar.setTitle(R.string.drawer_history);
                        break;
                    case R.id.navigation_item_list_favorite:
                        ft.replace(R.id.frame_content, new FavoriteFragment()).commit();
                        toolbar.setTitle(R.string.drawer_favorite);
                        break;
                    case R.id.navigation_account:
                        ft.replace(R.id.frame_content, new AccountFragment()).commit();
                        toolbar.setBackgroundColor(getColor(R.color.md_light_blue_100));
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
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            startActivity(new Intent(MainActivity.this, CreateVoteActivity.class));
            return true;
        } else if (id == R.id.menu_search) {

        }

        return super.onOptionsItemSelected(item);
    }
}
