package com.android.heaton.funnyvote.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.android.heaton.funnyvote.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import org.apmem.tools.layouts.FlowLayout;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;

/**
 * Created by chiu_mac on 2016/11/10.
 */

public class ShareDialogActivity extends AppCompatActivity implements View.OnClickListener{
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

    @BindView(R.id.share_options)
    FlowLayout shareOptions;

    private CallbackManager mCallbackManager;
    private String mTitle;
    private String mImgURL;
    private String mVoteURL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_dialog);
        ButterKnife.bind(this);
        mCallbackManager = CallbackManager.Factory.create();
        if (getIntent() != null) {
            mTitle = getIntent().getStringExtra(EXTRA_TITLE);
            mImgURL = getIntent().getStringExtra(EXTRA_IMG_URL);
            mVoteURL = getIntent().getStringExtra(EXTRA_VOTE_URL);
            initShareOptions();
        } else {
            finish();
        }
    }

    private void initShareOptions() {
        PackageManager pm = getPackageManager();
        try {
            for (int i = 0; i < APPS.length; i++) {
                ComponentName componentName = new ComponentName(APPS[i][0], APPS[i][1]);
                ActivityInfo info = pm.getActivityInfo(componentName, PackageManager.GET_META_DATA);
                View view = getLayoutInflater().inflate(R.layout.share_btn, null);
                ImageView imageView = (ImageView)view.findViewById(R.id.app_share_icon);
                imageView.setImageDrawable(info.loadIcon(pm));
                TextView labelTextView = (TextView)view.findViewById(R.id.app_label);
                labelTextView.setText(info.loadLabel(pm));
                view.setTag(componentName);
                view.setOnClickListener(this);
                shareOptions.addView(view);
            }
            //copy link to clipboard
            View copy = getLayoutInflater().inflate(R.layout.share_btn, null);
            ImageView copyImage = (ImageView)copy.findViewById(R.id.app_share_icon);
            copyImage.setImageResource(R.drawable.ic_shortcut_content_copy);
            TextView copyLabel = (TextView)copy.findViewById(R.id.app_label);
            copyLabel.setText(R.string.vote_share_copy_url);
            copy.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    onCopyLinkClicked();
                }
            });
            shareOptions.addView(copy);
            //more
            View more = getLayoutInflater().inflate(R.layout.share_btn, null);
            ImageView moreImg = (ImageView)more.findViewById(R.id.app_share_icon);
            moreImg.setImageResource(R.drawable.ic_navigation_more_horiz);
            TextView moreLabel = (TextView)more.findViewById(R.id.app_label);
            moreLabel.setText(R.string.vote_share_more);
            more.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    onOtherShareClicked();
                }
            });
            shareOptions.addView(more);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onCopyLinkClicked() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Link Copied", mVoteURL);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getApplicationContext(), R.string.vote_share_copied_msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onOtherShareClicked() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                getString(R.string.vote_share_msg), mVoteURL));
        startActivity(Intent.createChooser(sendIntent
                , getResources().getText(R.string.vote_share_via)));
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
            send.putExtra(Intent.EXTRA_TEXT, String.format(
                    getString(R.string.vote_share_msg), mVoteURL));
            startActivity(Intent.createChooser(send
                    , getResources().getText(R.string.vote_share_via)));
            finish();
        }
    }
}
