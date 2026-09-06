package com.heaton.funnyvote.ui.about.aboutapp;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.databinding.ActivityAboutAppBinding;
import com.heaton.funnyvote.utils.Util;

/**
 * Created by heaton on 2017/3/4.
 */
public class AboutAppActivity extends AppCompatActivity implements AboutAppContract.View {
    private ActivityAboutAppBinding binding;
    private Tracker tracker;
    protected AboutAppContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutAppBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();

        binding.mainToolbar.setTitle(getString(R.string.about_funnyvote));
        binding.mainToolbar.setTitleTextColor(Color.WHITE);
        binding.mainToolbar.setElevation(10);
        binding.mainToolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(binding.mainToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        String desc = getString(R.string.about_introduction_desc);
        binding.txtAppDesc.setText(Html.fromHtml(desc));
        binding.btnShareApp.setOnClickListener(v -> presenter.shareApp());

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
            return true;
        }
        return super.onOptionsItemSelected(item);
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
