package com.heaton.funnyvote.ui.about.licence;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.databinding.ActivityLicenceBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2017/3/2.
 */
public class LicenceActivity extends AppCompatActivity implements LicenceContract.View {
    private ActivityLicenceBinding binding;
    private Tracker tracker;
    private LicenceContract.Presenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLicenceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();

        String[] titles = getResources().getStringArray(R.array.licences_title);
        String[] descs = getResources().getStringArray(R.array.licences_desc);
        List<LicenceItem> licenceItemList = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            licenceItemList.add(new LicenceItem(titles[i], descs[i]));
        }
        binding.ryLicence.setAdapter(new LicenceItemAdapter(licenceItemList));

        binding.mainToolbar.setTitle(getString(R.string.about_licence));
        binding.mainToolbar.setTitleTextColor(Color.WHITE);
        binding.mainToolbar.setElevation(10);
        binding.mainToolbar.setNavigationOnClickListener(v -> finish());
        setSupportActionBar(binding.mainToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPresenter(LicenceContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public static class LicenceItem {
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