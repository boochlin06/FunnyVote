package com.heaton.funnyvote.ui.main;

import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.promotion.PromotionDataSource;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;

public class MainPagePresenter implements MainPageContract.Presenter {
    public static String TAG = MainPagePresenter.class.getSimpleName();
    private static final int LIMIT = VoteDataRepository.PAGE_COUNT;
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;
    private PromotionRepository promotionRepository;
    private MainPageContract.MainPageView mainPageView;
    private MainPageContract.TabPageFragment hotsFragment, newsFragment;

    private List<VoteData> hotVoteDataList, newVoteDataList;

    public List<VoteData> getHotVoteDataList() {
        return hotVoteDataList;
    }

    public void setHotVoteDataList(List<VoteData> hotVoteDataList) {
        this.hotVoteDataList = hotVoteDataList;
    }

    public List<VoteData> getNewVoteDataList() {
        return newVoteDataList;
    }

    public void setNewVoteDataList(List<VoteData> newVoteDataList) {
        this.newVoteDataList = newVoteDataList;
    }

    public void setUser(User user) {
        this.user = user;
    }

    private User user;

    private List<Promotion> promotionList;
    //private List<MainPageFragment.PromotionType> promotionTypeList;

    public MainPagePresenter(VoteDataRepository voteDataRepository
            , UserDataRepository userDataRepository, PromotionRepository promotionRepository
            , MainPageContract.MainPageView mainPageView) {
        this.mainPageView = mainPageView;
        this.promotionRepository = promotionRepository;
        this.userDataRepository = userDataRepository;
        this.voteDataRepository = voteDataRepository;
        hotVoteDataList = new ArrayList<>();
        newVoteDataList = new ArrayList<>();
        mainPageView.setPresenter(this);
    }

