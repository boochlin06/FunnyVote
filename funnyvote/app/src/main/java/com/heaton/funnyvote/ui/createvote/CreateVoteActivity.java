package com.heaton.funnyvote.ui.createvote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity;
import com.heaton.funnyvote.utils.FileUtils;
import com.heaton.funnyvote.utils.Util;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.Map;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 16/1/10.
 */
public class CreateVoteActivity extends AppCompatActivity implements CreateVoteContract.ActivityView {

    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.edtTitle)
    EditText edtTitle;
    @BindView(R.id.tabLayoutCreateVote)
    TabLayout tabLayoutCreateVote;
    @BindView(R.id.vpSubArea)
    ViewPager vpSubArea;
    @BindView(R.id.circleLoad)
    CircleProgressView circleLoad;
    Toolbar mainToolbar;

    public static String TAG = CreateVoteActivity.class.getSimpleName();
    private CreateVoteTabSettingFragment settingFragment;
    private CreateVoteTabOptionFragment optionFragment;
    private Uri cropImageUri;
    private Tracker tracker;
    private CreateVoteContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cteate_vote);
        ButterKnife.bind(this);

        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        mainToolbar.setTitle(getString(R.string.create_vote_toolbar_title));
        mainToolbar.setTitleTextColor(Color.WHITE);
        mainToolbar.setElevation(10);

        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(ContextCompat.getColor(this, R.color.md_amber_50));

        mainToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mainToolbar);

        tracker.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_OPTIONS);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
        vpSubArea.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        vpSubArea.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_OPTIONS);
                } else if (position == 1) {
                    tracker.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE_SETTINGS);
                }
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tabLayoutCreateVote.setupWithViewPager(vpSubArea);
        presenter = new CreateVoteActivityPresenter(
                Injection.provideVoteDataRepository(getApplicationContext())
                , Injection.provideUserRepository(getApplicationContext())
                , this
                , optionFragment, settingFragment);
        presenter.start();

    }

    @Override
    public void setPresenter(CreateVoteContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void showHintToast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showHintToast(int res, long arg) {
        Toast.makeText(this, getString(res, new Object[]{arg}), Toast.LENGTH_LONG).show();
    }

    @Override
    public void IntentToVoteDetail(final VoteData voteData) {
        VHVoteWallItem.startActivityToVoteDetail(getApplicationContext(), voteData.getVoteCode());
        circleLoad.postDelayed(new Runnable() {
            @Override
            public void run() {
                VoteDetailContentActivity.sendShareIntent(getApplicationContext(), voteData);
            }
        }, 1000);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_CREATE_VOTE)
                .setAction(AnalyzticsTag.ACTION_CREATE_VOTE)
                .setLabel(voteData.getVoteCode())
                .build());
        finish();
    }

    private class TabsAdapter extends FragmentPagerAdapter {
        public TabsAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    if (optionFragment == null) {
                        optionFragment = CreateVoteTabOptionFragment.newTabFragment();
                        optionFragment.setPresenter(presenter);
                    }
                    return optionFragment;
                case 1:
                    if (settingFragment == null) {
                        settingFragment = CreateVoteTabSettingFragment.newTabFragment();
                        settingFragment.setPresenter(presenter);
                    }
                    return settingFragment;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.create_vote_tab_options);
                case 1:
                    return getString(R.string.create_vote_tab_settings);
            }
            return "";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_CREATE_VOTE);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_vote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_submit) {
            presenter.updateVoteTitle(edtTitle.getText().toString());
            presenter.submitCreateVote();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                cropImageUri = imageUri;
                Log.d(TAG, "onActivityResult PICK_IMAGE_CHOOSER_REQUEST_CODE:" + cropImageUri);
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                Log.d(TAG, "onActivityResult PICK_IMAGE_CHOOSER_REQUEST_CODE no permission:" + imageUri);
                startCropImageActivity(imageUri);

            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Log.d(TAG, "CROP_IMAGE_ACTIVITY_REQUEST_CODE ok:" + resultUri);
                cropImageUri = resultUri;
                if (optionFragment == null) {
                    vpSubArea.setAdapter(new TabsAdapter(getSupportFragmentManager()));
                }
                File file = cropImageUri == null ? null : FileUtils.getFile(this, cropImageUri);
                presenter.updateVoteImage(file);
                optionFragment.setVoteImage(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCropImageActivity(cropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (cropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(cropImageUri);
            } else {
                Toast.makeText(this, R.string.create_vote_toast_image_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setActivityTitle(getString(R.string.create_vote_error_crop_image_title))
                .setMaxCropResultSize((int) Util.convertDpToPixel(320 * 2, this), (int) Util.convertDpToPixel(150 * 2, this))
                .setMinCropResultSize((int) Util.convertDpToPixel(320, this), (int) Util.convertDpToPixel(150, this))
                .start(this);
    }

    @Override
    public void onBackPressed() {
        if (edtTitle.getText().toString().length() > 0) {
            showExitCheckDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void showExitCheckDialog() {
        final AlertDialog.Builder exitDialog = new AlertDialog.Builder(CreateVoteActivity.this);
        exitDialog.setTitle(R.string.create_vote_dialog_exit_title);
        exitDialog.setMessage(R.string.create_vote_dialog_exit_message);
        exitDialog.setNegativeButton(R.string.create_vote_dialog_exit_button_leave
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CreateVoteActivity.super.onBackPressed();
                    }
                });
        exitDialog.setPositiveButton(R.string.create_vote_dialog_exit_button_keep
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        exitDialog.show();
    }

    @Override
    public void showLoadingCircle() {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(getString(R.string.vote_detail_circle_updating));
        circleLoad.spin();
    }

    @Override
    public void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
    }

    @Override
    public void showCreateVoteError(Map<String, Boolean> errorMap) {
        StringBuilder sb = new StringBuilder();
        Log.e(TAG, "ERROR CHECK MAP : " + errorMap);
        int errorNumber = 0;
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_ENDTIME_MORE_THAN_NOW)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_endtime_more_than_now) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_SMALL_THAN_TOTAL)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_total) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_SAMLL_THAN_MIN)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_min) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MIN_0)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_min_option_0) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_MAX_0)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_option_0) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_USER_CODE_ERROR)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_error_user_code) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_TITLE_EMPTY)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_title_empty) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_OPTION_DUPLICATE)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_title_duplicate) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_ENDTIME_MORE_THAN_MAX)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_endtime_more_than_max) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_FILL_ALL_OPTION)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_fill_all) + "\n");
        }
        if (errorMap.containsKey(CreateVoteActivityPresenter.ERROR_PASSWORD_EMPTY)) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_password_empty) + "\n");
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.create_vote_dialog_error_title);
        builder.setMessage(sb.toString());
        builder.setPositiveButton(R.string.create_vote_dialog_error_done, null);
        builder.show();
    }
}
