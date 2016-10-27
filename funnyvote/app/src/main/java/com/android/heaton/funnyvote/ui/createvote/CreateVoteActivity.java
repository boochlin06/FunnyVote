package com.android.heaton.funnyvote.ui.createvote;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 16/1/10.
 */
public class CreateVoteActivity extends AppCompatActivity {


    @BindView(R.id.create_vote_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.edtTitle)
    EditText edtTitle;
    @BindView(R.id.tabLayoutCreateVote)
    TabLayout tabLayoutCreateVote;
    @BindView(R.id.vpSubArea)
    ViewPager vpSubArea;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cteate_vote);

        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.create_vote_toolbar);
        toolbar.setTitle(getString(R.string.create_vote_toolbar_title));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setElevation(10);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(toolbar);
        vpSubArea.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayoutCreateVote.setupWithViewPager(vpSubArea);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ButterKnife.bind(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_vote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_submit) {
            EventBus.getDefault().post(new EventBusController.SubmitMessageEvent
                    (EventBusController.SubmitMessageEvent.EVENT_SUBMIT));
            finish();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
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
                    return CreateVoteTabFragment.newTabFragment(CreateVoteTabFragment.TAB_OPTIONS);
                case 1:
                    return CreateVoteTabFragment.newTabFragment(CreateVoteTabFragment.TAB_SETTINGS);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.create_vote_tab_options);
                case 1:
                    return getString(R.string.create_vote_tab_settings);
            }
            return "";
        }
    }

}
