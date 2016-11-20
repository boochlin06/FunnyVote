package com.android.heaton.funnyvote.ui.createvote;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.android.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 16/1/10.
 */
public class CreateVoteActivity extends AppCompatActivity {

    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.edtTitle)
    EditText edtTitle;
    @BindView(R.id.tabLayoutCreateVote)
    TabLayout tabLayoutCreateVote;
    @BindView(R.id.vpSubArea)
    ViewPager vpSubArea;
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.circleLoad)
    CircleProgressView circleLoad;
    Toolbar mainToolbar;
    private CreateVoteTabSettingFragment settingFragment;
    private CreateVoteTabOptionFragment optionFragment;
    private long newOptionIdAuto = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cteate_vote);
        ButterKnife.bind(this);
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

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
            submitCreateVote();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void submitCreateVote() {
        VoteData voteSetting = settingFragment.getVoteSettings();
        List<Option> optionList = optionFragment.getOptionList();
        StringBuilder sb = new StringBuilder();
        int errorNumber = 0;
        int optionCount = optionList.size();
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getTitle() == null || optionList.get(i).getTitle().length() == 0) {
                errorNumber++;
                sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_fill_all) + "\n");
                break;
            }
        }
        if (edtTitle.getText().length() == 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_title_empty) + "\n");
        } else {
            voteSetting.setTitle(edtTitle.getText().toString());
        }
        if (voteSetting.getAuthorName() == null || voteSetting.getAuthorName().isEmpty()) {
            voteSetting.setAuthorName(getString(R.string.create_vote_tab_settings_anonymous));
        }
        if (voteSetting.getMaxOption() == 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_option_0) + "\n");
        }
        if (voteSetting.getMinOption() == 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_min_option_0) + "\n");
        }
        if (voteSetting.getMaxOption() < voteSetting.getMinOption()) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_min) + "\n");
        }
        if (voteSetting.getMaxOption() > optionCount) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_total) + "\n");
        }
        if (voteSetting.getEndTime() < System.currentTimeMillis()) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_endtime_more_than_now) + "\n");
        }
        if (voteSetting.getIsNeedPassword() && voteSetting.password.length() <= 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_password_empty) + "\n");
        }

        if (errorNumber == 0) {
            // SHOW SHARE VOTE DIALOG
            new UpdateVoteDataTask(voteSetting, optionList).execute();
            finish();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.create_vote_dialog_error_title);
            builder.setMessage(sb.toString());
            builder.setPositiveButton(R.string.create_vote_dialog_error_done, null);
            builder.show();
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
                    if (optionFragment == null) {
                        optionFragment = CreateVoteTabOptionFragment.newTabFragment();
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
            optionFragment.getOptionList().add(option);
            optionFragment.notifyOptionChange();
            // refresh option
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_REMOVE)) {
            if (optionFragment.getOptionList().size() <= 2) {
                Toast.makeText(getApplicationContext(), getString(R.string.create_vote_toast_less_than_2_option)
                        , Toast.LENGTH_LONG).show();
                return;
            }
            int removePosition = -1;
            for (int i = 0; i < optionFragment.getOptionList().size(); i++) {
                if (optionFragment.getOptionList().get(i).getId() == id) {
                    removePosition = i;
                    break;
                }
            }
            if (removePosition >= 0) {
                optionFragment.getOptionList().remove(removePosition);
                optionFragment.notifyOptionChange();
            }
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_INPUT_TEXT)) {
            int targetPosition = -1;
            for (int i = 0; i < optionFragment.getOptionList().size(); i++) {
                if (optionFragment.getOptionList().get(i).getId() == id) {
                    targetPosition = i;
                    break;
                }
            }
            if (targetPosition >= 0) {
                optionFragment.getOptionList().get(targetPosition).setTitle(event.inputText);
            }
        }
    }

    private class UpdateVoteDataTask extends AsyncTask<Void, Void, Void> {

        private VoteData voteSetting;
        private List<Option> optionList;

        public UpdateVoteDataTask(VoteData voteSetting, List<Option> optionList) {
            this.voteSetting = voteSetting;
            this.optionList = optionList;
        }

        @Override
        protected void onPreExecute() {
            circleLoad.setText(getString(R.string.vote_detail_circle_updating));
            circleLoad.setVisibility(View.VISIBLE);
            circleLoad.spin();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            circleLoad.stopSpinning();
            circleLoad.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), R.string.create_vote_create_successful, Toast.LENGTH_LONG).show();
            VHVoteWallItem.startActivityToVoteDetail(getApplicationContext(), voteSetting.getVoteCode());
            circleLoad.postDelayed(new Runnable() {
                @Override
                public void run() {
                    VoteDetailContentActivity.sendShareIntent(getApplicationContext(), voteSetting);
                }
            }, 1000);
        }

        @Override
        protected Void doInBackground(Void... params) {
            voteSetting.setVoteCode(Long.toString(System.currentTimeMillis()));
            voteSetting.setOptionCount(optionList.size());
            // For test.
            voteSetting.setVoteImage("http://vinta.ws/booch/wp-content/uploads/2016/11/handsup.png");
            for (int i = 0; i < optionList.size(); i++) {
                Option option = optionList.get(i);
                option.setVoteCode(voteSetting.getVoteCode());
                option.setId(null);
                option.setCount(0);
                option.setIsUserChoiced(false);
                option.setCode(voteSetting.getVoteCode() + "_" + i);
                option.dumpDetail();
                if (i == 0) {
                    voteSetting.setOption1Title(option.getTitle());
                    voteSetting.setOption1Code(option.getCode());
                    voteSetting.setOption1Count(0);
                } else if (i == 1) {
                    voteSetting.setOption2Title(option.getTitle());
                    voteSetting.setOption2Code(option.getCode());
                    voteSetting.setOption2Count(0);
                }
            }
            DataLoader.getInstance(getApplicationContext()).getVoteDataDao().insert(voteSetting);
            DataLoader.getInstance(getApplicationContext()).getOptionDao().insertInTx(optionList);
            return null;
        }
    }
}
