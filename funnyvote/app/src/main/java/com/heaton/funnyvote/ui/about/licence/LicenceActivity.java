package com.heaton.funnyvote.ui.about.licence;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2017/3/2.
 */

public class LicenceActivity extends AppCompatActivity implements LicenceContract.View {
    @BindView(R.id.ryLicence)
    RecyclerView ryLicence;
    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;
    private Tracker tracker;
    private LicenceContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);
        ButterKnife.bind(this);
        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        String[] titles = getResources().getStringArray(R.array.licences_title);
        String[] descs = getResources().getStringArray(R.array.licences_desc);
        List<LicenceItem> licenceItemList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            licenceItemList.add(new LicenceItem(titles[i], descs[i]));
        }
        ryLicence.setAdapter(new LicenceItemAdapter(licenceItemList));
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        mainToolbar.setTitle(getString(R.string.about_licence));
        mainToolbar.setTitleTextColor(Color.WHITE);
        mainToolbar.setElevation(10);

        mainToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        presenter = new LicencePresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_LICENCE);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public class LicenceItem {
        private String title;
        private String desc;

        public LicenceItem(String title, String desc) {
            this.title = title;
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}