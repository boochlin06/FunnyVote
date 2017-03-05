package com.heaton.funnyvote.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.heaton.funnyvote.ui.ShareDialogActivity;
import com.heaton.funnyvote.ui.introduction.IntroductionActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 2017/3/2.
 */

public class AboutFragment extends Fragment {
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

    private Tracker tracker;
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
    }

    @OnClick({R.id.txtTutorial, R.id.txtAuthorInfo, R.id.txtLicence, R.id.txtProblem, R.id.txtAppIntroduction
            , R.id.txtUpdate, R.id.btnShareApp})
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.txtTutorial:
                startActivity(new Intent(getActivity(), IntroductionActivity.class));
                break;
            case R.id.txtAuthorInfo:
                startActivity(new Intent(getActivity(), AuthorInfoActivity.class));
                break;
            case R.id.txtLicence:
                startActivity(new Intent(getActivity(), LicenceActivity.class));
                break;
            case R.id.txtProblem:
                startActivity(new Intent(getActivity(), ProblemActivity.class));
                break;
            case R.id.txtUpdate:
                tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_UPDATE_APP);
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
                final String appPackageName = getActivity().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW
                            , Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW
                            , Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case R.id.txtAppIntroduction:
                startActivity(new Intent(getActivity(), AboutAppActivity.class));
                break;
            case R.id.btnShareApp:
                sendShareAppIntent(getActivity());
                break;
        }
    }

    public static void sendShareAppIntent(Context context) {
        final String appPackageName = context.getApplicationContext().getPackageName();
        String appURL = "https://play.google.com/store/apps/details?id=" + appPackageName;
        Intent shareDialog = new Intent(context, ShareDialogActivity.class);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, appURL);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IS_SHARE_APP, true);
        shareDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(shareDialog);
    }
}
