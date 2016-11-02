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
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

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
    private CreateVoteTabSettingFragment settingFragment;
    private CreateVoteTabOptionFragment optionFragment;
    private long newOptionIdAuto = 0;
    private List<Option> optionList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cteate_vote);
        ButterKnife.bind(this);

        mainToolbar.setTitle(getString(R.string.create_vote_toolbar_title));
        mainToolbar.setTitleTextColor(Color.WHITE);
        mainToolbar.setElevation(10);

        mainToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mainToolbar);

        optionList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Option option = new Option();
            option.setId(newOptionIdAuto++);
            option.setCount(0);
            optionList.add(option);
        }

        vpSubArea.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayoutCreateVote.setupWithViewPager(vpSubArea);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
                    if (optionFragment == null) {
                        optionFragment = CreateVoteTabOptionFragment.newTabFragment(optionList);
                    }
                    return optionFragment;
                case 1:
                    if (settingFragment == null) {
                        settingFragment = CreateVoteTabSettingFragment.newTabFragment();
                    }
                    return settingFragment;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionControl(EventBusController.OptionControlEvent event) {
        long id = event.Id;
        if (event.message.equals(EventBusController.OptionControlEvent.OPTION_ADD)) {
            Option option = new Option();
            option.setCount(0);
            option.setId(newOptionIdAuto++);
            optionList.add(option);
            optionFragment.notifyOptionChange();
            // refresh option
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_REMOVE)) {
            int removePosition = -1;
            for (int i = 0; i < optionList.size(); i++) {
                if (optionList.get(i).getId() == id) {
                    removePosition = i;
                    break;
                }
            }
            if (removePosition >= 0) {
                optionList.remove(removePosition);
                optionFragment.notifyOptionChange();
            }
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_INPUT_TEXT)) {
            int targetPosition = -1;
            for (int i = 0; i < optionList.size(); i++) {
                if (optionList.get(i).getId() == id) {
                    targetPosition = i;
                    break;
                }
            }
            if (targetPosition >= 0) {
                optionList.get(targetPosition).setTitle(event.inputText);
            }
        }
    }
}
