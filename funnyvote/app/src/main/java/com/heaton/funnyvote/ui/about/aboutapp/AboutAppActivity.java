package com.heaton.funnyvote.ui.about.aboutapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.ui.about.AboutFragment;
import com.heaton.funnyvote.ui.about.aboutapp.AboutAppContract;
import com.heaton.funnyvote.ui.about.aboutapp.AboutAppPresenter;
import com.heaton.funnyvote.utils.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 2017/3/4.
 */

public class AboutAppActivity extends AppCompatActivity implements AboutAppContract.View {
    @BindView(R.id.txtAppDesc)
    TextView txtAppDesc;
    private Toolbar mainToolbar;
    private Tracker tracker;
    protected AboutAppContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        ButterKnife.bind(this);
        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        mainToolbar.setTitle(getString(R.string.about_funnyvote));
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

        String desc = getString(R.string.about_introduction_desc);
        txtAppDesc.setText(Html.fromHtml(desc));
        presenter = new AboutAppPresenter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_FUNNYVOTE_APP);
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

    @OnClick(R.id.btnShareApp)
    public void onClick(View view) {
        presenter.shareApp();
    }

    @Override
    public void showShareApp() {
        Util.sendShareAppIntent(getApplicationContext());
    }

    @Override
    public void setPresenter(AboutAppContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
