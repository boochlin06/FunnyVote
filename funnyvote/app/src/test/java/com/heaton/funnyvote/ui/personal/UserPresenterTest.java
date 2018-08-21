package com.heaton.funnyvote.ui.personal;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
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
import static org.mockito.Mockito.verifyZeroInteractions;
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
    private CreateTabFragment createFragment;
    @Mock
    private ParticipateTabFragment partFragment;
    @Mock
    private FavoriteTabFragment favoriteFragment;

    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;
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
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);
        // Then the presenter is set to the userPageView
        //verify(userPageView).setPresenter(presenter);
    }

    @Test
    public void getVotesFromRepositoryAndLoadIntoView() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);

        presenter.setLoginUser(user);
        presenter.setTargetUser(user);
        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);
        presenter.setCreateVoteDataList(voteDataList);
        presenter.setParticipateVoteDataList(voteDataList);
        presenter.setFavoriteVoteDataList(voteDataList);
        presenter.takeView(userPageView);

        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        InOrder inOrder = Mockito.inOrder(userPageView);
        inOrder.verify(userPageView).showLoadingCircle();
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(userPageView).setUpUserView(user);
        verify(userPageView).setUpTabsAdapter(eq(user), eq(user));

        verify(voteDataRepository, times(2)).getCreateVoteList(eq(0), eq(user), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(createFragment).setMaxCount(anyInt());
        verify(createFragment).refreshFragment(anyList());
        verify(createFragment).hideSwipeLoadView();
        verify(userPageView).hideLoadingCircle();

        verify(voteDataRepository, times(2)).getParticipateVoteList(eq(0), eq(user), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(partFragment).setMaxCount(anyInt());
        verify(partFragment).refreshFragment(anyList());
        verify(partFragment).hideSwipeLoadView();
        verify(userPageView, times(2)).hideLoadingCircle();


        verify(voteDataRepository, times(2)).getFavoriteVoteList(eq(0), eq(user), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(partFragment).setMaxCount(anyInt());
        verify(partFragment).refreshFragment(anyList());
        verify(partFragment).hideSwipeLoadView();
        verify(userPageView, times(3)).hideLoadingCircle();

    }


    @Test
    public void pollVoteAndUpdateToView() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);
        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);
        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");

        InOrder inOrder = Mockito.inOrder(userPageView);
        verify(voteDataRepository).pollVote(anyString(), anyString(), anyList(), Matchers.any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(userPageView).showLoadingCircle();

        pollVoteCallbackArgumentCaptor.getValue().onSuccess(voteData);

        inOrder.verify(userPageView).hideLoadingCircle();
        verify(createFragment).refreshFragment(anyList());
        verify(partFragment).refreshFragment(anyList());
        verify(favoriteFragment).refreshFragment(anyList());
    }

    @Test
    public void pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);

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
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(userPageView).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onPasswordInvalid();
        inOrder.verify(userPageView).shakePollPasswordDialog();
        inOrder.verify(userPageView).hideLoadingCircle();
    }

    @Test
    public void pollVoteFailureAndShowPWDialog() {

        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        VoteData voteData = new VoteData();
        voteData.setVoteCode("CODE_123");
        voteData.setIsNeedPassword(false);
        InOrder inOrder = Mockito.inOrder(userPageView);
        presenter.takeView(userPageView);
        presenter.pollVote(voteData, "OPTION_CODE_123", "password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(userPageView).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onFailure();
        inOrder.verify(userPageView).hideLoadingCircle();
    }

    @Test
    public void favoriteVoteAndUpdateToView() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);

        presenter.takeView(userPageView);
        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);

        presenter.setCreateVoteDataList(voteDataList);
        presenter.setParticipateVoteDataList(voteDataList);
        presenter.setFavoriteVoteDataList(voteDataList);
        voteData.setVoteCode("CODE_123");
        presenter.favoriteVote(voteData);
        verify(voteDataRepository).favoriteVote(anyString(), anyBoolean(), any(User.class)
                , favoriteVoteCallbackArgumentCaptor.capture());
        favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
        verify(createFragment).refreshFragment(anyList());
        verify(partFragment).refreshFragment(anyList());
        verify(favoriteFragment).refreshFragment(anyList());

        verify(userPageView).showHintToast(anyInt(), anyLong());
    }

    @Test
    public void clickOnMainBar_IntentToShareDialogTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);
        presenter.IntentToShareDialog(voteData);
        verify(userPageView).showShareDialog(any(VoteData.class));
    }

    @Test
    public void clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);
        presenter.IntentToAuthorDetail(voteData);
        verify(userPageView).showAuthorDetail(any(VoteData.class));
    }

    @Test
    public void clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);
        presenter.IntentToVoteDetail(voteData);
        verify(userPageView).showVoteDetail(any(VoteData.class));
    }

    @Test
    public void clickOnNoVoteItem_IntentToCreateVoteTest() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);
        presenter.IntentToCreateVote();
        verify(userPageView).showCreateVote();
    }

    @Test
    public void refreshAllFragment_refreshSubFragment() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);
        presenter.takeView(userPageView);

        presenter.setCreateFragmentView(createFragment);
        presenter.setParticipateFragmentView(partFragment);
        presenter.setFavoriteFragmentView(favoriteFragment);
        presenter.refreshAllFragment();

        verify(createFragment).refreshFragment(anyList());
        verify(partFragment).refreshFragment(anyList());
        verify(favoriteFragment).refreshFragment(anyList());
    }


    @Test
    public void notToAddOtherFragment() {
        presenter = new UserPresenter(voteDataRepository, userDataRepository);

        presenter.reloadHotList(0);
        presenter.reloadNewList(0);
        verify(userPageView, never()).setUpTabsAdapter(any(User.class), any(User.class));

        presenter.refreshHotList();
        presenter.refreshNewList();
        verify(userPageView, never()).setUpTabsAdapter(any(User.class), any(User.class));
    }
}