    @Override
    public void resetPromotion() {
        if (user != null) {
            promotionRepository.getPromotionList(user, new PromotionDataSource.GetPromotionsCallback() {
                @Override
                public void onPromotionsLoaded(List<Promotion> promotionList) {
                    MainPagePresenter.this.promotionList = promotionList;
                    mainPageView.setupPromotionAdmob(promotionList, user);
                    Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size()
                            + ",type list size:");
                }

                @Override
                public void onPromotionsNotAvailable() {

                }
            });
        }
    }

    @Override
    public void setHotsFragmentView(MainPageContract.TabPageFragment hotsFragmentView) {
        this.hotsFragment = hotsFragmentView;
        hotsFragment.setTab(MainPageTabFragment.TAB_HOT);
        hotsFragment.setUpRecycleView(hotVoteDataList);
    }

    @Override
    public void setNewsFragmentView(MainPageContract.TabPageFragment newsFragmentView) {
        this.newsFragment = newsFragmentView;
        newsFragment.setTab(MainPageTabFragment.TAB_NEW);
        newsFragment.setUpRecycleView(newVoteDataList);
    }

    @Override
    public void setCreateFragmentView(MainPageContract.TabPageFragment fragmentView) {

    }

    @Override
    public void setParticipateFragmentView(MainPageContract.TabPageFragment fragmentView) {

    }

    @Override
    public void setFavoriteFragmentView(MainPageContract.TabPageFragment fragmentView) {

    }

    @Override
    public void favoriteVote(final VoteData voteData) {
        Log.d(TAG, "favoriteVote");
        voteDataRepository.favoriteVote(voteData.getVoteCode()
                , voteData.getIsFavorite(), user, new VoteDataSource.FavoriteVoteCallback() {
                    @Override
                    public void onSuccess(boolean isFavorite) {
                        Log.d(TAG, "favoriteVote SUCCESS");
                        voteData.setIsFavorite(isFavorite);
                        updateVoteDataToList(hotVoteDataList, voteData);
                        updateVoteDataToList(newVoteDataList, voteData);
                        hotsFragment.refreshFragment(hotVoteDataList);
                        newsFragment.refreshFragment(newVoteDataList);
                        if (voteData.getIsFavorite()) {
                            mainPageView.showHintToast(R.string.vote_detail_toast_add_favorite, 0);
                        } else {
                            mainPageView.showHintToast(R.string.vote_detail_toast_remove_favorite, 0);
                        }
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG, "favoriteVote onFailure");
                        mainPageView.showHintToast(R.string.toast_network_connect_error_favorite, 0);

                    }
                });
    }

    @Override
    public void IntentToShareDialog(VoteData voteData) {
        mainPageView.showShareDialog(voteData);
    }

    @Override
    public void IntentToCreateVote() {
        mainPageView.showCreateVote();
    }

    @Override
    public void IntentToAuthorDetail(VoteData voteData) {
        mainPageView.showAuthorDetail(voteData);
    }

    @Override
    public void IntentToVoteDetail(VoteData voteData) {
        mainPageView.showVoteDetail(voteData);
    }

    @Override
    public void pollVote(VoteData voteData, String optionCode, String password) {
        if (voteData.getIsNeedPassword() && !mainPageView.isPasswordDialogShowing()) {
            mainPageView.showPollPasswordDialog(voteData, optionCode);
        } else {
            mainPageView.showLoadingCircle();
            List<String> choiceCodeList = new ArrayList<>();
            choiceCodeList.add(optionCode);
            voteDataRepository.pollVote(voteData.getVoteCode(), password, choiceCodeList, user, new VoteDataSource.PollVoteCallback() {
                @Override
                public void onSuccess(VoteData voteData) {
                    mainPageView.hideLoadingCircle();
                    updateVoteDataToList(hotVoteDataList, voteData);
                    updateVoteDataToList(newVoteDataList, voteData);
                    hotsFragment.refreshFragment(hotVoteDataList);
                    newsFragment.refreshFragment(newVoteDataList);
                }

                @Override
                public void onFailure() {
                    mainPageView.showHintToast(R.string.toast_network_connect_error_quick_poll, 0);
                    mainPageView.hidePollPasswordDialog();
                    mainPageView.hideLoadingCircle();
                }

                @Override
                public void onPasswordInvalid() {
                    mainPageView.shakePollPasswordDialog();
                    mainPageView.hideLoadingCircle();
                    mainPageView.showHintToast(R.string.vote_detail_dialog_password_toast_retry, 0);
                }
            });
        }
    }

    @Override
    public void reloadHotList(final int offset) {
        if (user == null) {
            start();
            return;
        }
        voteDataRepository.getHotVoteList(offset, user, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                //hotVoteDataList = voteDataList;
                updateHotList(voteDataList, offset);
                if (hotsFragment != null) {
                    hotsFragment.refreshFragment(hotVoteDataList);
                    hotsFragment.hideSwipeLoadView();
                }
                mainPageView.hideLoadingCircle();
            }

            @Override
            public void onVoteListNotAvailable() {
                mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                mainPageView.hideLoadingCircle();
                if (hotsFragment != null) {
                    hotsFragment.hideSwipeLoadView();
                }
            }
        });
    }

    @Override
    public void reloadNewList(final int offset) {
        if (user == null) {
            start();
            return;
        }
        voteDataRepository.getNewVoteList(offset, user, new VoteDataSource.GetVoteListCallback() {
            @Override
            public void onVoteListLoaded(List<VoteData> voteDataList) {
                //newVoteDataList = voteDataList;
                updateNewList(voteDataList, offset);
                Log.d(TAG, "2NEW LIST offset:" + offset + " , size;" + newVoteDataList.size());
                if (newsFragment != null) {
                    newsFragment.hideSwipeLoadView();
                    newsFragment.refreshFragment(newVoteDataList);
                }
                mainPageView.hideLoadingCircle();
            }

            @Override
            public void onVoteListNotAvailable() {
                mainPageView.hideLoadingCircle();
                if (newsFragment != null) {
                    newsFragment.hideSwipeLoadView();
                }
                mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
            }
        });
    }

    private void updateHotList(List<VoteData> voteDataList, int offset) {
        int pageNumber = offset / LIMIT;
        if (offset == 0) {
            this.hotVoteDataList = voteDataList;
        } else if (offset >= this.hotVoteDataList.size()) {
            this.hotVoteDataList.addAll(voteDataList);
        }
        Log.d(TAG, "hotVoteDataList:" + hotVoteDataList.size() + ",offset :" + offset);
        if (this.hotVoteDataList.size() < LIMIT * (pageNumber + 1)) {
            if (hotsFragment != null) {
                hotsFragment.setMaxCount(this.hotVoteDataList.size());
            }
            if (offset != 0) {
                mainPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0);
            }
        } else {
            if (hotsFragment != null) {
                hotsFragment.setMaxCount(LIMIT * (pageNumber + 2));
            }
        }
    }

    private void updateNewList(List<VoteData> voteDataList, int offset) {
        int pageNumber = offset / LIMIT;
        if (offset == 0) {
            this.newVoteDataList = voteDataList;
        } else if (offset >= this.newVoteDataList.size()) {
            this.newVoteDataList.addAll(voteDataList);
        }
        Log.d(TAG, "newVoteDataList:" + newVoteDataList.size() + ",offset :" + offset);
        if (this.newVoteDataList.size() < LIMIT * (pageNumber + 1)) {
            if (newsFragment != null) {
                newsFragment.setMaxCount(this.newVoteDataList.size());
            }
            if (offset != 0) {
                mainPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0);
            }
        } else {
            if (newsFragment != null) {
                newsFragment.setMaxCount(LIMIT * (pageNumber + 2));
            }
        }
    }

    @Override
    public void refreshNewList() {
        Log.d(TAG, "1NEW LIST size;" + newVoteDataList.size());
        reloadNewList(newVoteDataList.size());
    }

    @Override
    public void refreshHotList() {
        reloadHotList(hotVoteDataList.size());
    }

    @Override
    public void reloadCreateList(int offset) {

    }

    @Override
    public void reloadParticipateList(int offset) {

    }

    @Override
    public void reloadFavoriteList(int offset) {

    }

    @Override
    public void refreshCreateList() {

    }

    @Override
    public void refreshParticipateList() {

    }

    @Override
    public void refreshFavoriteList() {

    }

    @Override
    public void setTargetUser(User targetUser) {

    }

    @Override
    public void refreshAllFragment() {
        if (hotsFragment != null)
            hotsFragment.refreshFragment(hotVoteDataList);
        if (newsFragment != null)
            newsFragment.refreshFragment(newVoteDataList);
    }

    private void updateVoteDataToList(List<VoteData> voteDataList, VoteData updateData) {
        for (int i = 0; i < voteDataList.size(); i++) {
            if (updateData.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                voteDataList.set(i, updateData);
                break;
            }
        }
    }

    @Override
    public void start() {
        mainPageView.showLoadingCircle();
        mainPageView.showIntroductionDialog();
        userDataRepository.getUser(new UserDataSource.GetUserCallback() {
            @Override
            public void onResponse(final User user) {
                MainPagePresenter.this.user = user;
                Log.d(TAG, "getUserCallback user:" + user.getType());
                mainPageView.setUpTabsAdapter(user);
                promotionRepository.getPromotionList(user, new PromotionDataSource.GetPromotionsCallback() {
                    @Override
                    public void onPromotionsLoaded(List<Promotion> promotionList) {
                        MainPagePresenter.this.promotionList = promotionList;
                        mainPageView.setupPromotionAdmob(promotionList, user);
                        Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size());
                    }

                    @Override
                    public void onPromotionsNotAvailable() {

                    }
                });
                reloadHotList(0);
                reloadNewList(0);
            }

            @Override
            public void onFailure() {
                mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                mainPageView.setUpTabsAdapter(user);
                mainPageView.hideLoadingCircle();
                Log.d(TAG, "getUserCallback user failure:" + user);
            }
        }, false);
    }
}
