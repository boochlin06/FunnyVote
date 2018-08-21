package com.heaton.funnyvote.ui.about.problem;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;

/**
 * Created by heaton on 2017/3/2.
 */

public class ProblemActivity extends AppCompatActivity implements ProblemContract.View {
    private Toolbar mainToolbar;
    private Tracker tracker;
    private ProblemContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_problem);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        mainToolbar.setTitle(getString(R.string.about_common_problem));
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

        presenter = new ProblemPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_PROBLEM);
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

}
