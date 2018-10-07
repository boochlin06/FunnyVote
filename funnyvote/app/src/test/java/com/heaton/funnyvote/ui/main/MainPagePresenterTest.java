package com.heaton.funnyvote.ui.main;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.promotion.PromotionRepository;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;
import com.heaton.funnyvote.utils.schedulers.ImmediateSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import rx.Observable;

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
    private MainPageTabFragment hotFragment;
    @Mock
    private MainPageTabFragment newFragment;

    private BaseSchedulerProvider mSchedulerProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mSchedulerProvider = new ImmediateSchedulerProvider();
        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);

        // Then the presenter is set to the mainPageView
        verify(mainPageView).setPresenter(presenter);
    }

    @Test
    public void getVotesAndPromotionFromRepositoryAndLoadIntoView() {
        when(userDataRepository.getUser(eq(false))).thenReturn(Observable.just(user));
        when(voteDataRepository.getNewVoteList(anyInt(), any())).thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getHotVoteList(anyInt(), any())).thenReturn(Observable.just(voteDataList));
        when(promotionRepository.getPromotionList(eq(user))).thenReturn(Observable.just(new ArrayList<Promotion>()));
        when(voteDataRepository.getVoteData(anyString(), any()))
                .thenReturn(Observable.just(voteData));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        presenter.subscribe();
        verify(userDataRepository).getUser(eq(false));
        InOrder inOrder = Mockito.inOrder(mainPageView);
        inOrder.verify(mainPageView).showLoadingCircle();
        verify(mainPageView).setUpTabsAdapter(eq(user));
        verify(promotionRepository).getPromotionList(eq(user));
        verify(mainPageView).setupPromotionAdmob(anyList(), eq(user));
        verify(voteDataRepository).getHotVoteList(eq(0), eq(user));
        verify(hotFragment).setMaxCount(anyInt());
        verify(hotFragment).refreshFragment(anyList());
        verify(hotFragment).hideSwipeLoadView();
        verify(mainPageView, times(2)).hideLoadingCircle();

        verify(voteDataRepository).getNewVoteList(eq(0), eq(user));
        verify(newFragment).setMaxCount(anyInt());
        verify(newFragment).refreshFragment(anyList());
        verify(newFragment).hideSwipeLoadView();
        verify(mainPageView, times(2)).hideLoadingCircle();

    }

    @Test
    public void refreshHotFragmentAndUpdateToView() {
        when(voteDataRepository.getHotVoteList(anyInt(), any())).thenReturn(Observable.just(voteDataList));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        presenter.setUser(user);
        presenter.refreshHotList();
        verify(voteDataRepository).getHotVoteList(eq(0), eq(user));
        verify(hotFragment).refreshFragment(anyList());
        verify(hotFragment).hideSwipeLoadView();
        verify(mainPageView).hideLoadingCircle();
    }

    @Test
    public void refreshNewFragmentAndUpdateToView() {
        when(voteDataRepository.getNewVoteList(anyInt(), any())).thenReturn(Observable.just(voteDataList));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setUser(user);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        presenter.refreshNewList();
        verify(voteDataRepository).getNewVoteList(eq(0), eq(user));
        verify(newFragment).refreshFragment(anyList());
        verify(newFragment).hideSwipeLoadView();
        verify(mainPageView, times(1)).hideLoadingCircle();
    }

    @Test
    public void refreshNewFragmentFailureUpdateToView() {
        when(voteDataRepository.getNewVoteList(anyInt(), any())).thenReturn(Observable
                .error(new Exception("TEST ERROR")));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setUser(user);
        presenter.setNewsFragmentView(newFragment);
        presenter.reloadNewList(0);
        verify(voteDataRepository).getNewVoteList(anyInt(), any(User.class));
        verify(mainPageView).showHintToast(anyInt(), anyInt());
        verify(mainPageView).hideLoadingCircle();
        verify(newFragment).hideSwipeLoadView();
    }

    @Test
    public void refreshHotFragmentFailureUpdateToView() {
        when(voteDataRepository.getHotVoteList(anyInt(), any())).thenReturn(Observable
                .error(new Exception("TEST ERROR")));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setUser(user);
        presenter.setHotsFragmentView(hotFragment);
        presenter.reloadHotList(0);
        verify(voteDataRepository).getHotVoteList(anyInt(), any(User.class));
        verify(mainPageView).showHintToast(anyInt(), anyInt());
        verify(mainPageView).hideLoadingCircle();
        verify(hotFragment).hideSwipeLoadView();
    }

    @Test
    public void pollVoteAndUpdateToView() {
        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any(User.class)))
                .thenReturn(Observable.<VoteData>just(voteData));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setNewsFragmentView(newFragment);
        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(mainPageView);
        verify(voteDataRepository).pollVote(anyString(), anyString(), anyList(), Matchers.any(User.class));
        inOrder.verify(mainPageView).showLoadingCircle();
        inOrder.verify(mainPageView).hideLoadingCircle();
        verify(hotFragment).refreshFragment(anyList());
        verify(newFragment).refreshFragment(anyList());
    }

    @Test
    public void pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {

        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any(User.class)))
                .thenReturn(Observable.error(new HttpException(
                        Response.error(500, ResponseBody.create(MediaType.parse("text/plain")
                                , "error_invalid_password")))));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);

        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(true);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(mainPageView);
        inOrder.verify(mainPageView).showPollPasswordDialog(voteData, "OPTION_CODE_123");
        when(mainPageView.isPasswordDialogShowing()).thenReturn(true);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class));
        inOrder.verify(mainPageView).showLoadingCircle();
        inOrder.verify(mainPageView).shakePollPasswordDialog();
        inOrder.verify(mainPageView).hideLoadingCircle();
    }

    @Test
    public void pollVoteFailure() {
        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any(User.class)))
                .thenReturn(Observable.error(new HttpException(
                        Response.error(500, ResponseBody.create(MediaType.parse("text/plain")
                                , "test error")))));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);

        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        InOrder inOrder = Mockito.inOrder(mainPageView);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class));
        inOrder.verify(mainPageView).showLoadingCircle();
        inOrder.verify(mainPageView).hideLoadingCircle();
    }

    @Test
    public void favoriteVoteAndUpdateToView() {
        when(voteDataRepository.favoriteVote(anyString(), anyBoolean(), any()))
                .thenReturn(Observable.just(true));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);

        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.setHotVoteDataList(voteDataList);
        presenter.setNewVoteDataList(voteDataList);
        voteData.setVoteCode("CODE_123");
        presenter.favoriteVote(voteData);
        verify(voteDataRepository).favoriteVote(anyString(), anyBoolean(), any(User.class));
        verify(hotFragment).refreshFragment(anyList());
        verify(newFragment).refreshFragment(anyList());
        verify(mainPageView).showHintToast(anyInt(), anyLong());
    }


    @Test
    public void clickOnMainBar_IntentToShareDialogTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.IntentToShareDialog(voteData);
        verify(mainPageView).showShareDialog(any(VoteData.class));
    }

    @Test
    public void clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.IntentToAuthorDetail(voteData);
        verify(mainPageView).showAuthorDetail(any(VoteData.class));
    }

    @Test
    public void clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.IntentToVoteDetail(voteData);
        verify(mainPageView).showVoteDetail(any(VoteData.class));
    }

    @Test
    public void clickOnNoVoteItem_IntentToCreateVoteTest() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.IntentToCreateVote();
        verify(mainPageView).showCreateVote();
    }

    @Test
    public void refreshAllFragment_refreshSubFragment() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setNewsFragmentView(newFragment);
        presenter.setHotsFragmentView(hotFragment);
        presenter.refreshAllFragment();
        verify(hotFragment).refreshFragment(anyList());
        verify(newFragment).refreshFragment(anyList());
    }

    @Test
    public void refreshPromotion_resetPromotion() {
        when(promotionRepository.getPromotionList(any())).thenReturn(Observable.just(new ArrayList<Promotion>()));
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
        presenter.setUser(user);
        presenter.resetPromotion();
        verify(promotionRepository).getPromotionList(any(User.class));
        verify(mainPageView).setupPromotionAdmob(anyList(), any(User.class));
    }

    @Test
    public void notToAddOtherFragment() {
        presenter = new MainPagePresenter(voteDataRepository, userDataRepository
                , promotionRepository, mainPageView, mSchedulerProvider);
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