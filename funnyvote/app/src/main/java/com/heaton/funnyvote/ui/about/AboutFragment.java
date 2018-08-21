package com.heaton.funnyvote.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.ui.about.aboutapp.AboutAppActivity;
import com.heaton.funnyvote.ui.about.authorinfo.AuthorInfoActivity;
import com.heaton.funnyvote.ui.about.licence.LicenceActivity;
import com.heaton.funnyvote.ui.about.problem.ProblemActivity;
import com.heaton.funnyvote.ui.introduction.IntroductionActivity;
import com.heaton.funnyvote.utils.Util;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 2017/3/2.
 */

@ActivityScoped
public class AboutFragment extends dagger.android.support.DaggerFragment implements AboutContract.View {
    @BindView(R.id.txtTutorial)
    TextView txtTutorial;
    @BindView(R.id.txtAuthorInfo)
    TextView txtAuthorInfo;
    @BindView(R.id.txtLicence)
    TextView txtLicence;
    @BindView(R.id.txtProblem)
    TextView txtProblem;
    @BindView(R.id.txtVersionName)
    TextView txtVersionName;
    @BindView(R.id.txtUpdate)
    TextView txtUpdate;
    @BindView(R.id.btnShareApp)
    CardView btnShareApp;
    @Inject
    AboutPresenter presenter;
    private Tracker tracker;

    @Inject
    public AboutFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, null);
        ButterKnife.bind(this, view);

        FunnyVoteApplication application = (FunnyVoteApplication) getActivity().getApplication();
        tracker = application.getDefaultTracker();
        PackageInfo pinfo = null;
        try {
            pinfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String versionName = pinfo.versionName;
            txtVersionName.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.takeView(this);
        //presenter = new AboutPresenter(this);
    }

    @OnClick({R.id.txtTutorial, R.id.txtAuthorInfo, R.id.txtLicence, R.id.txtProblem, R.id.txtAppIntroduction
            , R.id.txtUpdate, R.id.btnShareApp})
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.txtTutorial:
                presenter.IntentToIntroduction();
                break;
            case R.id.txtAuthorInfo:
                presenter.IntentToAuthorInfo();
                break;
            case R.id.txtLicence:
                presenter.IntentToLicence();
                break;
            case R.id.txtProblem:
                presenter.IntentToProblem();
                break;
            case R.id.txtUpdate:
                presenter.IntentToAppStore();
                break;
            case R.id.txtAppIntroduction:
                presenter.IntentToAbout();
                break;
            case R.id.btnShareApp:
                presenter.IntentToShareApp();
                break;
        }
    }

    @Override
    public void showIntroduction() {
        startActivity(new Intent(getActivity(), IntroductionActivity.class));
    }

    @Override
    public void showAuthorInfo() {
        startActivity(new Intent(getActivity(), AuthorInfoActivity.class));
    }

    @Override
    public void showLicence() {
        startActivity(new Intent(getActivity(), LicenceActivity.class));
    }

    @Override
    public void showProblem() {
        startActivity(new Intent(getActivity(), ProblemActivity.class));
    }

    @Override
    public void showAppStore() {
        tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_UPDATE_APP);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        final String appPackageName = getActivity().getPackageName();

        try {
            Intent intent = new Intent(new Intent(Intent.ACTION_VIEW
                    , Uri.parse("market://details?id=" + appPackageName)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Intent intent = new Intent(new Intent(Intent.ACTION_VIEW
                    , Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void showAbout() {
        startActivity(new Intent(getActivity(), AboutAppActivity.class));
    }

    @Override
    public void showShareApp() {
        Util.sendShareAppIntent(getActivity());
    }

}
