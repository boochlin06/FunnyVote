package com.heaton.funnyvote.ui.personal;

import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageContract;
import com.heaton.funnyvote.ui.main.MainPagePresenter;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;

import java.util.ArrayList;
import java.util.List;

public class UserPresenter implements PersonalContract.Presenter {
    public static String TAG = MainPagePresenter.class.getSimpleName();
    private static final int LIMIT = VoteDataRepository.PAGE_COUNT;
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;
    private PersonalContract.UserPageView userPageView;
    private MainPageContract.TabPageFragment createFragment, participateFragment, favoriteFragment;

    private List<VoteData> createVoteDataList;
    private List<VoteData> participateVoteDataList;

    public List<VoteData> getCreateVoteDataList() {
        return createVoteDataList;
    }

    public void setCreateVoteDataList(List<VoteData> createVoteDataList) {
        this.createVoteDataList = createVoteDataList;
    }

    public List<VoteData> getParticipateVoteDataList() {
        return participateVoteDataList;
    }

    public void setParticipateVoteDataList(List<VoteData> participateVoteDataList) {
        this.participateVoteDataList = participateVoteDataList;
    }

    public List<VoteData> getFavoriteVoteDataList() {
        return favoriteVoteDataList;
    }

    public void setFavoriteVoteDataList(List<VoteData> favoriteVoteDataList) {
        this.favoriteVoteDataList = favoriteVoteDataList;
    }

    private List<VoteData> favoriteVoteDataList;

    public User getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(User loginUser) {
        this.loginUser = loginUser;
    }

    public User getTargetUser() {
        return targetUser;
    }

    private User loginUser, targetUser;

