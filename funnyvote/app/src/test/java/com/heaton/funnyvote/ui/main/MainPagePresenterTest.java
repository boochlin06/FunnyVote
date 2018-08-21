package com.heaton.funnyvote.ui.main;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.promotion.PromotionDataSource;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainPagePresenterTest {

    private static User user;
    private static final String voteCode = "CODE_123";
    private static final VoteData voteData = new VoteData();
    private static List<VoteData> voteDataList = new ArrayList<>();

    private MainPagePresenter presenter;

    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private VoteDataRepository voteDataRepository;
    @Mock
    private PromotionRepository promotionRepository;
    @Mock
    private MainPageContract.MainPageView mainPageView;
    @Mock
    private HotTabFragment hotFragment;
    @Mock
    private NewsTabFragment newFragment;

    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<PromotionDataSource.GetPromotionsCallback> getPromotionsCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteListCallback> getVoteListCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteDataCallback> getVoteDataCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.PollVoteCallback> pollVoteCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.FavoriteVoteCallback> favoriteVoteCallbackArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test

        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);

        // Then the presenter is set to the mainPageView
        //verify(mainPageView).setPresenter(presenter);
    }

    @Test
    public void getVotesAndPromotionFromRepositoryAndLoadIntoView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.takeView(mainPageView);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        InOrder inOrder = Mockito.inOrder(mainPageView);
        inOrder.verify(mainPageView).showLoadingCircle();
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(mainPageView).setUpTabsAdapter(eq(user));
        verify(promotionRepository).getPromotionList(eq(user), getPromotionsCallbackArgumentCaptor.capture());
        getPromotionsCallbackArgumentCaptor.getValue().onPromotionsLoaded(new ArrayList<Promotion>());
        verify(mainPageView).setupPromotionAdmob(anyList(), eq(user));

        verify(voteDataRepository).getHotVoteList(eq(0), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(hotFragment).setMaxCount(anyInt());
        verify(hotFragment).refreshFragment(anyList());
        verify(hotFragment).hideSwipeLoadView();
        verify(mainPageView).hideLoadingCircle();

        verify(voteDataRepository).getNewVoteList(eq(0), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(newFragment).setMaxCount(anyInt());
        verify(newFragment).refreshFragment(anyList());
        verify(newFragment).hideSwipeLoadView();
        verify(mainPageView, times(2)).hideLoadingCircle();

    }

    @Test
    public void refreshHotFragmentAndUpdateToView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        presenter.setUser(user);
        presenter.takeView(mainPageView);
        presenter.refreshHotList();
        verify(voteDataRepository).getHotVoteList(eq(0), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(hotFragment).refreshFragment(anyList());
        verify(hotFragment).hideSwipeLoadView();
        verify(mainPageView).hideLoadingCircle();
    }

    @Test
    public void refreshNewFragmentAndUpdateToView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setUser(user);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        presenter.takeView(mainPageView);
        presenter.refreshNewList();
        verify(voteDataRepository).getNewVoteList(eq(0), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(newFragment).refreshFragment(anyList());
        verify(newFragment).hideSwipeLoadView();
        verify(mainPageView, times(1)).hideLoadingCircle();
    }

    @Test
    public void refreshNewFragmentFailureUpdateToView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setUser(user);
        presenter.setNewsFragmentView(newFragment);
        presenter.takeView(mainPageView);
        presenter.reloadNewList(0);
        verify(voteDataRepository).getNewVoteList(anyInt(), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListNotAvailable();
        verify(mainPageView).showHintToast(anyInt(), anyInt());
        verify(mainPageView).hideLoadingCircle();
        verify(newFragment).hideSwipeLoadView();
    }

    @Test
    public void refreshHotFragmentFailureUpdateToView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setUser(user);
        presenter.setHotsFragmentView(hotFragment);
        presenter.takeView(mainPageView);
        presenter.reloadHotList(0);
        verify(voteDataRepository).getHotVoteList(anyInt(), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListNotAvailable();
        verify(mainPageView).showHintToast(anyInt(), anyInt());
        verify(mainPageView).hideLoadingCircle();
        verify(hotFragment).hideSwipeLoadView();
    }

    @Test
    public void pollVoteAndUpdateToView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setNewsFragmentView(newFragment);
        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        presenter.takeView(mainPageView);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(mainPageView);
        verify(voteDataRepository).pollVote(anyString(), anyString(), anyList(), Matchers.any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(mainPageView).showLoadingCircle();

        pollVoteCallbackArgumentCaptor.getValue().onSuccess(voteData);

        inOrder.verify(mainPageView).hideLoadingCircle();
        verify(hotFragment).refreshFragment(anyList());
        verify(newFragment).refreshFragment(anyList());
    }

    @Test
    public void pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);

        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(true);
        presenter.takeView(mainPageView);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(mainPageView);
        inOrder.verify(mainPageView).showPollPasswordDialog(voteData, "OPTION_CODE_123");
        when(mainPageView.isPasswordDialogShowing()).thenReturn(true);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(mainPageView).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onPasswordInvalid();
        inOrder.verify(mainPageView).shakePollPasswordDialog();
        inOrder.verify(mainPageView).hideLoadingCircle();
    }

    @Test
    public void pollVoteFailureAndShowPWDialog() {

        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);

        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        InOrder inOrder = Mockito.inOrder(mainPageView);
        presenter.takeView(mainPageView);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(mainPageView).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onFailure();
        inOrder.verify(mainPageView).hideLoadingCircle();
    }

    @Test
    public void favoriteVoteAndUpdateToView() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);

        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        voteData.setVoteCode("CODE_123");
        presenter.takeView(mainPageView);
        presenter.favoriteVote(voteData);
        verify(voteDataRepository).favoriteVote(anyString(), anyBoolean(), any(User.class)
                , favoriteVoteCallbackArgumentCaptor.capture());
        favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
        verify(hotFragment).refreshFragment(anyList());
        verify(newFragment).refreshFragment(anyList());
        verify(mainPageView).showHintToast(anyInt(), anyLong());
    }


    @Test
    public void clickOnMainBar_IntentToShareDialogTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.takeView(mainPageView);
        presenter.IntentToShareDialog(voteData);
        verify(mainPageView).showShareDialog(any(VoteData.class));
    }

    @Test
    public void clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.takeView(mainPageView);
        presenter.IntentToAuthorDetail(voteData);
        verify(mainPageView).showAuthorDetail(any(VoteData.class));
    }

    @Test
    public void clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.takeView(mainPageView);
        presenter.IntentToVoteDetail(voteData);
        verify(mainPageView).showVoteDetail(any(VoteData.class));
    }

    @Test
    public void clickOnNoVoteItem_IntentToCreateVoteTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.takeView(mainPageView);
        presenter.IntentToCreateVote();
        verify(mainPageView).showCreateVote();
    }

    @Test
    public void refreshAllFragment_refreshSubFragment() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.takeView(mainPageView);
        presenter.refreshAllFragment();
        verify(hotFragment).refreshFragment(anyList());
        verify(newFragment).refreshFragment(anyList());
    }

    @Test
    public void refreshPromotion_resetPromotion() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.setUser(user);
        presenter.takeView(mainPageView);
        presenter.resetPromotion();
        verify(promotionRepository).getPromotionList(any(User.class), getPromotionsCallbackArgumentCaptor.capture());
        getPromotionsCallbackArgumentCaptor.getValue().onPromotionsLoaded(new ArrayList<Promotion>());
        verify(mainPageView).setupPromotionAdmob(anyList(), any(User.class));
    }

    @Test
    public void notToAddOtherFragment() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository);
        presenter.reloadCreateList(0);
        presenter.reloadParticipateList(0);
        presenter.reloadFavoriteList(0);
        verify(mainPageView, never()).setUpTabsAdapter(any(User.class), any(User.class));

        presenter.refreshCreateList();
        presenter.refreshFavoriteList();
        presenter.refreshParticipateList();
        verify(mainPageView, never()).setUpTabsAdapter(any(User.class), any(User.class));
    }

}