package com.heaton.funnyvote.ui.votedetail;

import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Strings;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class VoteDetailPresenter implements VoteDetailContract.Presenter {

    private static final String TAG = VoteDetailPresenter.class.getSimpleName();
    private final VoteDataRepository voteDataRepository;
    private final UserDataRepository userDataRepository;
    public String voteId;
    public boolean isUserPreResult = false;
    public int optionType = OptionItemAdapter.OPTION_UNPOLL;
    @Nullable
    private VoteDetailContract.View view;
    private User user;
    private VoteData voteData = new VoteData();
    private List<Option> optionList;
    private List<Option> searchList;
    private List<Long> choiceList;
    private List<String> choiceCodeList;
    private List<String> expandOptionList;
    private boolean isMultiChoice = false;
    private boolean isUserOnAddNewOption = false;
    private boolean isSearchMode = false;
    // all new option id is negative auto increment.
    private long newOptionIdAuto = -1;
    @Inject
    public VoteDetailPresenter(@Nullable String voteId,
                               @Nullable VoteDataRepository voteDataRepository,
                               @Nullable UserDataRepository userDataRepository,
                               @Nullable VoteDetailContract.View view) {
        this.view = view;
        this.voteDataRepository = voteDataRepository;
        this.userDataRepository = userDataRepository;
        this.voteId = voteId;
        this.optionList = new ArrayList<>();
        this.choiceList = new ArrayList<>();
        this.choiceCodeList = new ArrayList<>();
        this.expandOptionList = new ArrayList<>();
        this.searchList = new ArrayList<>();
    }

    public VoteData getVoteData() {
        return voteData;
    }

    @Override
    public void searchOption(String newText) {
        List<Option> searchList = new ArrayList<>();
        if (newText.length() > 0) {
            for (int i = 0; i < optionList.size(); i++) {
                if (optionList.get(i).getTitle().contains(newText)) {
                    searchList.add(optionList.get(i));
                }
            }
            isSearchMode = true;
        } else {
            isSearchMode = false;
        }
        view.updateSearchView(searchList, isSearchMode);
    }

    @Override
    public void favoriteVote() {
        voteDataRepository.favoriteVote(getVoteData().getVoteCode(), !getVoteData().getIsFavorite()
                , user, new VoteDataSource.FavoriteVoteCallback() {
                    @Override
                    public void onSuccess(boolean isFavorite) {
                        getVoteData().setIsFavorite(isFavorite);
                        view.updateFavoriteView(isFavorite);
                        if (getVoteData().getIsFavorite()) {
                            view.showHintToast(R.string.vote_detail_toast_add_favorite);
                        } else {
                            view.showHintToast(R.string.vote_detail_toast_remove_favorite);
                        }
                    }

                    @Override
                    public void onFailure() {
                        view.showHintToast(R.string.toast_network_connect_error);
                    }
                });
    }

    @Override
    public void pollVote(String password) {
        if (choiceCodeList.size() < getVoteData().getMinOption()) {
            view.showMultiChoiceAtLeast(getVoteData().getMinOption());
        } else if (choiceCodeList.size() > getVoteData().getMaxOption()) {
            view.showMultiChoiceOverMaxToast(getVoteData().getMaxOption());
        } else if (isUserOnAddNewOption) {
            view.showHintToast(R.string.vote_detail_toast_fill_new_option);
        } else {
            if (getVoteData().getIsNeedPassword() && !view.isPasswordDialogShowing()) {
                view.showPollPasswordDialog();
            } else {
                view.showLoadingCircle();
                voteDataRepository.pollVote(getVoteData().getVoteCode(), password, choiceCodeList, user, new VoteDataSource.PollVoteCallback() {
                    @Override
                    public void onSuccess(VoteData voteData) {
                        view.hideLoadingCircle();
                        VoteDetailPresenter.this.voteData = voteData;
                        VoteDetailPresenter.this.optionList = getVoteData().getNetOptions();
                        checkCurrentOptionType();
                        view.setUpViews(VoteDetailPresenter.this.getVoteData(), optionType);
                        view.setUpOptionAdapter(VoteDetailPresenter.this.getVoteData(), optionType, optionList);
                        view.setUpSubmit(optionType);
                        view.refreshOptions();
                        view.hidePollPasswordDialog();
                    }

                    @Override
                    public void onFailure() {
                        view.showHintToast(R.string.toast_network_connect_error_quick_poll);
                        view.hidePollPasswordDialog();
                        view.hideLoadingCircle();
                    }

                    @Override
                    public void onPasswordInvalid() {
                        view.shakePollPasswordDialog();
                        view.hideLoadingCircle();
                        view.showHintToast(R.string.vote_detail_dialog_password_toast_retry);
                    }
                });
            }
        }
    }

    @Override
    public void resetOptionChoiceStatus(long optionId, String optionCode) {
        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
            return;
        }
        if (!isMultiChoice) {
            choiceList.clear();
            choiceList.add(optionId);
            choiceCodeList.clear();
            choiceCodeList.add(optionCode);
            view.updateChoiceOptions(choiceList);
        } else {
            if (choiceList.contains(optionId)) {
                choiceList.remove(choiceList
                        .indexOf(optionId));
                choiceCodeList.remove(optionCode);
                view.updateChoiceOptions(choiceList);
            } else {
                if (choiceList.size() < getVoteData().getMaxOption()) {
                    choiceList.add(optionId);
                    choiceCodeList.add(optionCode);
                    view.updateChoiceOptions(choiceList);
                } else {
                    view.showMultiChoiceOverMaxToast(getVoteData().getMaxOption());
                }
            }
        }
    }

    @Override
    public void resetOptionExpandStatus(String optionCode) {
        if (expandOptionList.contains(optionCode)) {
            expandOptionList.remove(expandOptionList
                    .indexOf(optionCode));
        } else {
            expandOptionList.add(optionCode);
        }
        view.updateExpandOptions(expandOptionList);
    }

    @Override
    public void addNewOptionStart() {
        if (isUserOnAddNewOption) {
            view.showHintToast(R.string.vote_detail_toast_confirm_new_option);
        } else {
            isUserOnAddNewOption = true;
            Option option = new Option();
            option.setCount(0);
            option.setId(newOptionIdAuto--);
            option.setCode("add" + newOptionIdAuto);
            optionList.add(option);
            view.refreshOptions();
        }
    }

    @Override
    public void addNewOptionContentRevise(long optionId, String inputText) {
        int targetPosition = -1;
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getId() == optionId) {
                targetPosition = i;
                break;
            }
        }
        if (targetPosition >= 0) {
            optionList.get(targetPosition).setTitle(inputText);
        }
    }

    @Override
    public void addNewOptionCompleted(String password, String newOptionText) {
        if (newOptionText != null && !TextUtils.isEmpty(newOptionText)) {
            if (getVoteData().getIsNeedPassword() && !view.isPasswordDialogShowing()) {
                view.showAddNewOptionPasswordDialog(newOptionText);
            } else {
                view.showLoadingCircle();
                List<String> newOptions = new ArrayList<>();
                newOptions.add(newOptionText);
                voteDataRepository.addNewOption(getVoteData().getVoteCode(), password, newOptions, user
                        , new VoteDataSource.AddNewOptionCallback() {
                            @Override
                            public void onSuccess(VoteData voteData) {
                                Log.e(TAG, "onSuccess");
                                isUserOnAddNewOption = false;
                                view.hideLoadingCircle();
                                VoteDetailPresenter.this.voteData = voteData;
                                VoteDetailPresenter.this.optionList = voteData.getNetOptions();
                                checkCurrentOptionType();
                                view.setUpViews(VoteDetailPresenter.this.voteData, optionType);
                                view.setUpOptionAdapter(VoteDetailPresenter.this.voteData, optionType, optionList);
                                view.setUpSubmit(optionType);
                                view.refreshOptions();
                                view.hideAddNewOptionPasswordDialog();
                            }

                            @Override
                            public void onFailure() {
                                Log.e(TAG, "onFailure");
                                view.showHintToast(R.string.toast_network_connect_error_quick_poll);
                                view.hideAddNewOptionPasswordDialog();
                                view.hideLoadingCircle();
                            }

                            @Override
                            public void onPasswordInvalid() {
                                Log.e(TAG, "onPasswordInvalid");
                                view.shakeAddNewOptionPasswordDialog();
                                view.showHintToast(R.string.vote_detail_dialog_password_toast_retry);
                                view.hideLoadingCircle();
                            }
                        });
            }
        } else {
            view.showHintToast(R.string.vote_detail_toast_fill_new_option);
        }
    }

    @Override
    public void removeOption(long optionId) {
        isUserOnAddNewOption = false;
        int removePosition = -1;
        for (int i = 0; i < optionList.size(); i++) {
            if (optionList.get(i).getId() == optionId) {
                removePosition = i;
                break;
            }
        }
        if (removePosition >= 0) {
            optionList.remove(removePosition);
        }
        view.refreshOptions();
    }

    @Override
    public void IntentToVoteInfo() {
        view.showVoteInfoDialog(getVoteData());
    }

    @Override
    public void IntentToTitleDetail() {
        view.showTitleDetailDialog(getVoteData());
    }

    @Override
    public void IntentToShareDialog() {
        view.showShareDialog(getVoteData());
    }

    @Override
    public void IntentToAuthorDetail() {
        view.showAuthorDetail(getVoteData());
    }

    @Override
    public void changeOptionType() {
        isUserPreResult = !isUserPreResult;
        if (isUserPreResult) {
            optionType = OptionItemAdapter.OPTION_SHOW_RESULT;
            view.showResultOption(optionType);
        } else {
            optionType = OptionItemAdapter.OPTION_UNPOLL;
            view.showUnPollOption(optionType);
        }
        view.setUpSubmit(optionType);
    }

    @Override
    public void sortOptions(int sortType) {
        Comparator<Option> comparator = null;
        switch (sortType) {
            case 0:
                comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option option1, Option option2) {
                        // TODO:Add user add new option case id compare.
                        if (option1.getId() < 0 || option2.getId() < 0) {
                            return ((Long) (Math.abs(option1.getId()) + 100000))
                                    .compareTo(Math.abs(option2.getId()) + 100000);
                        } else {
                            return option1.getId().compareTo(option2.getId());
                        }
                    }
                };
                break;
            case 1:
                comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option option1, Option option2) {
                        return option1.getTitle().compareTo(option2.getTitle());
                    }
                };
                break;
            case 2:
                comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option option1, Option option2) {
                        return option2.getCount().compareTo(option1.getCount());
                    }
                };
                break;
        }
        Collections.sort(getCurrentList(), comparator);
        if (!isSearchMode) {
            Collections.sort(optionList, comparator);
        }
        view.refreshOptions();
    }

    private List<Option> getCurrentList() {
        return isSearchMode ? searchList : optionList;
    }

    @Override
    public void CheckSortOptionType() {
        view.showSortOptionDialog(getVoteData());
    }

    @Override
    public void takeView(VoteDetailContract.View view) {
        this.view = view;
        openVoteData();
    }

    @Override
    public void dropView() {
        this.view = null;
    }

    private void openVoteData() {
        if (Strings.isNullOrEmpty(voteId)) {
            return;
        }
        checkCurrentOptionType();
        view.showLoadingCircle();
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(User user) {
                VoteDetailPresenter.this.user = user;
                view.setUpAdMob(user);
                voteDataRepository.getVoteData(voteId, user, new VoteDataSource.GetVoteDataCallback() {
                    @Override
                    public void onVoteDataLoaded(VoteData voteData) {
                        VoteDetailPresenter.this.voteData = voteData;
                        VoteDetailPresenter.this.optionList = getVoteData().getNetOptions();
                        checkCurrentOptionType();
                        view.setUpViews(getVoteData(), optionType);
                        view.setUpSubmit(optionType);
                        if (optionType == OptionItemAdapter.OPTION_UNPOLL) {
                            view.showCaseView();
                        }
                        view.hideLoadingCircle();
                        voteDataRepository.getOptions(getVoteData(), new VoteDataSource.GetVoteOptionsCallback() {
                            @Override
                            public void onVoteOptionsLoaded(List<Option> optionList) {
                                VoteDetailPresenter.this.optionList = optionList;
                                view.setUpOptionAdapter(VoteDetailPresenter.this.getVoteData(), optionType, optionList);
                                if (VoteDetailPresenter.this.getVoteData().getEndTime() > System.currentTimeMillis() && !VoteDetailPresenter.this.getVoteData().getIsPolled() && VoteDetailPresenter.this.getVoteData().isMultiChoice()) {
                                    view.showMultiChoiceToast(VoteDetailPresenter.this.getVoteData().getMaxOption(), VoteDetailPresenter.this.getVoteData().getMinOption());
                                } else if (VoteDetailPresenter.this.getVoteData().getEndTime() < System.currentTimeMillis()) {
                                    if (VoteDetailPresenter.this.getVoteData().getIsPolled()) {
                                        view.showHintToast(R.string.vote_detail_toast_vote_end_polled);
                                    } else {
                                        view.showHintToast(R.string.vote_detail_toast_vote_end_not_poll);
                                    }
                                }
                            }

                            @Override
                            public void onVoteOptionsNotAvailable() {
                                view.showHintToast(R.string.create_vote_toast_create_fail);
                            }
                        });

                    }

                    @Override
                    public void onVoteDataNotAvailable() {
                        view.hideLoadingCircle();
                        view.showHintToast(R.string.create_vote_toast_create_fail);
                    }
                });
            }

            @Override
            public void onFailure() {
                view.showHintToast(R.string.create_vote_toast_create_fail);
            }
        }, false);
    }

    private void checkCurrentOptionType() {
        if (getVoteData().getEndTime() < System.currentTimeMillis() || getVoteData().getIsPolled() || isUserPreResult) {
            optionType = OptionItemAdapter.OPTION_SHOW_RESULT;
        } else {
            optionType = OptionItemAdapter.OPTION_UNPOLL;
        }
        this.isMultiChoice = getVoteData().isMultiChoice();
    }

}
