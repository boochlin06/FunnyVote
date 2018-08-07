package com.heaton.funnyvote.data.voteData;

import com.heaton.funnyvote.data.VoteData.VoteDataRepository;
import com.heaton.funnyvote.data.VoteData.VoteDataSource;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the implementation of {@link VoteDataRepository}
 */
public class VoteDataRepositoryTest {

    public static final int OFFSET = 0;
    public static List<VoteData> voteDataList;
    public static List<Option> optionList;
    public static VoteData voteData;

    private VoteDataRepository voteDataRepository;
    @Mock
    private VoteDataSource voteDataLocalDataSource;
    @Mock
    private VoteDataSource voteDataRemoteDataSource;
    @Mock
    private VoteDataSource.GetVoteListCallback getVoteListCallback;
    @Mock
    private VoteDataSource.GetVoteDataCallback getVoteDataCallback;
    @Mock
    private VoteDataSource.GetVoteOptionsCallback getVoteOptionsCallback;
    @Mock
    private VoteDataSource.AddNewOptionCallback addNewOptionCallback;
    @Mock
    private VoteDataSource.PollVoteCallback pollVoteCallback;
    @Mock
    private VoteDataSource.FavoriteVoteCallback favoriteVoteCallback;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteDataCallback> getVoteDataCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteListCallback> getVoteListCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.GetVoteOptionsCallback> getVoteOptionsCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.AddNewOptionCallback> addNewOptionCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.PollVoteCallback> pollVoteCallbackArgumentCaptor;
    @Captor
    private ArgumentCaptor<VoteDataSource.FavoriteVoteCallback> favoriteVoteCallbackArgumentCaptor;

    @Mock
    User user;

