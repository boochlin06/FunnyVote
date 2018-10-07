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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the implementation of {@link VoteDataRepository}
 */
public class VoteDataRepositoryTest {

//    public static final int OFFSET = 0;
//    public static List<VoteData> voteDataList;
//    public static List<Option> optionList;
//    public static VoteData voteData;
//
//    private VoteDataRepository voteDataRepository;
//    @Mock
//    private VoteDataSource voteDataLocalDataSource;
//    @Mock
//    private VoteDataSource voteDataRemoteDataSource;
//
//    private TestSubscriber<List<VoteData>> mTasksTestSubscriber;
//
//    @Mock
//    User user;
//
//    @Before
//    public void setupVoteDataRepository() {
//        MockitoAnnotations.initMocks(this);
//
//        for (long i = 0; i < OFFSET * 2; i++) {
//
//            voteDataList.add(newVoteData(i));
//        }
//        for (long i = 0; i < OFFSET; i++) {
//
//            optionList.add(newOption(i));
//        }
//        voteData = newVoteData(0);
//        voteDataRepository = VoteDataRepository.getInstance(voteDataLocalDataSource, voteDataRemoteDataSource);
//        mTasksTestSubscriber = new TestSubscriber<>();
//    }
//
//    @After
//    public void destroyRepositoryInstance() {
//        voteDataRepository.destroyInstance();
//    }
//
//
//    @Test
//    public void testUserIsNotNull() {
//        assertNotNull(user);
//    }
//
//    @Test
//    public void getHotVoteList_requestFromRemote() {
//        voteDataRepository.getHotVoteList(OFFSET, user);
//        verify(voteDataRemoteDataSource).getHotVoteList(eq(OFFSET), eq(user));
//
//    }
//
//    @Test
//    public void saveVote_saveVoteToLocal() {
//        VoteData voteData = newVoteData(0);
//        voteDataRepository.saveVoteData(voteData);
//
//        verify(voteDataLocalDataSource).saveVoteData(voteData);
//    }
//
//    @Test
//    public void saveHotVoteDataList_saveVoteDataListToLocal() {
//        voteDataRepository.saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT);
//        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT);
//    }
//
//    @Test
//    public void getHotVoteDataList_getFromLocal() {
//        when(voteDataRepository.getHotVoteList(OFFSET, user)).thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getHotVoteList(OFFSET, user);
//        setHotVoteDataListNotAvailable(voteDataRemoteDataSource);
//        setHotVoteDataListAvailable(voteDataLocalDataSource, voteDataList);
//        verify(voteDataLocalDataSource).getHotVoteList(eq(OFFSET), eq(user));
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getHotVoteDataListWithBothDataSourceFailure_firesOnDataUnavailable() {
//        when(voteDataRepository.getHotVoteList(OFFSET, user)).thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getHotVoteList(OFFSET, user);
//        setHotVoteDataListNotAvailable(voteDataRemoteDataSource);
//        setHotVoteDataListNotAvailable(voteDataLocalDataSource);
//        //verify(getVoteListCallback).onVoteListNotAvailable();
//    }
//
//    @Test
//    public void getCreateVoteDataList_getFromRemoteAndSaveToLocal() {
//        when(voteDataRepository.getCreateVoteList(OFFSET, user, null)).thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getCreateVoteList(OFFSET, user, null);
//        verify(voteDataRemoteDataSource).getCreateVoteList(eq(OFFSET), eq(user), any(User.class));
//        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
//        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_CREATE);
//        verify(voteDataLocalDataSource, never()).getCreateVoteList(OFFSET, user, null);
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getParticipateVoteList_getFromRemoteAndSaveToLocal() {
//        when(voteDataRepository.getParticipateVoteList(OFFSET, user, null)).thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getParticipateVoteList(OFFSET, user, null);
//        verify(voteDataRemoteDataSource).getParticipateVoteList(eq(OFFSET), eq(user), any(User.class));
//        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
//        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_PARTICIPATE);
//        verify(voteDataLocalDataSource, never()).getParticipateVoteList(OFFSET, user, null);
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getFavoriteVoteList_getFromRemoteAndSaveToLocal() {
//        when(voteDataRemoteDataSource.getFavoriteVoteList(anyInt(), any(), any()))
//                .thenReturn(Observable.just(voteDataList));
//        when(voteDataLocalDataSource.getFavoriteVoteList(anyInt(), any(), any()))
//                .thenReturn(Observable.just(voteDataList));
//        when(voteDataRepository.getFavoriteVoteList(OFFSET, user, null))
//                .thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getFavoriteVoteList(OFFSET, user, null);
//        verify(voteDataRemoteDataSource).getFavoriteVoteList(eq(OFFSET), eq(user), any(User.class));
//        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
//        //verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_FAVORITE);
//        //verify(voteDataLocalDataSource, never()).getFavoriteVoteList(OFFSET, user, null);
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getSearchVoteList_getFromRemoteAndSaveToLocal() {
//        when(voteDataRepository.getSearchVoteList(anyString(), anyInt(), any()))
//                .thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getSearchVoteList("searchkeyword", OFFSET, user);
//        verify(voteDataRemoteDataSource).getSearchVoteList(anyString(), eq(OFFSET), eq(user));
//        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
//        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, null);
//        verify(voteDataLocalDataSource, never()).getSearchVoteList("searckeyword", OFFSET, user);
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getHotVoteDataList_getFromRemoteAndSaveToLocal() {
//        when(voteDataRepository.getHotVoteList(OFFSET, user)).thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getHotVoteList(OFFSET, user);
//        setHotVoteDataListAvailable(voteDataRemoteDataSource, voteDataList);
//        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT);
//        verify(voteDataLocalDataSource, never()).getHotVoteList(OFFSET, user);
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getNewVoteDataList_getFromRemoteAndSaveToLocal() {
//        when(voteDataRepository.getNewVoteList(OFFSET, user)).thenReturn(rx.Observable.just(voteDataList));
//        voteDataRepository.getNewVoteList(OFFSET, user);
//        verify(voteDataRemoteDataSource).getNewVoteList(eq(OFFSET), eq(user));
//        //getVoteListCallbackArgumentCaptor.getValue().onVoteListLoaded(voteDataList);
//        verify(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_NEW);
//        verify(voteDataLocalDataSource, never()).getNewVoteList(OFFSET, user);
//        //verify(getVoteListCallback).onVoteListLoaded(voteDataList);
//    }
//
//    @Test
//    public void getVoteData_getFromRemoteAndSaveToLocal() {
//        when(voteDataRepository.getVoteData(anyString(), any())).thenReturn(rx.Observable.just(voteData));
//        voteDataRepository.getVoteData(voteData.getVoteCode(), user);
//        setVoteDataAvailable(voteDataRemoteDataSource, voteData);
//        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
//        verify(voteDataLocalDataSource, never()).getVoteData(voteData.getVoteCode(), user);
//
//        //verify(getVoteDataCallback).onVoteDataLoaded(voteData);
//    }
//
//    @Test
//    public void getVoteData_getFromLocal() {
//        when(voteDataRepository.getVoteData(anyString(), any())).thenReturn(rx.Observable.just(voteData));
//        voteDataRepository.getVoteData(voteData.getVoteCode(), user);
//        setVoteDataNotAvailable(voteDataRemoteDataSource, voteData.getVoteCode());
//        setVoteDataAvailable(voteDataLocalDataSource, voteData);
//        verify(voteDataLocalDataSource, never()).saveVoteData(eq(voteData));
//        //verify(getVoteDataCallback).onVoteDataLoaded(voteData);
//    }
//
//    @Test
//    public void getVoteDataWithBothDataSourceFailure_firesOnDataUnavailable() {
//        when(voteDataRepository.getVoteData(anyString(), any())).thenReturn(rx.Observable.just(voteData));
//        voteDataRepository.getVoteData(voteData.getVoteCode(), user);
//        setVoteDataNotAvailable(voteDataRemoteDataSource, voteData.getVoteCode());
//        setVoteDataNotAvailable(voteDataLocalDataSource, voteData.getVoteCode());
//        verify(voteDataLocalDataSource, never()).saveVoteData(eq(voteData));
//        //verify(getVoteDataCallback).onVoteDataNotAvailable();
//    }
//
//    @Test
//    public void getOptions_requestFromLocal() {
//        when(voteDataRepository.getOptions(voteData)).thenReturn(rx.Observable.just(optionList));
//        voteDataRepository.getOptions(voteData);
//        setOptionsAvailable(voteDataLocalDataSource, voteData);
//        verify(voteDataRemoteDataSource, never()).getOptions(voteData);
//        verify(voteDataLocalDataSource).getOptions(voteData);
//        //verify(getVoteOptionsCallback).onVoteOptionsLoaded(optionList);
//    }
//
//    @Test
//    public void saveOptions_saveToLocal() {
//        voteDataRepository.saveOptions(optionList);
//        verify(voteDataRemoteDataSource, never()).saveOptions(eq(optionList));
//    }
//
//    @Test
//    public void addNewOptionToRemoteSuccess_saveToLocal() {
//        when(voteDataRepository.addNewOption(anyString(), anyString(), anyList(), any()))
//                .thenReturn(rx.Observable.just(voteData));
//        List<String> newOption = new ArrayList<>();
//        voteDataRepository.addNewOption(voteData.getVoteCode(), "password", newOption
//                , user);
//        verify(voteDataRemoteDataSource).addNewOption(anyString(), anyString(), anyList()
//                , any(User.class));
//        //addNewOptionCallbackArgumentCaptor.getValue().onSuccess(voteData);
//        //verify(addNewOptionCallback).onSuccess(voteData);
//        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
//    }
//
//    @Test
//    public void pollVoteToRemoteSuccess_saveToLocal() {
//        List<String> pollOptions = new ArrayList<>();
//        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any())).thenReturn(Observable.just(voteData));
//        voteDataRepository.pollVote(voteData.getVoteCode(), "password", pollOptions
//                , user);
//        verify(voteDataRemoteDataSource).pollVote(anyString(), anyString(), anyList()
//                , any(User.class));
//        //pollVoteCallbackArgumentCaptor.getValue().onSuccess(voteData);
//        //verify(pollVoteCallback).onSuccess(eq(voteData));
//        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
//    }
//
//    @Test
//    public void pollVoteToRemoteWithErrorPassword_onPasswordInvalid() {
//        List<String> pollOptions = new ArrayList<>();
//        when(voteDataRepository.pollVote(anyString(), anyString(), anyList(), any())).thenReturn(Observable.just(voteData));
//        voteDataRepository.pollVote(voteData.getVoteCode(), "password", pollOptions
//                , user);
//        verify(voteDataRemoteDataSource).pollVote(anyString(), anyString(), anyList()
//                , any(User.class));
//        //pollVoteCallbackArgumentCaptor.getValue().onPasswordInvalid();
//        //verify(pollVoteCallback).onPasswordInvalid();
//        verify(voteDataLocalDataSource, never()).saveVoteData(eq(voteData));
//    }
//
//    @Test
//    public void favoriteVoteWithBothDataSourceSuccess() {
//        when(voteDataRepository.favoriteVote(anyString(), anyBoolean(), any())).thenReturn(Observable.just(true));
//        voteDataRepository.favoriteVote(voteData.getVoteCode(), voteData.getIsFavorite()
//                , user);
//        verify(voteDataRemoteDataSource).favoriteVote(anyString(), anyBoolean()
//                , any(User.class));
//        //favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
//        verify(voteDataLocalDataSource).favoriteVote(anyString(), anyBoolean()
//                , any(User.class));
//        //favoriteVoteCallbackArgumentCaptor.getValue().onSuccess(voteData.getIsFavorite());
//        //verify(favoriteVoteCallback).onSuccess(anyBoolean());
//    }
//
//    @Test
//    public void createVoteToRemote_saveToLocal() {
//        List<String> newOption = new ArrayList<>();
//        voteDataRepository.createVote(voteData, newOption
//                , null);
//        verify(voteDataRemoteDataSource).createVote(any(VoteData.class), anyList(), any(File.class));
//        //getVoteDataCallbackArgumentCaptor.getValue().onVoteDataLoaded(voteData);
//        //verify(getVoteDataCallback).onVoteDataLoaded(voteData);
//        verify(voteDataLocalDataSource).saveVoteData(eq(voteData));
//    }
//
//    private void setHotVoteDataListNotAvailable(VoteDataSource dataSource) {
//        when(dataSource.getHotVoteList(anyInt(), any())).thenReturn(Observable.error(new Exception("test error")));
//    }
//
//    private void setHotVoteDataListAvailable(VoteDataSource dataSource, List<VoteData> voteDataList) {
//        when(dataSource.getHotVoteList(anyInt(), any())).thenReturn(Observable.just(voteDataList));
//    }
//
//    private void setVoteDataNotAvailable(VoteDataSource dataSource, String voteCode) {
//        when(dataSource.getVoteData(anyString(), any())).thenReturn(Observable.error(new Exception("test error")));
//    }
//
//    private void setVoteDataAvailable(VoteDataSource dataSource, VoteData voteData) {
//        when(dataSource.getVoteData(anyString(), any())).thenReturn(Observable.just(voteData));
//    }
//
//    private void setOptionsNotAvailable(VoteDataSource dataSource, VoteData voteData) {
//        when(dataSource.getOptions(any())).thenReturn(Observable.error(new Exception("test error")));
//    }
//
//    private void setOptionsAvailable(VoteDataSource dataSource, VoteData voteData) {
//        when(dataSource.getOptions(any())).thenReturn(Observable.just(optionList));
//    }
//
//
//    private VoteData newVoteData(long index) {
//        VoteData Data = new VoteData();
//        Data.setVoteCode("CODE_" + index);
//        Data.setTitle("TITLE_" + index);
//        Data.setId(index);
//        Data.setStartTime(System.currentTimeMillis() - 36000000);
//        Data.setEndTime(System.currentTimeMillis() + 36000000);
//        return Data;
//    }
//
//    private Option newOption(long index) {
//        Option Data = new Option();
//        Data.setVoteCode("CODE_" + index);
//        Data.setTitle("TITLE_" + index);
//        Data.setId(index);
//        Data.setCode("OPTION_" + index);
//        return Data;
//    }
}

