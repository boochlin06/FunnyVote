package com.android.heaton.funnyvote.ui.createvote;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.heaton.funnyvote.FileUtils;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.data.VoteData.VoteDataManager;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.android.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity;
import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 16/1/10.
 */
public class CreateVoteActivity extends AppCompatActivity {

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
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.imgPick)
    ImageView imgPick;

    public static String TAG = "CreateVoteActivity";
    private CreateVoteTabSettingFragment settingFragment;
    private CreateVoteTabOptionFragment optionFragment;
    private long newOptionIdAuto = 2;
    private Uri cropImageUri;
    private VoteData localVoteSetting;
    private VoteDataManager voteDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cteate_vote);
        ButterKnife.bind(this);
        mainToolbar = (Toolbar) findViewById(R.id.main_toolbar);

        mainToolbar.setTitle(getString(R.string.create_vote_toolbar_title));
        mainToolbar.setTitleTextColor(Color.WHITE);
        mainToolbar.setElevation(10);

        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getResources().getColor(R.color.md_amber_50));

        mainToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mainToolbar);

        vpSubArea.setAdapter(new TabsAdapter(getSupportFragmentManager()));
        tabLayoutCreateVote.setupWithViewPager(vpSubArea);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        voteDataManager = VoteDataManager.getInstance(getApplicationContext());
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
                    }
                    return optionFragment;
                case 1:
                    if (settingFragment == null) {
                        settingFragment = CreateVoteTabSettingFragment.newTabFragment();
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
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
            submitCreateVote();
            return true;
        } else if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.imgMain, R.id.imgPick})
    public void onSelectImageClick(View view) {
        imgMain.setVisibility(View.VISIBLE);
        imgPick.setVisibility(View.GONE);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.BELOW, imgMain.getId());
        p.addRule(RelativeLayout.ABOVE, tabLayoutCreateVote.getId());
        vpSubArea.setLayoutParams(p);
        CropImage.startPickImageActivity(this);
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
                Glide.with(this).load(resultUri).into(imgMain);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
                .setMaxCropResultSize(imgMain.getWidth() * 2, (int) Util.convertDpToPixel(150 * 2, this))
                .setMinCropResultSize(imgMain.getWidth(), (int) Util.convertDpToPixel(150, this))
                .start(this);
    }

    private void submitCreateVote() {
        showLoadingCircle(getString(R.string.vote_detail_circle_updating));
        localVoteSetting = settingFragment.getVoteSettings();
        List<Option> optionList = optionFragment.getOptionList();
        List<String> optionTitles = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int errorNumber = 0;
        int optionCount = optionList.size();
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getTitle() == null || optionList.get(i).getTitle().length() == 0) {
                errorNumber++;
                sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_fill_all) + "\n");
                break;
            }
            optionTitles.add(optionList.get(i).getTitle());
        }
        if (edtTitle.getText().length() == 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_title_empty) + "\n");
        } else {
            localVoteSetting.setTitle(edtTitle.getText().toString());
        }
        if (localVoteSetting.getAuthorName() == null || localVoteSetting.getAuthorName().isEmpty()) {
            localVoteSetting.setAuthorName(getString(R.string.create_vote_tab_settings_anonymous));
        }
        if (localVoteSetting.getAuthorCode() == null || TextUtils.isEmpty(localVoteSetting.getAuthorCode())) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_error_user_code) + "\n");
        }
        if (localVoteSetting.getMaxOption() == 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_option_0) + "\n");
        }
        if (localVoteSetting.getMinOption() == 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_min_option_0) + "\n");
        }
        if (localVoteSetting.getMaxOption() < localVoteSetting.getMinOption()) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_min) + "\n");
        }
        if (localVoteSetting.getMaxOption() > optionCount) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_max_smaller_than_total) + "\n");
        }
        if (localVoteSetting.getEndTime() < System.currentTimeMillis()) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_endtime_more_than_now) + "\n");
        }
        if (localVoteSetting.getIsNeedPassword() && localVoteSetting.password.length() <= 0) {
            errorNumber++;
            sb.append(errorNumber + ". " + getString(R.string.create_vote_error_hint_password_empty) + "\n");
        }
        if (errorNumber == 0) {
            File file = cropImageUri == null ? null : FileUtils.getFile(this, cropImageUri);
            voteDataManager.createVote(localVoteSetting, optionTitles, file);
        } else {
            hideLoadingCircle();
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.create_vote_dialog_error_title);
            builder.setMessage(sb.toString());
            builder.setPositiveButton(R.string.create_vote_dialog_error_done, null);
            builder.show();
        }

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionControl(EventBusController.OptionControlEvent event) {
        long id = event.Id;
        if (event.message.equals(EventBusController.OptionControlEvent.OPTION_ADD)) {
            Option option = new Option();
            option.setCount(0);
            option.setId(newOptionIdAuto++);
            optionFragment.getOptionList().add(option);
            optionFragment.notifyOptionChange();
            // refresh option
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_REMOVE)) {
            if (optionFragment.getOptionList().size() <= 2) {
                Toast.makeText(getApplicationContext(), getString(R.string.create_vote_toast_less_than_2_option)
                        , Toast.LENGTH_LONG).show();
                return;
            }
            int removePosition = -1;
            for (int i = 0; i < optionFragment.getOptionList().size(); i++) {
                if (optionFragment.getOptionList().get(i).getId() == id) {
                    removePosition = i;
                    break;
                }
            }
            if (removePosition >= 0) {
                optionFragment.getOptionList().remove(removePosition);
                optionFragment.notifyOptionChange();
            }
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_INPUT_TEXT)) {
            int targetPosition = -1;
            for (int i = 0; i < optionFragment.getOptionList().size(); i++) {
                if (optionFragment.getOptionList().get(i).getId() == id) {
                    targetPosition = i;
                    break;
                }
            }
            if (targetPosition >= 0) {
                optionFragment.getOptionList().get(targetPosition).setTitle(event.inputText);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteService(final EventBusController.RemoteServiceEvent event) {
        if (event.message.equals(EventBusController.RemoteServiceEvent.CREATE_VOTE)) {
            if (event.success) {
                this.localVoteSetting = event.voteData;
                Toast.makeText(getApplicationContext(), R.string.create_vote_create_successful, Toast.LENGTH_LONG).show();
                VHVoteWallItem.startActivityToVoteDetail(getApplicationContext(), event.voteData.getVoteCode());
                circleLoad.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        VoteDetailContentActivity.sendShareIntent(getApplicationContext(), event.voteData);
                    }
                }, 1000);
                hideLoadingCircle();
                finish();
                Log.d(TAG, "create vote success:" + event.voteData.getVoteCode() + " image:" + localVoteSetting.getVoteImage());
            } else {
                Toast.makeText(this, R.string.create_vote_toast_create_fail, Toast.LENGTH_LONG).show();
                Log.d(TAG, "create vote false:");
                hideLoadingCircle();
            }
        }
    }

    private void showLoadingCircle(String content) {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(content);
        circleLoad.spin();
    }

    private void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
    }
}
