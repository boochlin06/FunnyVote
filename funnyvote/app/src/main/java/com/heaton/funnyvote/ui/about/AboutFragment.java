package com.heaton.funnyvote.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.databinding.FragmentAboutBinding;
import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.ui.about.aboutapp.AboutAppActivity;
import com.heaton.funnyvote.ui.about.authorinfo.AuthorInfoActivity;
import com.heaton.funnyvote.ui.about.licence.LicenceActivity;
import com.heaton.funnyvote.ui.about.problem.ProblemActivity;
import com.heaton.funnyvote.ui.introduction.IntroductionActivity;
import com.heaton.funnyvote.utils.Util;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

/**
 * Created by heaton on 2017/3/2.
 */
@ActivityScoped
public class AboutFragment extends DaggerFragment implements AboutContract.View {
    private FragmentAboutBinding binding;
    private Tracker tracker;

    @Inject
    AboutPresenter presenter;

    @Inject
    public AboutFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        if (presenter != null) {
            presenter.takeView(this);
        }

        FunnyVoteApplication application = (FunnyVoteApplication) requireActivity().getApplication();
        tracker = application.getDefaultTracker();
        try {
            PackageInfo pinfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            String versionName = pinfo.versionName;
            binding.txtVersionName.setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        binding.txtTutorial.setOnClickListener(v -> presenter.IntentToIntroduction());
        binding.txtAuthorInfo.setOnClickListener(v -> presenter.IntentToAuthorInfo());
        binding.txtLicence.setOnClickListener(v -> presenter.IntentToLicence());
        binding.txtProblem.setOnClickListener(v -> presenter.IntentToProblem());
        binding.txtUpdate.setOnClickListener(v -> presenter.IntentToAppStore());
        binding.txtAppIntroduction.setOnClickListener(v -> presenter.IntentToAbout());
        binding.btnShareApp.setOnClickListener(v -> presenter.IntentToShareApp());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter != null) {
            presenter.takeView(this);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (presenter != null) {
            presenter.dropView();
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
        if (tracker != null) {
            tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_UPDATE_APP);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        final String appPackageName = requireActivity().getPackageName();

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException anfe) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
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


    public void setPresenter(AboutPresenter presenter) {
        this.presenter = presenter;
    }
}
