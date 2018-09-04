package com.heaton.funnyvote.data.voteData

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.VoteData.VoteDataSource.*
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.eq
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import java.io.File
import java.util.*

/**
 * Unit tests for the implementation of [VoteDataRepository]
 */
class VoteDataRepositoryTest {

    private lateinit var voteDataRepository: VoteDataRepository
    @Mock
    private lateinit var voteDataLocalDataSource: VoteDataSource
    @Mock
    private lateinit var voteDataRemoteDataSource: VoteDataSource
    @Mock
    private lateinit var getVoteListCallback: VoteDataSource.GetVoteListCallback
    @Mock
    private lateinit var getVoteDataCallback: VoteDataSource.GetVoteDataCallback
    @Mock
    private lateinit var getVoteOptionsCallback: VoteDataSource.GetVoteOptionsCallback
    @Mock
    private lateinit var addNewOptionCallback: VoteDataSource.AddNewOptionCallback
    @Mock
    private lateinit var pollVoteCallback: VoteDataSource.PollVoteCallback
    @Mock
    private lateinit var favoriteVoteCallback: VoteDataSource.FavoriteVoteCallback
    @Captor
    private lateinit var getVoteDataCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteDataCallback>
    @Captor
    private lateinit var getVoteListCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteListCallback>
    @Captor
    private lateinit var getVoteOptionsCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteOptionsCallback>
    @Captor
    private lateinit var addNewOptionCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.AddNewOptionCallback>
    @Captor
    private lateinit var pollVoteCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.PollVoteCallback>
    @Captor
    private lateinit var favoriteVoteCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.FavoriteVoteCallback>

    @Mock
    private lateinit var user: User

    private var voteData: VoteData = newVoteData(101)

    @Before
    fun setupVoteDataRepository() {
        MockitoAnnotations.initMocks(this)

        for (i in 0 until OFFSET * 2) {

            voteDataList.add(newVoteData(i.toLong()))
        }
        for (i in 0 until OFFSET) {

            optionList.add(newOption(i.toLong()))
        }
        voteData.voteCode = "code"
        voteData = newVoteData(0)
        voteDataRepository = VoteDataRepository.getInstance(voteDataLocalDataSource, voteDataRemoteDataSource)

        user = Mockito.mock(User::class.java)

        `when`(user.getUserName()).thenReturn("Heaton")
        `when`(user.userCode).thenReturn("Heaton")
    }

    @After
    fun destroyRepositoryInstance() {
        VoteDataRepository.destroyInstance()
    }


    @Test
    fun testUserIsNotNull() {
        assertNotNull(user)
    }

