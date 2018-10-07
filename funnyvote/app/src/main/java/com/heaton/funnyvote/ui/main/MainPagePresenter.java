package com.heaton.funnyvote.ui.main;

import android.support.annotation.NonNull;
import android.util.Log;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.retrofit.PasswordObserver;
import com.heaton.funnyvote.retrofit.VoteListObserver;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class MainPagePresenter implements MainPageContract.Presenter {
    public static String TAG = MainPagePresenter.class.getSimpleName();
    private static final int LIMIT = VoteDataRepository.PAGE_COUNT;
    private VoteDataRepository voteDataRepository;
    private UserDataRepository userDataRepository;
    private PromotionRepository promotionRepository;
    private MainPageContract.MainPageView mainPageView;
    private MainPageContract.TabPageFragment hotsFragment, newsFragment;
    @NonNull
    private final BaseSchedulerProvider schedulerProvider;

    private List<VoteData> hotVoteDataList, newVoteDataList;

    @NonNull
    private CompositeSubscription mSubscriptions;

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
            , MainPageContract.MainPageView mainPageView
            , BaseSchedulerProvider schedulerProvider) {
        this.mainPageView = mainPageView;
        this.promotionRepository = promotionRepository;
        this.userDataRepository = userDataRepository;
        this.voteDataRepository = voteDataRepository;
        hotVoteDataList = new ArrayList<>();
        newVoteDataList = new ArrayList<>();
        mSubscriptions = new CompositeSubscription();
        mainPageView.setPresenter(this);
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public void resetPromotion() {
        if (user != null) {
            mSubscriptions.add(promotionRepository.getPromotionList(user)
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(new Observer<List<Promotion>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            mainPageView.setupPromotionAdmob(new ArrayList<>(), user);
                        }

                        @Override
                        public void onNext(List<Promotion> promotionList) {
                            MainPagePresenter.this.promotionList = promotionList;
                            mainPageView.setupPromotionAdmob(promotionList, user);
                            Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size()
                                    + ",type list size:");
                        }
                    }));
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
    public void favoriteVote(VoteData voteData) {
        Log.d(TAG, "favoriteVote before!:" + voteData.getIsFavorite());
        mSubscriptions.add(voteDataRepository.favoriteVote(voteData.getVoteCode(), voteData.getIsFavorite()
                , user)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "favoriteVote onFailure");
                        mainPageView.showHintToast(R.string.toast_network_connect_error_favorite, 0);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        voteData.setIsFavorite(aBoolean);
                        updateVoteDataToList(hotVoteDataList, voteData);
                        updateVoteDataToList(newVoteDataList, voteData);
                        hotsFragment.refreshFragment(hotVoteDataList);
                        newsFragment.refreshFragment(newVoteDataList);
                        Log.d(TAG, "favoriteVote onNext:" + aBoolean);
                        if (voteData.getIsFavorite()) {
                            mainPageView.showHintToast(R.string.vote_detail_toast_add_favorite, 0);
                        } else {
                            mainPageView.showHintToast(R.string.vote_detail_toast_remove_favorite, 0);
                        }
                    }
                }));
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

            mSubscriptions.add(voteDataRepository
                    .pollVote(voteData.getVoteCode(), password, choiceCodeList, user)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(new PasswordObserver<VoteData>() {

                        @Override
                        public void onSuccess(VoteData voteData) {
                            mainPageView.hideLoadingCircle();
                            updateVoteDataToList(hotVoteDataList, voteData);
                            updateVoteDataToList(newVoteDataList, voteData);
                            hotsFragment.refreshFragment(hotVoteDataList);
                            newsFragment.refreshFragment(newVoteDataList);
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            mainPageView.showHintToast(R.string.toast_network_connect_error_quick_poll, 0);
                            mainPageView.hidePollPasswordDialog();
                            mainPageView.hideLoadingCircle();
                        }

                        @Override
                        public void onPasswordInValid() {
                            mainPageView.shakePollPasswordDialog();
                            mainPageView.hideLoadingCircle();
                            mainPageView.showHintToast(R.string.vote_detail_dialog_password_toast_retry, 0);
                        }
                    })
            );
        }
    }

    @Override
    public void reloadHotList(final int offset) {
        if (user == null) {
            subscribe();
            return;
        }
        mSubscriptions.add(voteDataRepository.getHotVoteList(offset, user)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new VoteListObserver<List<VoteData>>() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        Log.d("test", "rx onVoteListLoaded votelist:" + voteDataList.size());
                        //hotVoteDataList = voteDataList;
                        updateHotList(voteDataList, offset);
                        if (hotsFragment != null) {
                            hotsFragment.refreshFragment(hotVoteDataList);
                            hotsFragment.hideSwipeLoadView();
                        }
                        mainPageView.hideLoadingCircle();
                    }

                    @Override
                    public void onVoteListNotAvailable(Throwable e) {
                        Log.e("test", "rx onVoteListNotAvailable votelist:" + e.getMessage());
                        mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                        mainPageView.hideLoadingCircle();
                        if (hotsFragment != null) {
                            hotsFragment.hideSwipeLoadView();
                        }
                    }
                }));
    }

    @Override
    public void reloadNewList(final int offset) {
        if (user == null) {
            subscribe();
            return;
        }
        mSubscriptions.add(voteDataRepository.getNewVoteList(offset, user)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new VoteListObserver<List<VoteData>>() {
                    @Override
                    public void onVoteListLoaded(List<VoteData> voteDataList) {
                        //newVoteDataList = voteDataList;
                        updateNewList(voteDataList, offset);
                        Log.d(TAG, "NEW LIST offset:" + offset + " , size;" + newVoteDataList.size());
                        if (newsFragment != null) {
                            newsFragment.hideSwipeLoadView();
                            newsFragment.refreshFragment(newVoteDataList);
                        }
                        mainPageView.hideLoadingCircle();
                    }

                    @Override
                    public void onVoteListNotAvailable(Throwable e) {
                        mainPageView.hideLoadingCircle();
                        if (newsFragment != null) {
                            newsFragment.hideSwipeLoadView();
                        }
                        mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                    }
                }));
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
    public void subscribe() {
        mainPageView.showLoadingCircle();
        mainPageView.showIntroductionDialog();
        mSubscriptions.add(userDataRepository.getUser(false)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .doOnNext(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        MainPagePresenter.this.user = user;
                        mainPageView.setUpTabsAdapter(user);
                        System.out.println("load user");
                        reloadNewList(0);
                        reloadHotList(0);
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .flatMap(new Func1<User, Observable<List<Promotion>>>() {
                    @Override
                    public Observable<List<Promotion>> call(User user) {
                        Log.d("test", "get promotion list user:" + user.getUserName());
                        return promotionRepository.getPromotionList(user);
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Observer<List<Promotion>>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0);
                        mainPageView.setUpTabsAdapter(user);
                        mainPageView.hideLoadingCircle();
                        mainPageView.setupPromotionAdmob(new ArrayList<>(), user);
                        Log.d(TAG, "get promotion onError:" + e.getMessage());
                    }

                    @Override
                    public void onNext(List<Promotion> promotionList) {
                        MainPagePresenter.this.promotionList = promotionList;
                        mainPageView.setupPromotionAdmob(promotionList, user);
                        Log.d(TAG, "get promotion onNext:" + promotionList.size());
                    }
                })
        );

    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }
}
