package com.heaton.funnyvote.ui.search;

import com.google.common.collect.Lists;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class searchPresenterTest {

    private static User user;
    private static final String voteCode = "CODE_123";
    private static final VoteData voteData = new VoteData();
    private static List<VoteData> voteDataList = new ArrayList<>();

    private SearchPresenter presenter;

    @Mock
    private UserDataRepository userDataRepository;
    @Mock
    private VoteDataRepository voteDataRepository;
    @Mock
    private SearchContract.View view;

    @Captor
    private ArgumentCaptor<UserDataSource.GetUserCallback> getUserCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteListCallback> getVoteListCallbackArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        user = mock(User.class);
        when(user.getUserName()).thenReturn("Heaton");
    }

    @Test
    public void createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = new SearchPresenter(voteDataRepository, userDataRepository
                , view);

        // Then the presenter is set to the userPageView
        verify(view).setPresenter(presenter);
    }

    @Test
    public void getSearchVotesFromRepositoryAndLoadIntoView() {
        presenter = new SearchPresenter(voteDataRepository, userDataRepository, view);
        presenter.start("keyword");
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(voteDataRepository).getSearchVoteList(anyString(), eq(0), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(view).refreshFragment(voteDataList);
        verify(view).setMaxCount(anyInt());
    }

    @Test
    public void getEmptySearchKeywordVotesFromRepositoryAndLoadIntoView() {
        presenter = new SearchPresenter(voteDataRepository, userDataRepository, view);
        presenter.start();
        verify(userDataRepository).getUser(getUserCallbackArgumentCaptor.capture(), eq(false));
        getUserCallbackArgumentCaptor.getValue().onResponse(user);
        verify(voteDataRepository, never()).getSearchVoteList(anyString(), anyInt(), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
    }

    @Test
    public void clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = new SearchPresenter(voteDataRepository, userDataRepository, view);
        presenter.IntentToVoteDetail(voteData);
        verify(view).showVoteDetail(any(VoteData.class));
    }

    @Test
    public void refreshSearchVotesFromRepositoryAndLoadIntoView() {
        presenter = new SearchPresenter(voteDataRepository, userDataRepository, view);
        List<VoteData> fullList = Lists.newArrayList();
        for (int i = 0; i < VoteDataRepository.PAGE_COUNT; i++) {
            fullList.add(voteData);
        }
        presenter.setSearchVoteDataList(fullList);
        presenter.refreshSearchList();
        verify(voteDataRepository).getSearchVoteList(anyString(), eq(20), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(fullList);
        verify(view).refreshFragment(fullList);
        verify(view).setMaxCount(anyInt());
    }
}
