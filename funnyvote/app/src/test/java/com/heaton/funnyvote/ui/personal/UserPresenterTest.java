package com.heaton.funnyvote.ui.personal;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.utils.schedulers.BaseSchedulerProvider;
import com.heaton.funnyvote.utils.schedulers.ImmediateSchedulerProvider;

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

public class UserPresenterTest {

    private static User user;
    private static final String voteCode = "CODE_123";
    private static final VoteData voteData = new VoteData();
    private static List<VoteData> voteDataList = new ArrayList<>();

    private UserPresenter presenter;

    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private VoteDataRepository voteDataRepository;
    @Mock
    private PersonalContract.UserPageView userPageView;
    @Mock
    private MainPageTabFragment createFragment;
    @Mock
    private MainPageTabFragment partFragment;
    @Mock
    private MainPageTabFragment favoriteFragment;

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
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);

        // Then the presenter is set to the userPageView
        verify(userPageView).setPresenter(presenter);
    }

    @Test
    public void getVotesFromRepositoryAndLoadIntoView() {
        when(userDataRepository.getUser(eq(false))).thenReturn(rx.Observable.just(user));
        when(voteDataRepository.getCreateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getParticipateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getFavoriteVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.setLoginUser(user);
        presenter.setTargetUser(user);
        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);
        presenter.setCreateVoteDataList(voteDataList);
        presenter.setParticipateVoteDataList(voteDataList);
        presenter.setFavoriteVoteDataList(voteDataList);
        presenter.subscribe();

        verify(userDataRepository).getUser(eq(false));
        InOrder inOrder = Mockito.inOrder(userPageView);
        inOrder.verify(userPageView).showLoadingCircle();
        verify(userPageView).setUpUserView(user);
        verify(userPageView).setUpTabsAdapter(eq(user), eq(user));

        verify(voteDataRepository, times(2)).getCreateVoteList(eq(0), eq(user), eq(user));
        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(createFragment, times(2)).setMaxCount(anyInt());
        verify(createFragment, times(2)).refreshFragment(anyList());
        verify(createFragment, times(2)).hideSwipeLoadView();
        verify(userPageView, times(6)).hideLoadingCircle();

        verify(voteDataRepository, times(2)).getParticipateVoteList(eq(0), eq(user), eq(user));
        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(partFragment, times(2)).setMaxCount(anyInt());
        verify(partFragment, times(2)).refreshFragment(anyList());
        verify(partFragment, times(2)).hideSwipeLoadView();
        verify(userPageView, times(6)).hideLoadingCircle();


        verify(voteDataRepository, times(2)).getFavoriteVoteList(eq(0), eq(user), eq(user));
        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(partFragment, times(2)).setMaxCount(anyInt());
        verify(partFragment, times(2)).refreshFragment(anyList());
        verify(partFragment, times(2)).hideSwipeLoadView();
        verify(userPageView, times(6)).hideLoadingCircle();

    }


    @Test
    public void pollVoteAndUpdateToView() {
        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any(User.class)))
                .thenReturn(Observable.<VoteData>just(voteData));
        when(voteDataRepository.getCreateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getParticipateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getFavoriteVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.setLoginUser(user);
        presenter.setTargetUser(user);
        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);
        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(userPageView);
        verify(voteDataRepository).pollVote(anyString(), anyString(), anyList(), Matchers.any(User.class));
        inOrder.verify(userPageView).showLoadingCircle();

        inOrder.verify(userPageView).hideLoadingCircle();
        verify(createFragment, times(2)).refreshFragment(anyList());
        verify(partFragment, times(2)).refreshFragment(anyList());
        verify(favoriteFragment, times(2)).refreshFragment(anyList());
    }

    @Test
    public void pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        when(voteDataRepository.getCreateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getParticipateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getFavoriteVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any(User.class)))
                .thenReturn(Observable.error(new HttpException(
                        Response.error(500, ResponseBody.create(MediaType.parse("text/plain")
                                , "error_invalid_password")))));
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);

        presenter.setLoginUser(user);
        presenter.setTargetUser(user);
        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);

        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(true);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(userPageView);
        inOrder.verify(userPageView).showPollPasswordDialog(voteData, "OPTION_CODE_123");
        when(userPageView.isPasswordDialogShowing()).thenReturn(true);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class));
        inOrder.verify(userPageView).showLoadingCircle();
        inOrder.verify(userPageView).shakePollPasswordDialog();
        inOrder.verify(userPageView).hideLoadingCircle();
    }

    @Test
    public void pollVoteFailure() {
        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any(User.class)))
                .thenReturn(Observable.error(new HttpException(
                        Response.error(500, ResponseBody.create(MediaType.parse("text/plain")
                                , "test error")))));
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        InOrder inOrder = Mockito.inOrder(userPageView);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class));
        inOrder.verify(userPageView).showLoadingCircle();
        inOrder.verify(userPageView).hideLoadingCircle();
    }

    @Test
    public void favoriteVoteAndUpdateToView() {
        when(voteDataRepository.favoriteVote(anyString(), anyBoolean(), any()))
                .thenReturn(Observable.just(true));
        when(voteDataRepository.getCreateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getParticipateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getFavoriteVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.setLoginUser(user);
        presenter.setTargetUser(user);

        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);

        presenter.setCreateVoteDataList(voteDataList);
        presenter.setParticipateVoteDataList(voteDataList);
        presenter.setFavoriteVoteDataList(voteDataList);
        voteData.setVoteCode("CODE_123");
        presenter.favoriteVote(voteData);
        verify(voteDataRepository).favoriteVote(anyString(), anyBoolean(), any(User.class));
        verify(createFragment, times(2)).refreshFragment(anyList());
        verify(partFragment, times(2)).refreshFragment(anyList());
        verify(favoriteFragment, times(2)).refreshFragment(anyList());

        verify(userPageView).showHintToast(anyInt(), anyLong());
    }

    @Test
    public void clickOnMainBar_IntentToShareDialogTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.IntentToShareDialog(voteData);
        verify(userPageView).showShareDialog(any(VoteData.class));
    }

    @Test
    public void clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.IntentToAuthorDetail(voteData);
        verify(userPageView).showAuthorDetail(any(VoteData.class));
    }

    @Test
    public void clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.IntentToVoteDetail(voteData);
        verify(userPageView).showVoteDetail(any(VoteData.class));
    }

    @Test
    public void clickOnNoVoteItem_IntentToCreateVoteTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        presenter.IntentToCreateVote();
        verify(userPageView).showCreateVote();
    }

    @Test
    public void refreshAllFragment_refreshSubFragment() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);
        when(voteDataRepository.getCreateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getParticipateVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        when(voteDataRepository.getFavoriteVoteList(eq(0), eq(user), eq(user)))
                .thenReturn(Observable.just(voteDataList));
        presenter.setLoginUser(user);
        presenter.setTargetUser(user);

        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);
        presenter.refreshAllFragment();

        verify(createFragment, times(2)).refreshFragment(anyList());
        verify(partFragment, times(2)).refreshFragment(anyList());
        verify(favoriteFragment, times(2)).refreshFragment(anyList());
    }


    @Test
    public void notToAddOtherFragment() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository
                , userPageView, mSchedulerProvider);

        presenter.reloadHotList(0);
        presenter.reloadNewList(0);
        verify(userPageView, never()).setUpTabsAdapter(any(User.class), any(User.class));

        presenter.refreshHotList();
        presenter.refreshNewList();
        verify(userPageView, never()).setUpTabsAdapter(any(User.class), any(User.class));
    }
}
