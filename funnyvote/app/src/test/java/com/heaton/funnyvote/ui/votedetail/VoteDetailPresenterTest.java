package com.heaton.funnyvote.ui.votedetail;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link VoteDetailPresenter}
 */
public class VoteDetailPresenterTest {
    private static User user;
    private static final String voteCode = "CODE_123";
    private static final VoteData voteData = new VoteData();
    private static List<Option> optionList = new ArrayList<>();

    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private VoteDataRepository voteDataRepository;
    @Mock
    private VoteDetailContract.View view;

    private VoteDetailPresenter presenter;
    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteDataCallback> getVoteDataCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteOptionsCallback> getVoteOptionsCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.FavoriteVoteCallback> favoriteVoteCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.PollVoteCallback> pollVoteCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.AddNewOptionCallback> addNewOptionCallbackArgumentCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);

        // Then the presenter is set to the view
        verify(view).setPresenter(presenter);
    }

    @Test
    public void getVoteDataFromRepositoryAndLoadIntoView() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.start();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        InOrder inOrder = Mockito.inOrder(view);
        inOrder.verify(view).showLoadingCircle();
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(view).setUpAdMob(eq(user));
        verify(voteDataRepository).getVoteData(eq(voteCode), eq(user), getVoteDataCallbackArgumentCaptor.capture());
        getVoteDataCallbackArgumentCaptor.getValue().onVoteDataLoaded(voteData);
        inOrder.verify(view).hideLoadingCircle();
        verify(voteDataRepository).getOptions(eq(voteData), getVoteOptionsCallbackArgumentCaptor.capture());
        getVoteOptionsCallbackArgumentCaptor.getValue().onVoteOptionsLoaded(optionList);
        verify(view).setUpOptionAdapter(eq(voteData), any(Integer.class), eq(optionList));
    }

    @Test
    public void searchOptionAndUpdateToView() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.searchOption("test");
        verify(view).updateSearchView(any(List.class), eq(true));
    }

    @Test
    public void favoriteVoteAndUpdateToView() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.favoriteVote();
        verify(voteDataRepository).favoriteVote(anyString(), anyBoolean(), any(User.class)
                , favoriteVoteCallbackArgumentCaptor.capture());
        favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
        verify(view).updateFavoriteView(anyBoolean());
        verify(view).showHintToast(anyInt());
    }

    @Test
    public void pollVoteAndUpdateToView() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.pollVote("password");
        InOrder inOrder = Mockito.inOrder(view);
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(view).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onSuccess(voteData);
        inOrder.verify(view).hideLoadingCircle();
        verify(view).setUpViews(any(VoteData.class), anyInt());
        verify(view).setUpOptionAdapter(any(VoteData.class), anyInt(), anyList());
        verify(view).setUpSubmit(anyInt());
        verify(view).refreshOptions();
        verify(view).hidePollPasswordDialog();
    }

    @Test
    public void pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.getVoteData().setIsNeedPassword(true);
        presenter.pollVote("password");
        InOrder inOrder = Mockito.inOrder(view);
        inOrder.verify(view).showPollPasswordDialog();
        when(view.isPasswordDialogShowing()).thenReturn(true);
        presenter.pollVote("password");
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(view).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onPasswordInvalid();
        inOrder.verify(view).shakePollPasswordDialog();
        inOrder.verify(view).hideLoadingCircle();
    }

    @Test
    public void pollVoteFailureAndShowPWDialog() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.getVoteData().setIsNeedPassword(false);
        presenter.pollVote("password");
        InOrder inOrder = Mockito.inOrder(view);
        verify(voteDataRepository).pollVote(anyString(), eq("password"), anyList(), any(User.class)
                , pollVoteCallbackArgumentCaptor.capture());
        inOrder.verify(view).showLoadingCircle();
        pollVoteCallbackArgumentCaptor.getValue().onFailure();
        inOrder.verify(view).hideLoadingCircle();
    }

    @Test
    public void addNewOptionNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.getVoteData().setIsNeedPassword(true);
        presenter.addNewOptionCompleted("password", "newOptionText");
        InOrder inOrder = Mockito.inOrder(view);
        inOrder.verify(view).showAddNewOptionPasswordDialog(anyString());
        when(view.isPasswordDialogShowing()).thenReturn(true);
        presenter.addNewOptionCompleted("password", "newOptionText");
        verify(voteDataRepository).addNewOption(anyString(), eq("password"), anyList(), any(User.class)
                , addNewOptionCallbackArgumentCaptor.capture());
        inOrder.verify(view).showLoadingCircle();
        addNewOptionCallbackArgumentCaptor.getValue().onPasswordInvalid();
        inOrder.verify(view).shakeAddNewOptionPasswordDialog();
        inOrder.verify(view).hideLoadingCircle();
    }

    @Test
    public void addNewOptionFailureAndShowPWDialog() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.getVoteData().setIsNeedPassword(false);
        presenter.addNewOptionCompleted("password", "newOptionText");
        InOrder inOrder = Mockito.inOrder(view);
        verify(voteDataRepository).addNewOption(anyString(), eq("password"), anyList(), any(User.class)
                , addNewOptionCallbackArgumentCaptor.capture());
        inOrder.verify(view).showLoadingCircle();
        addNewOptionCallbackArgumentCaptor.getValue().onFailure();
        inOrder.verify(view).hideLoadingCircle();
    }

    @Test
    public void clickOnOption_refreshOptionChoiceTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.resetOptionChoiceStatus(1, "OPTION_123");
        verify(view).updateChoiceOptions(anyList());
    }

    @Test
    public void clickOnOption_refreshOptionExpandTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.resetOptionExpandStatus("OPTION_123");
        verify(view).updateExpandOptions(anyList());
    }

    @Test
    public void clickAddNewOption_addNewOptionStartTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.addNewOptionStart();
        verify(view).refreshOptions();
    }

    @Test
    public void edit_addNewOptionContentReviseTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.addNewOptionContentRevise(1, "inputText");

    }

    @Test
    public void clickComplete_addNewOptionCompletedAndUpdateToView() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.addNewOptionCompleted("password", "newOptionText");
        InOrder inOrder = Mockito.inOrder(view);
        verify(voteDataRepository).addNewOption(anyString(), eq("password"), anyList(), any(User.class)
                , addNewOptionCallbackArgumentCaptor.capture());
        inOrder.verify(view).showLoadingCircle();
        addNewOptionCallbackArgumentCaptor.getValue().onSuccess(voteData);
        inOrder.verify(view).hideLoadingCircle();
        verify(view).setUpViews(any(VoteData.class), anyInt());
        verify(view).setUpOptionAdapter(any(VoteData.class), anyInt(), anyList());
        verify(view).setUpSubmit(anyInt());
        verify(view).refreshOptions();
        verify(view).hideAddNewOptionPasswordDialog();
    }

    @Test
    public void clickOnOption_removeOptionTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.removeOption(1);
        verify(view).refreshOptions();
    }

    @Test
    public void clickOnFab_changeOptionTypeTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.isUserPreResult = false;
        presenter.changeOptionType();
        verify(view).showResultOption(anyInt());
        presenter.isUserPreResult = true;
        presenter.changeOptionType();
        verify(view).showUnPollOption(anyInt());
    }

    @Test
    public void clickOnFab_CheckSortOptionTypeTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.CheckSortOptionType();
        verify(view).showSortOptionDialog(any(VoteData.class));
    }

    @Test
    public void clickOnFab_sortOptionsTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.sortOptions(0);
        verify(view).refreshOptions();
    }

    @Test
    public void clickOnToolbar_IntentToVoteInfoTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.IntentToVoteInfo();
        verify(view).showVoteInfoDialog(any(VoteData.class));
    }

    @Test
    public void clickOnTitle_IntentToTitleDetailTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.IntentToTitleDetail();
        verify(view).showTitleDetailDialog(any(VoteData.class));
    }

    @Test
    public void clickOnMainBar_IntentToShareDialogTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.IntentToShareDialog();
        verify(view).showShareDialog(any(VoteData.class));
    }

    @Test
    public void clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = new VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view);
        presenter.IntentToAuthorDetail();
        verify(view).showAuthorDetail(any(VoteData.class));
    }
}
