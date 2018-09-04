package com.heaton.funnyvote.ui.votedetail

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.eq
import com.heaton.funnyvote.ui.votedetail.VoteDetailContract.View
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import java.util.*

/**
 * Unit tests for the implementation of [VoteDetailPresenter]
 */
class VoteDetailPresenterTest {

    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Mock
    private lateinit var voteDataRepository: VoteDataRepository
    @Mock
    private lateinit var view: VoteDetailContract.View

    private lateinit var presenter: VoteDetailPresenter
    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>
    @Captor
    private lateinit var getVoteDataCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteDataCallback>
    @Captor
    private lateinit var getVoteOptionsCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteOptionsCallback>
    @Captor
    private lateinit var favoriteVoteCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.FavoriteVoteCallback>
    @Captor
    private lateinit var pollVoteCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.PollVoteCallback>
    @Captor
    private lateinit var addNewOptionCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.AddNewOptionCallback>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        user = mock(User::class.java)
        `when`(user.getUserName()).thenReturn("Heaton")
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)

        // Then the presenter is set to the view
        verify(view).setPresenter(presenter)
    }

    @Test
    fun getVoteDataFromRepositoryAndLoadIntoView() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.start()
        verify<UserDataRepository>(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.eq(false))
        val inOrder = Mockito.inOrder(view)
        inOrder.verify<VoteDetailContract.View>(view).showLoadingCircle()
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify<View>(view).setUpAdMob(eq(user))
        verify<VoteDataRepository>(voteDataRepository).getVoteData(eq(voteCode), eq(user), getVoteDataCallbackArgumentCaptor.capture())
        getVoteDataCallbackArgumentCaptor.value.onVoteDataLoaded(voteData)
        inOrder.verify<View>(view).hideLoadingCircle()
        verify<VoteDataRepository>(voteDataRepository).getOptions(eq(voteData), capture(getVoteOptionsCallbackArgumentCaptor))
        getVoteOptionsCallbackArgumentCaptor.value.onVoteOptionsLoaded(optionList)
        verify<View>(view).setUpOptionAdapter(eq(voteData), ArgumentMatchers.anyInt(), eq(optionList))
    }

    @Test
    fun searchOptionAndUpdateToView() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.searchOption("test")
        verify<View>(view).updateSearchView(ArgumentMatchers.anyList(), ArgumentMatchers.eq(true))
    }

    @Test
    fun favoriteVoteAndUpdateToView() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData.voteCode = "VOTE_CODE_123"
        presenter.favoriteVote()
        verify<VoteDataRepository>(voteDataRepository).favoriteVote(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyBoolean(), any<User>(), capture(favoriteVoteCallbackArgumentCaptor))
        favoriteVoteCallbackArgumentCaptor.value.onSuccess(voteData.isFavorite)
        verify<View>(view).updateFavoriteView(ArgumentMatchers.anyBoolean())
        verify<View>(view).showHintToast(ArgumentMatchers.anyInt())
    }

    @Test
    fun pollVoteAndUpdateToView() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData = voteData
        presenter.pollVote("password")
        val inOrder = Mockito.inOrder(view)
        verify<VoteDataRepository>(voteDataRepository).pollVote(ArgumentMatchers.anyString(), eq("password")
                , ArgumentMatchers.anyList(), any<User>(), pollVoteCallbackArgumentCaptor.capture())
        inOrder.verify<View>(view).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onSuccess(voteData)
        inOrder.verify<View>(view).hideLoadingCircle()
        verify<View>(view).setUpViews(any<VoteData>(), ArgumentMatchers.anyInt())
        verify<View>(view).setUpOptionAdapter(any<VoteData>(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyList())
        verify<View>(view).setUpSubmit(ArgumentMatchers.anyInt())
        verify<View>(view).refreshOptions()
        verify<View>(view).hidePollPasswordDialog()
    }

    @Test
    fun pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData.isNeedPassword = true
        presenter.voteData.voteCode = "VOTE_CODE_123"
        presenter.pollVote("password")
        val inOrder = Mockito.inOrder(view)
        inOrder.verify<View>(view).showPollPasswordDialog()
        `when`(view.isPasswordDialogShowing).thenReturn(true)
        presenter.pollVote("password")
        verify<VoteDataRepository>(voteDataRepository).pollVote(ArgumentMatchers.anyString(), eq("password"), ArgumentMatchers.anyList(), any<User>(), pollVoteCallbackArgumentCaptor.capture())
        inOrder.verify<View>(view).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onPasswordInvalid()
        inOrder.verify<View>(view).shakePollPasswordDialog()
        inOrder.verify<View>(view).hideLoadingCircle()
    }

    @Test
    fun pollVoteFailureAndShowPWDialog() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData = voteData
        presenter.voteData.isNeedPassword = false
        presenter.pollVote("password")
        val inOrder = Mockito.inOrder(view)
        verify<VoteDataRepository>(voteDataRepository).pollVote(ArgumentMatchers.anyString(), eq("password"), ArgumentMatchers.anyList(), any<User>(), pollVoteCallbackArgumentCaptor.capture())
        inOrder.verify<View>(view).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onFailure()
        inOrder.verify<View>(view).hideLoadingCircle()
    }

    @Test
    fun addNewOptionNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData.isNeedPassword = true
        presenter.voteData.voteCode = voteCode
        presenter.addNewOptionCompleted("password", "newOptionText")
        val inOrder = Mockito.inOrder(view)
        inOrder.verify<View>(view).showAddNewOptionPasswordDialog(ArgumentMatchers.anyString())
        `when`(view.isPasswordDialogShowing).thenReturn(true)
        presenter.addNewOptionCompleted("password", "newOptionText")
        verify<VoteDataRepository>(voteDataRepository).addNewOption(ArgumentMatchers.anyString(), eq("password"), ArgumentMatchers.anyList(), any<User>(), capture(addNewOptionCallbackArgumentCaptor))
        inOrder.verify<View>(view).showLoadingCircle()
        addNewOptionCallbackArgumentCaptor.value.onPasswordInvalid()
        inOrder.verify<View>(view).shakeAddNewOptionPasswordDialog()
        inOrder.verify<View>(view).hideLoadingCircle()
    }

    @Test
    fun addNewOptionFailureAndShowPWDialog() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData = voteData
        presenter.voteData.isNeedPassword = false

        presenter.addNewOptionCompleted("password", "newOptionText")
        val inOrder = Mockito.inOrder(view)
        verify<VoteDataRepository>(voteDataRepository).addNewOption(ArgumentMatchers.anyString()
                , eq("password"), ArgumentMatchers.anyList(), any<User>(), capture(addNewOptionCallbackArgumentCaptor))
        inOrder.verify<View>(view).showLoadingCircle()
        addNewOptionCallbackArgumentCaptor.value.onFailure()
        inOrder.verify<View>(view).hideLoadingCircle()
    }

    @Test
    fun clickOnOption_refreshOptionChoiceTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.resetOptionChoiceStatus(1, "OPTION_123")
        verify<View>(view).updateChoiceOptions(ArgumentMatchers.anyList())
    }

    @Test
    fun clickOnOption_refreshOptionExpandTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.resetOptionExpandStatus("OPTION_123")
        verify<View>(view).updateExpandOptions(ArgumentMatchers.anyList())
    }

    @Test
    fun clickAddNewOption_addNewOptionStartTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.addNewOptionStart()
        verify<View>(view).refreshOptions()
    }

    @Test
    fun edit_addNewOptionContentReviseTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData = voteData
        presenter.addNewOptionContentRevise(1, "inputText")

    }

    @Test
    fun clickComplete_addNewOptionCompletedAndUpdateToView() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData = voteData
        presenter.addNewOptionCompleted("password", "newOptionText")
        val inOrder = Mockito.inOrder(view)
        verify<VoteDataRepository>(voteDataRepository).addNewOption(ArgumentMatchers.anyString(), eq("password"), ArgumentMatchers.anyList(), any<User>(), capture(addNewOptionCallbackArgumentCaptor))
        inOrder.verify<View>(view).showLoadingCircle()
        addNewOptionCallbackArgumentCaptor.value.onSuccess(voteData)
        inOrder.verify<View>(view).hideLoadingCircle()
        verify<View>(view).setUpViews(any<VoteData>(), ArgumentMatchers.anyInt())
        verify<View>(view).setUpOptionAdapter(any<VoteData>(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyList())
        verify<View>(view).setUpSubmit(ArgumentMatchers.anyInt())
        verify<View>(view).refreshOptions()
        verify<View>(view).hideAddNewOptionPasswordDialog()
    }

    @Test
    fun clickOnOption_removeOptionTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.removeOption(1)
        verify<View>(view).refreshOptions()
    }

    @Test
    fun clickOnFab_changeOptionTypeTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.isUserPreResult = false
        presenter.changeOptionType()
        verify<View>(view).showResultOption(ArgumentMatchers.anyInt())
        presenter.isUserPreResult = true
        presenter.changeOptionType()
        verify<View>(view).showUnPollOption(ArgumentMatchers.anyInt())
    }

    @Test
    fun clickOnFab_CheckSortOptionTypeTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.CheckSortOptionType()
        verify<View>(view).showSortOptionDialog(any<VoteData>())
    }

    @Test
    fun clickOnFab_sortOptionsTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.voteData.netOptions = optionList
        presenter.voteData.voteCode = voteCode;
        presenter.sortOptions(0)
        verify<View>(view).updateCurrentOptionsOrder(ArgumentMatchers.anyList());
    }

    @Test
    fun clickOnToolbar_IntentToVoteInfoTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.IntentToVoteInfo()
        verify<View>(view).showVoteInfoDialog(any<VoteData>())
    }

    @Test
    fun clickOnTitle_IntentToTitleDetailTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.IntentToTitleDetail()
        verify<View>(view).showTitleDetailDialog(any<VoteData>())
    }

    @Test
    fun clickOnMainBar_IntentToShareDialogTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.IntentToShareDialog()
        verify<View>(view).showShareDialog(any<VoteData>())
    }

    @Test
    fun clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = VoteDetailPresenter(voteCode, voteDataRepository, userDataRepository, view)
        presenter.IntentToAuthorDetail()
        verify<View>(view).showAuthorDetail(any<VoteData>())
    }

    companion object {
        private lateinit var user: User
        private val voteCode = "CODE_123"
        private val optionList = ArrayList<Option>()
        private val voteData = VoteData().apply {
            voteCode = "CODE_123"
            netOptions = optionList
        }
    }
}
