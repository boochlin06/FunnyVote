package com.heaton.funnyvote.ui.createvote;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;

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
    public static final long DEFAULT_END_TIME = 30;
    public static final long DEFAULT_END_TIME_MAX = 90;
    private static final String TAG = CreateVoteActivityPresenter.class.getSimpleName();
    private CreateVoteContract.ActivityView activityView;
    private CreateVoteContract.OptionFragmentView optionFragmentView;
    private CreateVoteContract.SettingFragmentView settingFragmentView;


    private List<Option> optionList;


    private VoteData voteSettings;
    private User user;
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;
    @NonNull
    private final BaseSchedulerProvider schedulerProvider;
    @NonNull
    private CompositeSubscription mSubscriptions;

    private long newOptionIdAuto = 2;

    public CreateVoteActivityPresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository
            , CreateVoteContract.ActivityView activityView
            , CreateVoteContract.OptionFragmentView optionFragmentView
            , CreateVoteContract.SettingFragmentView settingFragmentView
            , BaseSchedulerProvider schedulerProvider) {
        this.activityView = activityView;
        this.optionFragmentView = optionFragmentView;
        this.settingFragmentView = settingFragmentView;
        this.optionList = new ArrayList<>();
        this.voteSettings = new VoteData();
        this.voteDataRepository = voteDataRepository;
        this.userDataRepository = userDataRepository;
        this.activityView.setPresenter(this);
        //this.settingFragmentView.setPresenter(this);
        //this.optionFragmentView.setPresenter(this);

        mSubscriptions = new CompositeSubscription();
        user = new User();
        this.schedulerProvider = schedulerProvider;
    }


    public VoteData getVoteSettings() {
        return voteSettings;
    }

    public void setVoteSettings(VoteData voteSettings) {
        this.voteSettings = voteSettings;
    }

    public List<Option> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<Option> optionList) {
        this.optionList = optionList;
    }

    @Override
    public void subscribe() {
        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setId(newOptionIdAuto);
            option.setCount(0);
            getOptionList().add(option);
            newOptionIdAuto++;
        }

        voteSettings = new VoteData();
        getVoteSettings().setMaxOption(1);
        getVoteSettings().setMinOption(1);
        getVoteSettings().setIsUserCanAddOption(false);
        getVoteSettings().setIsCanPreviewResult(false);
        getVoteSettings().setIsNeedPassword(false);
        getVoteSettings().setSecurity(VoteData.SECURITY_PUBLIC);
        getVoteSettings().setEndTime(System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000);

        mSubscriptions.add(userDataRepository.getUser(false)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<User>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(User user) {
                        CreateVoteActivityPresenter.this.user = user;
                        voteSettings.author = user;
                        String name = user.getUserName();
                        String code = user.getUserCode();
                        String icon = user.getUserIcon();
                        getVoteSettings().setAuthorName(name);
                        getVoteSettings().setAuthorCode(code);
                        getVoteSettings().setAuthorIcon(icon);
                    }
                }));
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }


    @Override
    public void submitCreateVote() {
        activityView.showLoadingCircle();
        errorCheckMap = new HashMap<>();
        voteSettings = settingFragmentView.getFinalVoteSettings(getVoteSettings());
        getVoteSettings().setStartTime(System.currentTimeMillis());
        List<String> optionTitles = new ArrayList<>();
        int errorNumber = 0;
        int optionCount = getOptionList().size();
        for (int i = 0; i < getOptionList().size(); i++) {
            if (getOptionList().get(i).getTitle() == null || getOptionList().get(i).getTitle().length() == 0) {
                errorNumber++;
                errorCheckMap.put(ERROR_FILL_ALL_OPTION, true);
                break;
            }
        }
        for (int i = 0; i < getOptionList().size(); i++) {
            if (optionTitles.contains(getOptionList().get(i).getTitle())) {
                errorNumber++;
                errorCheckMap.put(ERROR_OPTION_DUPLICATE, true);
                break;
            } else {
                optionTitles.add(getOptionList().get(i).getTitle());
                Log.d(TAG, "option " + i + " title:" + optionTitles.get(i));
            }
        }
        if (getVoteSettings().getTitle() == null || getVoteSettings().getTitle().isEmpty()) {
            errorNumber++;
            errorCheckMap.put(ERROR_TITLE_EMPTY, true);
        }
        if (getVoteSettings().getAuthorCode() == null || TextUtils.isEmpty(getVoteSettings().getAuthorCode())) {
            errorNumber++;
            errorCheckMap.put(ERROR_USER_CODE_ERROR, true);
        }
        if (getVoteSettings().getMaxOption() == 0) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MAX_0, true);
        }
        if (getVoteSettings().getMinOption() == 0) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MIN_0, true);
        }
        if (getVoteSettings().getMaxOption() < getVoteSettings().getMinOption()) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MAX_SAMLL_THAN_MIN, true);
        }
        if (getVoteSettings().getMaxOption() > optionCount) {
            errorNumber++;
            errorCheckMap.put(ERROR_OPTION_MAX_SMALL_THAN_TOTAL, true);
        }
        if (getVoteSettings().getEndTime() < System.currentTimeMillis()) {
            errorNumber++;
            errorCheckMap.put(ERROR_ENDTIME_MORE_THAN_NOW, true);
        }
        if (getVoteSettings().getIsNeedPassword() && getVoteSettings().password.length() <= 0) {
            errorNumber++;
            errorCheckMap.put(ERROR_PASSWORD_EMPTY, true);
        }
        Log.d(TAG, "ERROR NUMBER:" + errorNumber);
        if (errorNumber == 0) {
            mSubscriptions.add(voteDataRepository.createVote(getVoteSettings(), optionTitles, getVoteSettings().getImageFile())
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(new Observer<VoteData>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            activityView.showHintToast(R.string.create_vote_toast_create_fail);
                            activityView.hideLoadingCircle();
                            Log.d(TAG, "create vote false:");
                        }

                        @Override
                        public void onNext(VoteData voteData) {
                            activityView.showHintToast(R.string.create_vote_create_successful);
                            activityView.hideLoadingCircle();
                            activityView.IntentToVoteDetail(voteData);
                            Log.d(TAG, "create vote success:" + voteData.getVoteCode()
                                    + " image:" + getVoteSettings().getVoteImage());
                        }
                    }));
        } else {
            activityView.hideLoadingCircle();
            activityView.showCreateVoteError(errorCheckMap);
        }
    }

    @Override
    public long addNewOption() {
        Option option = new Option();
        option.setCount(0);
        option.setId(newOptionIdAuto++);
        getOptionList().add(option);
        optionFragmentView.refreshOptions();
        return option.getId();
    }

    @Override
    public void removeOption(long optionId) {
        if (getOptionList().size() <= 2) {
            activityView.showHintToast(R.string.create_vote_toast_less_than_2_option);
            return;
        }
        int removePosition = -1;
        for (int i = 0; i < getOptionList().size(); i++) {
            if (getOptionList().get(i).getId() == optionId) {
                removePosition = i;
                break;
            }
        }
        if (removePosition >= 0) {
            getOptionList().remove(removePosition);
        }
        optionFragmentView.refreshOptions();
    }

    @Override
    public void reviseOption(long optionId, String optionText) {
        int targetPosition = -1;
        for (int i = 0; i < getOptionList().size(); i++) {
            if (getOptionList().get(i).getId() == optionId) {
                targetPosition = i;
                break;
            }
        }
        if (targetPosition >= 0) {
            getOptionList().get(targetPosition).setTitle(optionText);
        }
    }

    @Override
    public void setActivityView(CreateVoteContract.ActivityView view) {
        this.activityView = view;
    }

    @Override
    public void setOptionFragmentView(CreateVoteContract.OptionFragmentView view) {
        this.optionFragmentView = view;
        optionFragmentView.setUpOptionAdapter(getOptionList());
    }

    @Override
    public void setSettingFragmentView(CreateVoteContract.SettingFragmentView view) {
        this.settingFragmentView = view;
        settingFragmentView.setUpVoteSettings(getVoteSettings());
        settingFragmentView.updateUserSetting(user);
    }

    @Override
    public void updateVoteSecurity(String security) {
        getVoteSettings().setSecurity(security);
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
            getVoteSettings().setEndTime(timeInMill);
            settingFragmentView.setUpVoteSettings(getVoteSettings());
        }
    }

    @Override
    public void updateVoteImage(File image) {
        getVoteSettings().setImageFile(image);
    }

    @Override
    public void updateVoteTitle(String title) {
        getVoteSettings().setTitle(title);
    }
}