    @Before
    public void setupVoteDataRepository() {
        MockitoAnnotations.initMocks(this);

        for (long i = 0; i < OFFSET * 2; i++) {

            voteDataList.add(newVoteData(i));
        }
        for (long i = 0; i < OFFSET; i++) {

            optionList.add(newOption(i));
        }
        voteData = newVoteData(0);
        voteDataRepository = VoteDataRepository.getInstance(voteDataLocalDataSource, voteDataRemoteDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        voteDataRepository.destroyInstance();
    }


    @Test
    public void testUserIsNotNull() {
        assertNotNull(user);
    }

    @Test
    public void getHotVoteList_requestFromRemote() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback);
        verify(voteDataRemoteDataSource).getHotVoteList(eq(OFFSET), eq(user)
                , any(VoteDataSource.GetVoteListCallback.class));

    }

    @Test
    public void saveVote_saveVoteToLocal() {
        VoteData voteData = newVoteData(0);
        voteDataRepository.saveVoteData(voteData);

        verify(voteDataLocalDataSource).saveVoteData(voteData);
    }

    @Test
    public void saveHotVoteDataList_saveVoteDataListToLocal() {
        voteDataRepository.saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT);
    }

    @Test
    public void getHotVoteDataList_getFromLocal() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback);
        setHotVoteDataListNotAvailable(voteDataRemoteDataSource);
        setHotVoteDataListAvailable(voteDataLocalDataSource, voteDataList);
        verify(voteDataLocalDataSource).getHotVoteList(eq(OFFSET), eq(user)
                , any(VoteDataSource.GetVoteListCallback.class));
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getHotVoteDataListWithBothDataSourceFailure_firesOnDataUnavailable() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback);
        setHotVoteDataListNotAvailable(voteDataRemoteDataSource);
        setHotVoteDataListNotAvailable(voteDataLocalDataSource);
        verify(getVoteListCallback).onVoteListNotAvailable();
    }

    @Test
    public void getCreateVoteDataList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getCreateVoteList(OFFSET, user, null, getVoteListCallback);
        verify(voteDataRemoteDataSource).getCreateVoteList(eq(OFFSET), eq(user), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_CREATE);
        verify(voteDataLocalDataSource, never()).getCreateVoteList(OFFSET, user, null, getVoteListCallback);
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getParticipateVoteList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getParticipateVoteList(OFFSET, user, null, getVoteListCallback);
        verify(voteDataRemoteDataSource).getParticipateVoteList(eq(OFFSET), eq(user), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_PARTICIPATE);
        verify(voteDataLocalDataSource, never()).getParticipateVoteList(OFFSET, user, null, getVoteListCallback);
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getFavoriteVoteList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getFavoriteVoteList(OFFSET, user, null, getVoteListCallback);
        verify(voteDataRemoteDataSource).getFavoriteVoteList(eq(OFFSET), eq(user), any(User.class)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_FAVORITE);
        verify(voteDataLocalDataSource, never()).getFavoriteVoteList(OFFSET, user, null, getVoteListCallback);
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getSearchVoteList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getSearchVoteList("searchkeyword", OFFSET, user, getVoteListCallback);
        verify(voteDataRemoteDataSource).getSearchVoteList(anyString(), eq(OFFSET), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, null);
        verify(voteDataLocalDataSource,never()).getSearchVoteList("searckeyword",OFFSET,user,getVoteListCallback);
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getHotVoteDataList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback);
        setHotVoteDataListAvailable(voteDataRemoteDataSource, voteDataList);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT);
        verify(voteDataLocalDataSource, never()).getHotVoteList(OFFSET, user, getVoteListCallback);
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getNewVoteDataList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getNewVoteList(OFFSET, user, getVoteListCallback);
        verify(voteDataRemoteDataSource).getNewVoteList(eq(OFFSET), eq(user)
                , getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_NEW);
        verify(voteDataLocalDataSource, never()).getNewVoteList(OFFSET, user, getVoteListCallback);
        verify(getVoteListCallback).onVoteListLoaded(voteDataList);
    }

    @Test
    public void getVoteData_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getVoteData(voteData.getVoteCode(), user, getVoteDataCallback);
        setVoteDataAvailable(voteDataRemoteDataSource, voteData);
        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
        verify(voteDataLocalDataSource, never()).getVoteData(voteData.getVoteCode(), user, getVoteDataCallback);

        verify(getVoteDataCallback).onVoteDataLoaded(voteData);
    }

    @Test
    public void getVoteData_getFromLocal() {
        voteDataRepository.getVoteData(voteData.getVoteCode(), user, getVoteDataCallback);
        setVoteDataNotAvailable(voteDataRemoteDataSource, voteData.getVoteCode());
        setVoteDataAvailable(voteDataLocalDataSource, voteData);
        verify(voteDataLocalDataSource, never()).saveVoteData(eq(voteData));
        verify(getVoteDataCallback).onVoteDataLoaded(voteData);
    }

    @Test
    public void getVoteDataWithBothDataSourceFailure_firesOnDataUnavailable() {
        voteDataRepository.getVoteData(voteData.getVoteCode(), user, getVoteDataCallback);
        setVoteDataNotAvailable(voteDataRemoteDataSource, voteData.getVoteCode());
        setVoteDataNotAvailable(voteDataLocalDataSource, voteData.getVoteCode());
        verify(voteDataLocalDataSource, never()).saveVoteData(eq(voteData));
        verify(getVoteDataCallback).onVoteDataNotAvailable();
    }

    @Test
    public void getOptions_requestFromLocal() {
        voteDataRepository.getOptions(voteData, getVoteOptionsCallback);
        setOptionsAvailable(voteDataLocalDataSource, voteData);
        verify(voteDataRemoteDataSource, never()).getOptions(voteData, getVoteOptionsCallback);
        verify(voteDataLocalDataSource).getOptions(voteData, getVoteOptionsCallback);
        verify(getVoteOptionsCallback).onVoteOptionsLoaded(optionList);
    }

    @Test
    public void saveOptions_saveToLocal() {
        voteDataRepository.saveOptions(optionList);
        verify(voteDataRemoteDataSource, never()).saveOptions(eq(optionList));
    }

    @Test
    public void addNewOptionToRemoteSuccess_saveToLocal() {
        List<String> newOption = new ArrayList<>();
        voteDataRepository.addNewOption(voteData.getVoteCode(), "password", newOption
                , user, addNewOptionCallback);
        verify(voteDataRemoteDataSource).addNewOption(anyString(), anyString(), anyList()
                , any(User.class), addNewOptionCallbackArgumentCaptor.capture());
        addNewOptionCallbackArgumentCaptor.getValue().onSuccess(voteData);
        verify(addNewOptionCallback).onSuccess(voteData);
        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
    }

    @Test
    public void pollVoteToRemoteSuccess_saveToLocal() {
        List<String> pollOptions = new ArrayList<>();
        voteDataRepository.pollVote(voteData.getVoteCode(), "password", pollOptions
                , user, pollVoteCallback);
        verify(voteDataRemoteDataSource).pollVote(anyString(), anyString(), anyList()
                , any(User.class), pollVoteCallbackArgumentCaptor.capture());
        pollVoteCallbackArgumentCaptor.getValue().onSuccess(voteData);
        verify(pollVoteCallback).onSuccess(eq(voteData));
        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
    }

    @Test
    public void pollVoteToRemoteWithErrorPassword_onPasswordInvalid() {
        List<String> pollOptions = new ArrayList<>();
        voteDataRepository.pollVote(voteData.getVoteCode(), "password", pollOptions
                , user, pollVoteCallback);
        verify(voteDataRemoteDataSource).pollVote(anyString(), anyString(), anyList()
                , any(User.class), pollVoteCallbackArgumentCaptor.capture());
        pollVoteCallbackArgumentCaptor.getValue().onPasswordInvalid();
        verify(pollVoteCallback).onPasswordInvalid();
        verify(voteDataLocalDataSource, never()).saveVoteData(eq(voteData));
    }

    @Test
    public void favoriteVoteWithBothDataSourceSuccess() {
        voteDataRepository.favoriteVote(voteData.getVoteCode(), voteData.getIsFavorite()
                , user, favoriteVoteCallback);
        verify(voteDataRemoteDataSource).favoriteVote(anyString(), anyBoolean()
                , any(User.class), favoriteVoteCallbackArgumentCaptor.capture());
        favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
        verify(voteDataLocalDataSource).favoriteVote(anyString(), anyBoolean()
                , any(User.class), favoriteVoteCallbackArgumentCaptor.capture());
        favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
        verify(favoriteVoteCallback).onSuccess(anyBoolean());
    }

    @Test
    public void createVoteToRemote_saveToLocal() {
        List<String> newOption = new ArrayList<>();
        voteDataRepository.createVote(voteData, newOption
                , null, getVoteDataCallback);
        verify(voteDataRemoteDataSource).createVote(any(VoteData.class), anyList(), any(File.class)
                , getVoteDataCallbackArgumentCaptor.capture());
        getVoteDataCallbackArgumentCaptor.getValue().onVoteDataLoaded(voteData);
        verify(getVoteDataCallback).onVoteDataLoaded(voteData);
        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
    }

    private void setHotVoteDataListNotAvailable(VoteDataSource dataSource) {
        verify(dataSource).getHotVoteList(eq(OFFSET), eq(user), getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListNotAvailable();
    }

    private void setHotVoteDataListAvailable(VoteDataSource dataSource, List<VoteData> voteDataList) {
        verify(dataSource).getHotVoteList(eq(OFFSET), eq(user), getVoteListCallbackArgumentCaptor.capture());
        getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
    }

    private void setVoteDataNotAvailable(VoteDataSource dataSource, String voteCode) {
        verify(dataSource).getVoteData(eq(voteCode), eq(user), getVoteDataCallbackArgumentCaptor.capture());
        getVoteDataCallbackArgumentCaptor.getValue().onVoteDataNotAvailable();
    }

    private void setVoteDataAvailable(VoteDataSource dataSource, VoteData voteData) {
        verify(dataSource).getVoteData(eq(voteData.getVoteCode()), eq(user), getVoteDataCallbackArgumentCaptor.capture());
        getVoteDataCallbackArgumentCaptor.getValue().onVoteDataLoaded(voteData);
    }

    private void setOptionsNotAvailable(VoteDataSource dataSource, VoteData voteData) {
        verify(dataSource).getOptions(eq(voteData), getVoteOptionsCallbackArgumentCaptor.capture());
        getVoteOptionsCallbackArgumentCaptor.getValue().onVoteOptionsNotAvailable();
    }

    private void setOptionsAvailable(VoteDataSource dataSource, VoteData voteData) {
        verify(dataSource).getOptions(eq(voteData), getVoteOptionsCallbackArgumentCaptor.capture());
        getVoteOptionsCallbackArgumentCaptor.getValue().onVoteOptionsLoaded(optionList);
    }


    private VoteData newVoteData(long index) {
        VoteData Data = new VoteData();
        Data.setVoteCode("CODE_" + index);
        Data.setTitle("TITLE_" + index);
        Data.setId(index);
        Data.setStartTime(System.currentTimeMillis() - 36000000);
        Data.setEndTime(System.currentTimeMillis() + 36000000);
        return Data;
    }

    private Option newOption(long index) {
        Option Data = new Option();
        Data.setVoteCode("CODE_" + index);
        Data.setTitle("TITLE_" + index);
        Data.setId(index);
        Data.setCode("OPTION_" + index);
        return Data;
    }
}

