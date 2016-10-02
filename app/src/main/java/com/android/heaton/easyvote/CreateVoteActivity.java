package com.android.heaton.easyvote;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by heaton on 16/1/10.
 */
public class CreateVoteActivity extends AppCompatActivity {


    @Bind(R.id.create_vote_toolbar)
    Toolbar mainToolbar;
    @Bind(R.id.txtTitle)
    TextView txtTitle;
    @Bind(R.id.edtTitle)
    EditText edtTitle;
    @Bind(R.id.tabLayoutCreateVote)
    TabLayout tabLayoutCreateVote;
    @Bind(R.id.vpSubArea)
    ViewPager vpSubArea;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cteate_vote);

        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.create_vote_toolbar);
        toolbar.setTitle("New Vote");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setElevation(10);
        setSupportActionBar(toolbar);
        vpSubArea.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayoutCreateVote.setupWithViewPager(vpSubArea);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ButterKnife.bind(this);

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
            switch(i) {
                case 0: return CreateVoteTabFragment.newTabFragment(CreateVoteTabFragment.TAB_OPTIONS);
                case 1: return CreateVoteTabFragment.newTabFragment(CreateVoteTabFragment.TAB_SETTINGS);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position) {
                case 0: return getString(R.string.create_vote_tab_options);
                case 1: return getString(R.string.create_vote_tab_settings);
            }
            return "";
        }
    }

}
