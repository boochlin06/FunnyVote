package com.heaton.funnyvote.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;

import org.apmem.tools.layouts.FlowLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chiu_mac on 2016/11/10.
 */

public class ShareDialogActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ShareDialogActivity.class.getSimpleName();
    private static final String[][] APPS = {
            {"com.facebook.katana", "com.facebook.composer.shareintent.ImplicitShareIntentHandlerDefaultAlias"},
            {"jp.naver.line.android", "jp.naver.line.android.activity.selectchat.SelectChatActivity"},
            {"com.twitter.android", "com.twitter.android.composer.ComposerActivity"},
            {"com.google.android.apps.plus", "com.google.android.libraries.social.gateway.GatewayActivity"}
    };

    public static final String EXTRA_VOTE_URL = "vote_url";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_IMG_URL = "image_url";
    public static final String EXTRA_IS_SHARE_APP = "is_share_app";

    @BindView(R.id.share_options)
    FlowLayout shareOptions;
    @BindView(R.id.share_to)
    TextView shareTo;

    private CallbackManager mCallbackManager;
    private String voteURL;
    private boolean isShareApp;

    private Tracker tracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_share);
        ButterKnife.bind(this);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        mCallbackManager = CallbackManager.Factory.create();
        if (getIntent() != null) {
            voteURL = getIntent().getStringExtra(EXTRA_VOTE_URL);
            isShareApp = getIntent().getBooleanExtra(EXTRA_IS_SHARE_APP, false);
            initShareOptions();
        } else {
            finish();
        }
        if (isShareApp) {
            shareTo.setText(R.string.vote_share_app_via);
        } else {
            shareTo.setText(R.string.vote_share_vote_via);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isShareApp) {
            tracker.setScreenName(AnalyzticsTag.SCREEN_ABOUT_SHARE_APP);
        } else {
            tracker.setScreenName(AnalyzticsTag.SCREEN_SHARE_VOTE);
        }
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void initShareOptions() {
        PackageManager pm = getPackageManager();
        for (int i = 0; i < APPS.length; i++) {
            ComponentName componentName = new ComponentName(APPS[i][0], APPS[i][1]);
            try {
                ActivityInfo info = pm.getActivityInfo(componentName, PackageManager.GET_META_DATA);
                View view = getLayoutInflater().inflate(R.layout.btn_share, null);
                ImageView imageView = (ImageView) view.findViewById(R.id.app_share_icon);
                imageView.setImageDrawable(info.loadIcon(pm));
                TextView labelTextView = (TextView) view.findViewById(R.id.app_label);
                labelTextView.setText(info.loadLabel(pm));
                view.setTag(componentName);
                view.setOnClickListener(this);
                shareOptions.addView(view);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        //copy link to clipboard
        View copy = getLayoutInflater().inflate(R.layout.btn_share, null);
        ImageView copyImage = (ImageView) copy.findViewById(R.id.app_share_icon);
        copyImage.setImageResource(R.drawable.ic_shortcut_content_copy);
        TextView copyLabel = (TextView) copy.findViewById(R.id.app_label);
        copyLabel.setText(R.string.vote_share_copy_url);
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCopyLinkClicked();
            }
        });
        shareOptions.addView(copy);
        //more
        View more = getLayoutInflater().inflate(R.layout.btn_share, null);
        ImageView moreImg = (ImageView) more.findViewById(R.id.app_share_icon);
        moreImg.setImageResource(R.drawable.ic_navigation_more_horiz);
        TextView moreLabel = (TextView) more.findViewById(R.id.app_label);
        moreLabel.setText(R.string.vote_share_more);
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOtherShareClicked();
            }
        });
        shareOptions.addView(more);
    }

    public void onCopyLinkClicked() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Link Copied", voteURL);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), R.string.vote_share_copied_msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onOtherShareClicked() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        if (isShareApp) {
            sendIntent.putExtra(Intent.EXTRA_TEXT
                    , String.format(getString(R.string.share_funny_vote_app), voteURL));
            startActivity(Intent.createChooser(sendIntent
                    , getResources().getText(R.string.vote_share_app_via)));
        } else {
            sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                    getString(R.string.vote_share_msg), voteURL));
            startActivity(Intent.createChooser(sendIntent
                    , getResources().getText(R.string.vote_share_vote_via)));
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        Object tag = view.getTag();
        if (tag != null && tag instanceof ComponentName) {
            Intent send = new Intent();
            send.setComponent((ComponentName) tag);
            send.setAction(Intent.ACTION_SEND);
            send.setType("text/plain");
            if (isShareApp) {
                send.putExtra(Intent.EXTRA_TEXT
                        , String.format(getString(R.string.share_funny_vote_app), voteURL));
            } else {
                send.putExtra(Intent.EXTRA_TEXT, String.format(
                        getString(R.string.vote_share_msg), voteURL));
            }
            startActivity(send);
            finish();
        }
    }
}