    @Test
    fun getHotVoteList_requestFromRemote() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource)
                .getHotVoteList(eq(OFFSET), eq(user), any<VoteDataSource.GetVoteListCallback>())

    }

    @Test
    fun saveVote_saveVoteToLocal() {
        val voteData = newVoteData(0)
        voteDataRepository.saveVoteData(voteData)

        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteData(voteData)
    }

    @Test
    fun saveHotVoteDataList_saveVoteDataListToLocal() {
        voteDataRepository.saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT)
        verify<VoteDataSource>(voteDataLocalDataSource)
                .saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT)
    }

    @Test
    fun getHotVoteDataList_getFromLocal() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback)
        setHotVoteDataListNotAvailable(voteDataRemoteDataSource)
        setHotVoteDataListAvailable(voteDataLocalDataSource, voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).getHotVoteList(eq(OFFSET)
                , eq(user), any<VoteDataSource.GetVoteListCallback>())
        verify(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getHotVoteDataListWithBothDataSourceFailure_firesOnDataUnavailable() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback)
        setHotVoteDataListNotAvailable(voteDataRemoteDataSource)
        setHotVoteDataListNotAvailable(voteDataLocalDataSource)
        verify<GetVoteListCallback>(getVoteListCallback).onVoteListNotAvailable()
    }

    @Test
    fun getCreateVoteDataList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getCreateVoteList(OFFSET, user, User(), getVoteListCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).getCreateVoteList(eq(OFFSET), eq(user), any<User>(), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_CREATE)
        verify<VoteDataSource>(voteDataLocalDataSource, never()).getCreateVoteList(OFFSET, user, User(), getVoteListCallback)
        verify<VoteDataSource.GetVoteListCallback>(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getParticipateVoteList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getParticipateVoteList(OFFSET, user, User(), getVoteListCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).getParticipateVoteList(
                eq(OFFSET), eq(user)
                , any(), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteDataList(voteDataList
                , OFFSET, MainPageTabFragment.TAB_PARTICIPATE)
        verify<VoteDataSource>(voteDataLocalDataSource, never()).getParticipateVoteList(OFFSET
                , user, User(), getVoteListCallback)
        verify<GetVoteListCallback>(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getFavoriteVoteList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getFavoriteVoteList(OFFSET, user, user, getVoteListCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).getFavoriteVoteList(eq(OFFSET)
                , eq(user), any<User>(), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteDataList(voteDataList
                , OFFSET, MainPageTabFragment.TAB_FAVORITE)
        verify<VoteDataSource>(voteDataLocalDataSource, never())
                .getFavoriteVoteList(OFFSET, user, User(), getVoteListCallback)
        verify<GetVoteListCallback>(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getSearchVoteList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getSearchVoteList("searchkeyword", OFFSET, user, getVoteListCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).getSearchVoteList(ArgumentMatchers.anyString(), eq(OFFSET), eq(user), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, "tab")
        verify<VoteDataSource>(voteDataLocalDataSource, never()).getSearchVoteList("searckeyword", OFFSET, user, getVoteListCallback)
        verify<GetVoteListCallback>(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getHotVoteDataList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getHotVoteList(OFFSET, user, getVoteListCallback)
        setHotVoteDataListAvailable(voteDataRemoteDataSource, voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_HOT)
        verify<VoteDataSource>(voteDataLocalDataSource, never()).getHotVoteList(OFFSET, user, getVoteListCallback)
        verify<GetVoteListCallback>(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getNewVoteDataList_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getNewVoteList(OFFSET, user, getVoteListCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).getNewVoteList(eq(OFFSET), eq(user), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteDataList(voteDataList, OFFSET, MainPageTabFragment.TAB_NEW)
        verify<VoteDataSource>(voteDataLocalDataSource, never()).getNewVoteList(OFFSET, user, getVoteListCallback)
        verify<GetVoteListCallback>(getVoteListCallback).onVoteListLoaded(voteDataList)
    }

    @Test
    fun getVoteData_getFromRemoteAndSaveToLocal() {
        voteDataRepository.getVoteData(voteData.voteCode, user, getVoteDataCallback)
        setVoteDataAvailable(voteDataRemoteDataSource, voteData)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteData(eq(voteData))
        verify<VoteDataSource>(voteDataLocalDataSource
                , never()).getVoteData(voteData.voteCode, user, getVoteDataCallback)

        verify<GetVoteDataCallback>(getVoteDataCallback).onVoteDataLoaded(voteData)
    }

    @Test
    fun getVoteData_getFromLocal() {
        voteDataRepository.getVoteData(voteData.voteCode, user, getVoteDataCallback)
        setVoteDataNotAvailable(voteDataRemoteDataSource, voteData.voteCode)
        setVoteDataAvailable(voteDataLocalDataSource, voteData)
        verify<VoteDataSource>(voteDataLocalDataSource, never()).saveVoteData(eq(voteData))
        verify<GetVoteDataCallback>(getVoteDataCallback).onVoteDataLoaded(voteData)
    }

    @Test
    fun getVoteDataWithBothDataSourceFailure_firesOnDataUnavailable() {
        voteDataRepository.getVoteData(voteData.voteCode, user, getVoteDataCallback)
        setVoteDataNotAvailable(voteDataRemoteDataSource, voteData.voteCode)
        setVoteDataNotAvailable(voteDataLocalDataSource, voteData.voteCode)
        verify<VoteDataSource>(voteDataLocalDataSource, never()).saveVoteData(eq(voteData))
        verify<GetVoteDataCallback>(getVoteDataCallback).onVoteDataNotAvailable()
    }

    @Test
    fun getOptions_requestFromLocal() {
        voteDataRepository.getOptions(voteData, getVoteOptionsCallback)
        setOptionsAvailable(voteDataLocalDataSource, voteData)
        verify<VoteDataSource>(voteDataRemoteDataSource, never()).getOptions(voteData, getVoteOptionsCallback)
        verify<VoteDataSource>(voteDataLocalDataSource).getOptions(voteData, getVoteOptionsCallback)
        verify<GetVoteOptionsCallback>(getVoteOptionsCallback).onVoteOptionsLoaded(optionList)
    }

    @Test
    fun saveOptions_saveToLocal() {
        voteDataRepository.saveOptions(optionList)
        verify<VoteDataSource>(voteDataRemoteDataSource, never()).saveOptions(eq(optionList))
    }

    @Test
    fun addNewOptionToRemoteSuccess_saveToLocal() {
        val newOption = ArrayList<String>()
        voteDataRepository.addNewOption(voteData.voteCode, "password", newOption, user, addNewOptionCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).addNewOption(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), any<User>(), capture(addNewOptionCallbackArgumentCaptor))
        addNewOptionCallbackArgumentCaptor.value.onSuccess(voteData)
        verify<AddNewOptionCallback>(addNewOptionCallback).onSuccess(voteData)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteData(eq(voteData))
    }

    @Test
    fun pollVoteToRemoteSuccess_saveToLocal() {
        val pollOptions = ArrayList<String>()
        voteDataRepository.pollVote(voteData.voteCode, "password", pollOptions, user, pollVoteCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).pollVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), any<User>(), capture(pollVoteCallbackArgumentCaptor))
        pollVoteCallbackArgumentCaptor.value.onSuccess(voteData)
        verify<PollVoteCallback>(pollVoteCallback).onSuccess(eq(voteData))
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteData(eq(voteData))
    }

    @Test
    fun pollVoteToRemoteWithErrorPassword_onPasswordInvalid() {
        val pollOptions = ArrayList<String>()
        voteDataRepository.pollVote(voteData.voteCode, "password", pollOptions, user, pollVoteCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).pollVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), any<User>(), capture(pollVoteCallbackArgumentCaptor))
        pollVoteCallbackArgumentCaptor.value.onPasswordInvalid()
        verify<PollVoteCallback>(pollVoteCallback).onPasswordInvalid()
        verify<VoteDataSource>(voteDataLocalDataSource, never()).saveVoteData(eq(voteData))
    }

    @Test
    fun favoriteVoteWithBothDataSourceSuccess() {
        voteDataRepository.favoriteVote(voteData.voteCode, voteData.isFavorite, user, favoriteVoteCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).favoriteVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean(), any<User>(), capture(favoriteVoteCallbackArgumentCaptor))
        favoriteVoteCallbackArgumentCaptor.value.onSuccess(voteData.isFavorite)
        verify<VoteDataSource>(voteDataLocalDataSource).favoriteVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean(), any<User>(), capture(favoriteVoteCallbackArgumentCaptor))
        favoriteVoteCallbackArgumentCaptor.value.onSuccess(voteData.isFavorite)
        verify<FavoriteVoteCallback>(favoriteVoteCallback).onSuccess(ArgumentMatchers.anyBoolean())
    }

    @Test
    fun createVoteToRemote_saveToLocal() {
        val newOption = ArrayList<String>()
        voteDataRepository.createVote(voteData, newOption, null, getVoteDataCallback)
        verify<VoteDataSource>(voteDataRemoteDataSource).createVote(any<VoteData>()
                , ArgumentMatchers.anyList(), any<File>()
                , capture(getVoteDataCallbackArgumentCaptor))
        getVoteDataCallbackArgumentCaptor.value.onVoteDataLoaded(voteData)
        verify<GetVoteDataCallback>(getVoteDataCallback).onVoteDataLoaded(voteData)
        verify<VoteDataSource>(voteDataLocalDataSource).saveVoteData(eq(voteData))
    }

    private fun setHotVoteDataListNotAvailable(dataSource: VoteDataSource) {
        verify(dataSource).getHotVoteList(eq(OFFSET), eq(user), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListNotAvailable()
    }

    private fun setHotVoteDataListAvailable(dataSource: VoteDataSource, voteDataList: List<VoteData>) {
        verify(dataSource).getHotVoteList(eq(OFFSET), eq(user), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
    }

    private fun setVoteDataNotAvailable(dataSource: VoteDataSource, voteCode: String) {
        verify(dataSource).getVoteData(eq<String>(voteCode), eq(user), capture(getVoteDataCallbackArgumentCaptor))
        getVoteDataCallbackArgumentCaptor.value.onVoteDataNotAvailable()
    }

    private fun setVoteDataAvailable(dataSource: VoteDataSource, voteData: VoteData) {
        verify(dataSource).getVoteData(eq<String>(voteData.voteCode), eq(user), capture(getVoteDataCallbackArgumentCaptor))
        getVoteDataCallbackArgumentCaptor.value.onVoteDataLoaded(voteData)
    }

    private fun setOptionsNotAvailable(dataSource: VoteDataSource, voteData: VoteData) {
        verify(dataSource).getOptions(eq(voteData), capture(getVoteOptionsCallbackArgumentCaptor))
        getVoteOptionsCallbackArgumentCaptor.value.onVoteOptionsNotAvailable()
    }

    private fun setOptionsAvailable(dataSource: VoteDataSource, voteData: VoteData) {
        verify(dataSource).getOptions(eq(voteData), capture(getVoteOptionsCallbackArgumentCaptor))
        getVoteOptionsCallbackArgumentCaptor.value.onVoteOptionsLoaded(optionList)
    }


    private fun newVoteData(index: Long): VoteData {
        val voteData1 = VoteData()
        voteData1.voteCode = "CODE_$index"
        voteData1.title = "TITLE_$index"
        voteData1.id = index
        voteData1.startTime = System.currentTimeMillis() - 36000000
        voteData1.endTime = System.currentTimeMillis() + 36000000
        return voteData1
    }

    fun newOption(index: Long): Option {
        val Data = Option()
        Data.voteCode = "CODE_$index"
        Data.title = "TITLE_$index"
        Data.id = index
        Data.code = "OPTION_$index"
        return Data
    }

    companion object {

        val OFFSET = 0
        var voteDataList: MutableList<VoteData> = mutableListOf<VoteData>()
        var optionList: MutableList<Option> = mutableListOf()
    }
}

