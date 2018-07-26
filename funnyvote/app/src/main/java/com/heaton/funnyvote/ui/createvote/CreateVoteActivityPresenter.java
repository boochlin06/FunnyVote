package com.heaton.funnyvote.ui.createvote;

import android.text.TextUtils;
import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateVoteActivityPresenter implements CreateVoteContract.Presenter {

    public static final String ERROR_FILL_ALL_OPTION = "ERROR_FILL_ALL_OPTION";
    public static final String ERROR_OPTION_DUPLICATE = "ERROR_OPTION_DUPLICATE";
    public static final String ERROR_USER_CODE_ERROR = "ERROR_USER_CODE_ERROR";
    public static final String ERROR_OPTION_MAX_0 = "ERROR_OPTION_MAX_0";
    public static final String ERROR_OPTION_MIN_0 = "ERROR_OPTION_MIN_0";
    public static final String ERROR_OPTION_MAX_SAMLL_THAN_MIN = "ERROR_OPTION_MAX_SAMLL_THAN_MIN";
    public static final String ERROR_OPTION_MAX_SMALL_THAN_TOTAL = "ERROR_OPTION_MAX_SMALL_THAN_TOTAL";
    public static final String ERROR_TITLE_EMPTY = "ERROR_TITLE_EMPTY";
    public static final String ERROR_PASSWORD_EMPTY = "ERROR_PASSWORD_EMPTY";
    public static final String ERROR_ENDTIME_MORE_THAN_NOW = "ERROR_ENDTIME_MORE_THAN_NOW";
    public static final String ERROR_ENDTIME_MORE_THAN_MAX = "ERROR_ENDTIME_MORE_THAN_MAX";
    public Map<String, Boolean> errorCheckMap;
    private static final long DEFAULT_END_TIME = 30;
    private static final long DEFAULT_END_TIME_MAX = 90;
    private static final String TAG = CreateVoteActivityPresenter.class.getSimpleName();
    private CreateVoteContract.ActivityView activityView;
    private CreateVoteContract.OptionFragmentView optionFragmentView;
    private CreateVoteContract.SettingFragmentView settingFragmentView;
    private List<Option> optionList;
    private VoteData voteSettings;
    private User user;
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;

    private long newOptionIdAuto = 2;

    public CreateVoteActivityPresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository
            , CreateVoteContract.ActivityView activityView
            , CreateVoteContract.OptionFragmentView optionFragmentView
            , CreateVoteContract.SettingFragmentView settingFragmentView) {
        this.activityView = activityView;
        this.optionFragmentView = optionFragmentView;
        this.settingFragmentView = settingFragmentView;
        this.optionList = new ArrayList<>();
        this.voteSettings = new VoteData();
        this.voteDataRepository = voteDataRepository;
        this.userDataRepository = userDataRepository;
    }

    public CreateVoteActivityPresenter(CreateVoteContract.ActivityView activityView
            , VoteDataRepository voteDataRepository, UserDataRepository userDataRepository) {
        this(voteDataRepository, userDataRepository, activityView, null, null);
    }

    @Override
    public void start() {
        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setId(newOptionIdAuto);
            option.setCount(0);
            optionList.add(option);
            newOptionIdAuto++;
        }

        voteSettings = new VoteData();
        voteSettings.setMaxOption(1);
        voteSettings.setMinOption(1);
        voteSettings.setIsUserCanAddOption(false);
        voteSettings.setIsCanPreviewResult(false);
        voteSettings.setIsNeedPassword(false);
        voteSettings.setSecurity(VoteData.SECURITY_PUBLIC);
        voteSettings.setEndTime(System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000);
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                CreateVoteActivityPresenter.this.user = user;
                voteSettings.author = user;
                String name = user.getUserName();
                String code = user.getUserCode();
                String icon = user.getUserIcon();
                voteSettings.setAuthorName(name);
                voteSettings.setAuthorCode(code);
                voteSettings.setAuthorIcon(icon);
            }

            @Override
            public void onFailure() {

            }
        }, false);
    }


    @Override
    public void submitCreateVote() {
        activityView.showLoadingCircle();
        errorCheckMap = new HashMap<>();
        voteSettings = settingFragmentView.getFinalVoteSettings(voteSettings);
        voteSettings.setStartTime(System.currentTimeMillis());
        List<String> optionTitles = new ArrayList<>();
        int errorNumber = 0;
        int optionCount = optionList.size();
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getTitle() == null || optionList.get(i).getTitle().length() == 0) {
                errorNumber++;
                errorCheckMap.put(ERROR_FILL_ALL_OPTION, true);
                break;
            }
        }
        for (int i = 0; i < optionList.size(); i++) {
            if (optionTitles.contains(optionList.get(i).getTitle())) {
                errorNumber++;
                errorCheckMap.put(ERROR_OPTION_DUPLICATE, true);
                break;
            } else {
                optionTitles.add(optionList.get(i).getTitle());
                Log.d(TAG, "option " + i + " title:" + optionTitles.get(i));
            }
        }
        if (voteSettings.getTitle() == null || voteSettings.getTitle().isEmpty()) {
            errorNumber++;
            errorCheckMap.put(ERROR_TITLE_EMPTY, true);
        }

        if (voteSettings.getAuthorCode() == null || TextUtils.isEmpty(voteSettings.getAuthorCode())) {
            errorNumber++;
            errorCheckMap.put(ERROR_USER_CODE_ERROR, true);
        }
        if (voteSettings.getMaxOption() == 0) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MAX_0, true);
        }
        if (voteSettings.getMinOption() == 0) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MIN_0, true);
        }
        if (voteSettings.getMaxOption() < voteSettings.getMinOption()) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MAX_SAMLL_THAN_MIN, true);
        }
        if (voteSettings.getMaxOption() > optionCount) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MAX_SMALL_THAN_TOTAL, true);
        }
        if (voteSettings.getEndTime() < System.currentTimeMillis()) {
            errorNumber++;
            errorCheckMap.put(ERROR_ENDTIME_MORE_THAN_NOW, true);
        }
        if (voteSettings.getIsNeedPassword() && voteSettings.password.length() <= 0) {
            errorNumber++;
            errorCheckMap.put(ERROR_PASSWORD_EMPTY, true);
        }
        if (errorNumber == 0) {
            voteDataRepository.createVote(voteSettings, optionTitles, voteSettings.getImageFile()
                    , new VoteDataSource.GetVoteDataCallback() {
                        @Override
                        public void onVoteDataLoaded(VoteData voteData) {
                            activityView.showHintToast(R.string.create_vote_create_successful);
                            activityView.hideLoadingCircle();
                            activityView.IntentToVoteDetail(voteData);
                            Log.d(TAG, "create vote success:" + voteData.getVoteCode()
                                    + " image:" + voteSettings.getVoteImage());
                        }

                        @Override
                        public void onVoteDataNotAvailable() {
                            activityView.showHintToast(R.string.create_vote_toast_create_fail);
                            activityView.hideLoadingCircle();
                            Log.d(TAG, "create vote false:");
                        }
                    });
        } else {
            activityView.hideLoadingCircle();
            activityView.showCreateVoteError(errorCheckMap);
        }
    }

    @Override
    public void addNewOption() {
        Option option = new Option();
        option.setCount(0);
        option.setId(newOptionIdAuto++);
        optionList.add(option);
        optionFragmentView.refreshOptions();
    }

    @Override
    public void removeOption(long optionId) {
        if (optionList.size() <= 2) {
            activityView.showHintToast(R.string.create_vote_toast_less_than_2_option);
            return;
        }
        int removePosition = -1;
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getId() == optionId) {
                removePosition = i;
                break;
            }
        }
        if (removePosition >= 0) {
            optionList.remove(removePosition);
            optionFragmentView.refreshOptions();
        }
    }

    @Override
    public void reviseOption(long optionId, String optionText) {
        int targetPosition = -1;
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getId() == optionId) {
                targetPosition = i;
                break;
            }
        }
        if (targetPosition >= 0) {
            optionList.get(targetPosition).setTitle(optionText);
        }
    }

    @Override
    public void setActivityView(CreateVoteContract.ActivityView view) {
        this.activityView = view;
    }

    @Override
    public void setOptionFragmentView(CreateVoteContract.OptionFragmentView view) {
        this.optionFragmentView = view;
        optionFragmentView.setUpOptionAdapter(optionList);
    }

    @Override
    public void setSettingFragmentView(CreateVoteContract.SettingFragmentView view) {
        this.settingFragmentView = view;
        settingFragmentView.setUpVoteSettings(voteSettings);
        settingFragmentView.updateUserSetting(user);
    }

    @Override
    public void updateVoteSecurity(String security) {
        voteSettings.setSecurity(security);
    }

    @Override
    public void updateVoteEndTime(long timeInMill) {
        if (timeInMill < System.currentTimeMillis()) {
            activityView.showHintToast(R.string.create_vote_toast_endtime_more_than_current);
            return;
        } else if (timeInMill - System.currentTimeMillis()
                > DEFAULT_END_TIME_MAX * 86400 * 1000) {
            activityView.showHintToast(R.string.create_vote_error_hint_endtime_more_than_max, DEFAULT_END_TIME_MAX);
            return;
        } else {
            voteSettings.setEndTime(timeInMill);
            settingFragmentView.setUpVoteSettings(voteSettings);
        }
    }

    @Override
    public void updateVoteImage(File image) {
        voteSettings.setImageFile(image);
    }

    @Override
    public void updateVoteTitle(String title) {
        voteSettings.setTitle(title);
    }
}