    public UserPresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository
            , PersonalContract.UserPageView userPageView) {
        this.userPageView = userPageView;
        this.userDataRepository = userDataRepository;
        this.voteDataRepository = voteDataRepository;
        createVoteDataList = new ArrayList<>();
        participateVoteDataList = new ArrayList<>();
        favoriteVoteDataList = new ArrayList<>();
        this.userPageView.setPresenter(this);
    }

    @Override
    public void resetPromotion() {

    }

    @Override
    public void setHotsFragmentView(MainPageContract.TabPageFragment hotsFragmentView) {

    }

    @Override
    public void setNewsFragmentView(MainPageContract.TabPageFragment newsFragmentView) {

    }

    @Override
    public void setCreateFragmentView(MainPageContract.TabPageFragment fragmentView) {
        this.createFragment = fragmentView;
        reloadCreateList(0);
        createFragment.setTab(MainPageTabFragment.TAB_CREATE);
        createFragment.setUpRecycleView(createVoteDataList);
    }

    @Override
    public void setParticipateFragmentView(MainPageContract.TabPageFragment fragmentView) {
        this.participateFragment = fragmentView;
        reloadParticipateList(0);
        participateFragment.setTab(MainPageTabFragment.TAB_PARTICIPATE);
        participateFragment.setUpRecycleView(participateVoteDataList);
    }

    @Override
    public void setFavoriteFragmentView(MainPageContract.TabPageFragment fragmentView) {
        this.favoriteFragment = fragmentView;
        reloadFavoriteList(0);
        favoriteFragment.setTab(MainPageTabFragment.TAB_FAVORITE);
        favoriteFragment.setUpRecycleView(favoriteVoteDataList);
    }

    @Override
    public void favoriteVote(final VoteData voteData) {
        voteDataRepository.favoriteVote(voteData.getVoteCode()
                , voteData.getIsFavorite(), loginUser, new VoteDataSource.FavoriteVoteCallback() {
                    @Override
                    public void onSuccess(boolean isFavorite) {
                        voteData.setIsFavorite(isFavorite);
                        updateVoteDataToAllList(voteData);
                        refreshAllFragment();
                        if (voteData.getIsFavorite()) {
                            userPageView.showHintToast(R.string.vote_detail_toast_add_favorite, 0);
                        } else {
                            userPageView.showHintToast(R.string.vote_detail_toast_remove_favorite, 0);
                        }
                    }

                    @Override
                    public void onFailure() {
                        userPageView.showHintToast(R.string.toast_network_connect_error_favorite, 0);
                    }
                });
    }


    @Override
    public void IntentToShareDialog(VoteData voteData) {
        userPageView.showShareDialog(voteData);
    }

    @Override
    public void IntentToCreateVote() {
        userPageView.showCreateVote();
    }

    @Override
    public void IntentToAuthorDetail(VoteData voteData) {
        userPageView.showAuthorDetail(voteData);
    }

    @Override
    public void IntentToVoteDetail(VoteData voteData) {
        userPageView.showVoteDetail(voteData);
    }

    @Override
    public void pollVote(VoteData voteData, String optionCode, String password) {
        if (voteData.getIsNeedPassword() && !userPageView.isPasswordDialogShowing()) {
            userPageView.showPollPasswordDialog(voteData, optionCode);
        } else {
            userPageView.showLoadingCircle();
            List<String> choiceCodeList = new ArrayList<>();
            choiceCodeList.add(optionCode);
            voteDataRepository.pollVote(voteData.getVoteCode(), password, choiceCodeList, loginUser, new VoteDataSource.PollVoteCallback() {
                @Override
                public void onSuccess(VoteData voteData) {
                    userPageView.hideLoadingCircle();
                    updateVoteDataToAllList(voteData);
                    refreshAllFragment();
                }

                @Override
                public void onFailure() {
                    userPageView.showHintToast(R.string.toast_network_connect_error_quick_poll, 0);
                    userPageView.hidePollPasswordDialog();
                    userPageView.hideLoadingCircle();
                }

                @Override
                public void onPasswordInvalid() {
                    userPageView.shakePollPasswordDialog();
                    userPageView.hideLoadingCircle();
                    userPageView.showHintToast(R.string.vote_detail_dialog_password_toast_retry, 0);
                }
            });
        }
    }

    @Override
    public void reloadHotList(int offset) {

    }

    @Override
    public void reloadNewList(int offset) {

    }

    @Override
    public void refreshNewList() {

    }

    @Override
    public void refreshHotList() {

    }

    private void updateVoteDataToList(List<VoteData> voteDataList, VoteData updateData) {
        for (int i = 0; i < voteDataList.size(); i++) {
            if (updateData.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                voteDataList.set(i, updateData);
                break;
            }
        }
    }

    private void updateVoteDataToAllList(VoteData updateData) {
        updateVoteDataToList(createVoteDataList, updateData);
        updateVoteDataToList(favoriteVoteDataList, updateData);
        updateVoteDataToList(participateVoteDataList, updateData);
    }

    @Override
    public void refreshAllFragment() {
        if (createFragment != null)
            createFragment.refreshFragment(createVoteDataList);
        if (participateFragment != null)
            participateFragment.refreshFragment(participateVoteDataList);
        if (favoriteFragment != null)
            favoriteFragment.refreshFragment(favoriteVoteDataList);
    }

    @Override
    public void reloadCreateList(final int offset) {
        if (loginUser == null) {
            start();
            return;
        }
        voteDataRepository.getCreateVoteList(offset, loginUser, targetUser, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                updateCreateList(voteDataList, offset);
                createFragment.refreshFragment(createVoteDataList);
                createFragment.hideSwipeLoadView();
                userPageView.hideLoadingCircle();
            }

            @Override
            public void onVoteListNotAvailable() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                userPageView.hideLoadingCircle();
                createFragment.hideSwipeLoadView();
            }
        });
    }

    @Override
    public void reloadParticipateList(final int offset) {
        if (loginUser == null) {
            start();
            return;
        }
        voteDataRepository.getParticipateVoteList(offset, loginUser, targetUser, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                updateParticipateList(voteDataList, offset);
                participateFragment.refreshFragment(participateVoteDataList);
                participateFragment.hideSwipeLoadView();
                userPageView.hideLoadingCircle();
            }

            @Override
            public void onVoteListNotAvailable() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                userPageView.hideLoadingCircle();
                participateFragment.hideSwipeLoadView();
            }
        });
    }

    @Override
    public void reloadFavoriteList(final int offset) {
        if (loginUser == null) {
            start();
            return;
        }
        voteDataRepository.getFavoriteVoteList(offset, loginUser, targetUser, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                updateFavoriteList(voteDataList, offset);
                favoriteFragment.refreshFragment(favoriteVoteDataList);
                favoriteFragment.hideSwipeLoadView();
                userPageView.hideLoadingCircle();
            }

            @Override
            public void onVoteListNotAvailable() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                userPageView.hideLoadingCircle();
                favoriteFragment.hideSwipeLoadView();
            }
        });
    }

    private void updateFavoriteList(List<VoteData> voteDataList, int offset) {
        int pageNumber = offset / LIMIT;
        if (offset == 0) {
            this.favoriteVoteDataList = voteDataList;
        } else if (offset >= this.favoriteVoteDataList.size()) {
            this.favoriteVoteDataList.addAll(voteDataList);
        }
        Log.d(TAG, "favoriteVoteDataList:" + favoriteVoteDataList.size() + ",offset :" + offset);
        if (this.favoriteVoteDataList.size() < LIMIT * (pageNumber + 1)) {
            favoriteFragment.setMaxCount(this.favoriteVoteDataList.size());
            if (offset != 0) {
                userPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0);
            }
        } else {
            favoriteFragment.setMaxCount(LIMIT * (pageNumber + 2));
        }
    }

    private void updateCreateList(List<VoteData> voteDataList, int offset) {
        int pageNumber = offset / LIMIT;
        if (offset == 0) {
            this.createVoteDataList = voteDataList;
        } else if (offset >= this.createVoteDataList.size()) {
            this.createVoteDataList.addAll(voteDataList);
        }
        Log.d(TAG, "createVoteDataList:" + favoriteVoteDataList.size() + ",offset :" + offset);
        if (this.createVoteDataList.size() < LIMIT * (pageNumber + 1)) {
            createFragment.setMaxCount(this.favoriteVoteDataList.size());
            if (offset != 0) {
                userPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0);
            }
        } else {
            createFragment.setMaxCount(LIMIT * (pageNumber + 2));
        }
    }

    private void updateParticipateList(List<VoteData> voteDataList, int offset) {
        int pageNumber = offset / LIMIT;
        if (offset == 0) {
            this.participateVoteDataList = voteDataList;
        } else if (offset >= this.participateVoteDataList.size()) {
            this.participateVoteDataList.addAll(voteDataList);
        }
        Log.d(TAG, "participateVoteDataList:" + participateVoteDataList.size() + ",offset :" + offset);
        if (this.participateVoteDataList.size() < LIMIT * (pageNumber + 1)) {
            participateFragment.setMaxCount(this.participateVoteDataList.size());
            if (offset != 0) {
                userPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0);
            }
        } else {
            participateFragment.setMaxCount(LIMIT * (pageNumber + 2));
        }
    }

    @Override
    public void refreshCreateList() {
        reloadCreateList(createVoteDataList.size());
    }

    @Override
    public void refreshParticipateList() {
        reloadParticipateList(participateVoteDataList.size());
    }

    @Override
    public void refreshFavoriteList() {
        reloadFavoriteList(favoriteVoteDataList.size());
    }

    @Override
    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    @Override
    public void start() {
        userPageView.showLoadingCircle();
        userPageView.showIntroductionDialog();
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(final User user) {
                UserPresenter.this.loginUser = user;
                Log.d(TAG, "getUserCallback loginUser:" + user.getType());
                userPageView.setUpUserView(targetUser == null ? user : targetUser);
                userPageView.setUpTabsAdapter(user, targetUser);
                reloadAllList(0);
            }

            @Override
            public void onFailure() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                userPageView.setUpTabsAdapter(loginUser, targetUser);
                userPageView.hideLoadingCircle();
                Log.d(TAG, "getUserCallback loginUser failure:" + loginUser);
            }
        }, false);
    }

    private void reloadAllList(int offset) {
        if (createFragment != null)
            reloadCreateList(offset);
        if (participateFragment != null)
            reloadParticipateList(offset);
        if (favoriteFragment != null)
            reloadFavoriteList(offset);
    }

}
