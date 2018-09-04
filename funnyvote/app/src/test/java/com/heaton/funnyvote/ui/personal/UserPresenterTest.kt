package com.heaton.funnyvote.ui.personal

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.eq
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.ui.personal.PersonalContract.UserPageView
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import java.util.*

class UserPresenterTest {

    private lateinit var presenter: UserPresenter

    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Mock
    private lateinit var voteDataRepository: VoteDataRepository
    @Mock
    private lateinit var userPageView: PersonalContract.UserPageView
    @Mock
    private lateinit var createFragment: MainPageTabFragment
    @Mock
    private lateinit var partFragment: MainPageTabFragment
    @Mock
    private lateinit var favoriteFragment: MainPageTabFragment

    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>
    @Captor
    private lateinit var getVoteListCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteListCallback>
    @Captor
    private lateinit var getVoteDataCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteDataCallback>
    @Captor
    private lateinit var pollVoteCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.PollVoteCallback>
    @Captor
    private lateinit var favoriteVoteCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.FavoriteVoteCallback>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        user = mock(User::class.java)
        `when`(user.getUserName()).thenReturn("Heaton")
        `when`(user.userCode).thenReturn("Heaton")
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)

        // Then the presenter is set to the userPageView
        verify<UserPageView>(userPageView).setPresenter(presenter)
    }

    @Test
    fun getVotesFromRepositoryAndLoadIntoView() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)
        presenter.loginUser = user
        presenter.setTargetUser(user)
        presenter.setCreateFragmentView(createFragment)
        presenter.setParticipateFragmentView(partFragment)
        presenter.setFavoriteFragmentView(favoriteFragment)
        presenter.setCreateVoteDataList(voteDataList)
        presenter.setParticipateVoteDataList(voteDataList)
        presenter.setFavoriteVoteDataList(voteDataList)
        presenter.start()

        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), eq(false))
        val inOrder = Mockito.inOrder(userPageView)
        inOrder.verify<UserPageView>(userPageView).showLoadingCircle()
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify<UserPageView>(userPageView).setUpUserView(user)
        verify<UserPageView>(userPageView).setUpTabsAdapter(eq(user), eq(user))

        verify(voteDataRepository, times(2)).getCreateVoteList(eq(0)
                , eq(user), eq(user), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(createFragment).setMaxCount(ArgumentMatchers.anyInt())
        verify(createFragment).refreshFragment(any())
        verify(createFragment).hideSwipeLoadView()
        verify<UserPageView>(userPageView).hideLoadingCircle()

        verify(voteDataRepository, times(2)).getParticipateVoteList(eq(0), eq(user), eq(user)
                , capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(partFragment).setMaxCount(ArgumentMatchers.anyInt())
        verify(partFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(partFragment).hideSwipeLoadView()
        verify<UserPageView>(userPageView, times(2)).hideLoadingCircle()


        verify(voteDataRepository, times(2)).getFavoriteVoteList(eq(0)
                , eq(user), eq(user), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(partFragment).setMaxCount(ArgumentMatchers.anyInt())
        verify(partFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(partFragment).hideSwipeLoadView()
        verify<UserPageView>(userPageView, times(3)).hideLoadingCircle()

    }


    @Test
    fun pollVoteAndUpdateToView() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)

        presenter.setCreateFragmentView(createFragment)
        presenter.setParticipateFragmentView(partFragment)
        presenter.setFavoriteFragmentView(favoriteFragment)
        val voteData = VoteData()
        voteData.voteCode = "CODE_123"
        voteData.isNeedPassword = false
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")

        val inOrder = Mockito.inOrder(userPageView)
        verify(voteDataRepository).pollVote(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyString(), ArgumentMatchers.anyList()
                , any(), capture(pollVoteCallbackArgumentCaptor))
        inOrder.verify<UserPageView>(userPageView).showLoadingCircle()

        pollVoteCallbackArgumentCaptor.value.onSuccess(voteData)

        inOrder.verify(userPageView).hideLoadingCircle()
        verify(createFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(partFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(favoriteFragment).refreshFragment(ArgumentMatchers.anyList())
    }

    @Test
    fun pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)

        presenter.setCreateFragmentView(createFragment)
        presenter.setParticipateFragmentView(partFragment)
        presenter.setFavoriteFragmentView(favoriteFragment)

        val voteData = VoteData()
        voteData.voteCode = "CODE_123"
        voteData.isNeedPassword = true
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")

        val inOrder = Mockito.inOrder(userPageView)
        inOrder.verify<UserPageView>(userPageView).showPollPasswordDialog(voteData, "OPTION_CODE_123")
        `when`(userPageView.isPasswordDialogShowing).thenReturn(true)
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")
        verify(voteDataRepository).pollVote(any(), eq("password")
                , any()
                , any(), capture(pollVoteCallbackArgumentCaptor))
        inOrder.verify<UserPageView>(userPageView).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onPasswordInvalid()
        inOrder.verify(userPageView).shakePollPasswordDialog()
        inOrder.verify(userPageView).hideLoadingCircle()
    }

    @Test
    fun pollVoteFailureAndShowPWDialog() {

        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)
        val voteData = VoteData()
        voteData.voteCode = "CODE_123"
        voteData.isNeedPassword = false
        val inOrder = Mockito.inOrder(userPageView)
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")
        verify(voteDataRepository).pollVote(ArgumentMatchers.anyString(), eq("password")
                , any(), any()
                , capture(pollVoteCallbackArgumentCaptor))
        inOrder.verify<UserPageView>(userPageView).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onFailure()
        inOrder.verify<UserPageView>(userPageView).hideLoadingCircle()
    }

    @Test
    fun favoriteVoteAndUpdateToView() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)

        presenter.setCreateFragmentView(createFragment)
        presenter.setParticipateFragmentView(partFragment)
        presenter.setFavoriteFragmentView(favoriteFragment)

        presenter.setCreateVoteDataList(voteDataList)
        presenter.setParticipateVoteDataList(voteDataList)
        presenter.setFavoriteVoteDataList(voteDataList)
        voteData.voteCode = "CODE_123"
        presenter.favoriteVote(voteData)
        verify(voteDataRepository).favoriteVote(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyBoolean()
                , any(), capture(favoriteVoteCallbackArgumentCaptor))
        favoriteVoteCallbackArgumentCaptor.value.onSuccess(voteData.isFavorite)
        verify(createFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(partFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(favoriteFragment).refreshFragment(ArgumentMatchers.anyList())

        verify<UserPageView>(userPageView).showHintToast(ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong())
    }

    @Test
    fun clickOnMainBar_IntentToShareDialogTest() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)
        presenter.IntentToShareDialog(voteData)
        verify(userPageView).showShareDialog(any())
    }

    @Test
    fun clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)
        presenter.IntentToAuthorDetail(voteData)
        verify<UserPageView>(userPageView).showAuthorDetail(any())
    }

    @Test
    fun clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)
        presenter.IntentToVoteDetail(voteData)
        verify<UserPageView>(userPageView).showVoteDetail(any<VoteData>())
    }

    @Test
    fun clickOnNoVoteItem_IntentToCreateVoteTest() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)
        presenter.IntentToCreateVote()
        verify<UserPageView>(userPageView).showCreateVote()
    }

    @Test
    fun refreshAllFragment_refreshSubFragment() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)

        presenter.setCreateFragmentView(createFragment)
        presenter.setParticipateFragmentView(partFragment)
        presenter.setFavoriteFragmentView(favoriteFragment)
        presenter.refreshAllFragment()

        verify(createFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(partFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(favoriteFragment).refreshFragment(ArgumentMatchers.anyList())
    }


    @Test
    fun notToAddOtherFragment() {
        presenter = UserPresenter(voteDataRepository, userDataRepository, userPageView)

        presenter.reloadHotList(0)
        presenter.reloadNewList(0)
        verify<UserPageView>(userPageView, never()).setUpTabsAdapter(any(), any())

        presenter.refreshHotList()
        presenter.refreshNewList()
        verify(userPageView, never()).setUpTabsAdapter(any(), any())
    }

    companion object {

        private lateinit var user: User
        private val voteCode = "CODE_123"
        private val voteData = VoteData()
        private val voteDataList = ArrayList<VoteData>()
    }
}
