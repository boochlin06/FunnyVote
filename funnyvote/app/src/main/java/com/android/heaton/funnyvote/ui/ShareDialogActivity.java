package com.android.heaton.funnyvote.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
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

/**
 * Created by chiu_mac on 2016/11/10.
 */

public class ShareDialogActivity extends AppCompatActivity {
    private static final String TAG = ShareDialogActivity.class.getSimpleName();

    public static final String EXTRA_VOTE_URL = "vote_url";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_IMG_URL = "image_url";

    @BindView(R.id.fb_share)
    ImageView mFBShare;
    @BindView(R.id.share_other)
    ImageView mOtherShare;

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
        } else {
            finish();
        }
    }

    @OnClick(R.id.fb_share)
    public void onFacebookShareClick() {
        ShareDialog fbShare = new ShareDialog(this);
        fbShare.registerCallback(mCallbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                finish();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent content = new ShareLinkContent.Builder()
                    .setContentTitle(mTitle)
                    .setImageUrl(Uri.parse(mImgURL))
                    .setContentUrl(Uri.parse(mImgURL)).build();
            fbShare.show(content);
        } else {
            Log.e(TAG, "Could NOT show facebook share dialog!!");
        }
    }

    @OnClick(R.id.share_other)
    public void onOtherShareClick() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                getString(R.string.vote_share_to_other_app_default_message), "Share"));
        startActivity(Intent.createChooser(sendIntent
                , getResources().getText(R.string.vote_detail_menu_share_to)));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("Request Code:%d, Result Code:%d", requestCode, resultCode));
        mCallbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
