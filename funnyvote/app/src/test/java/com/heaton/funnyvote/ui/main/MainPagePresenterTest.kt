package com.heaton.funnyvote.ui.main

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.promotion.PromotionDataSource
import com.heaton.funnyvote.data.promotion.PromotionRepository
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.main.MainPageContract.MainPageView
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import java.util.*

class MainPagePresenterTest {

    private lateinit var presenter: MainPagePresenter

    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Mock
    private lateinit var voteDataRepository: VoteDataRepository
    @Mock
    private lateinit var promotionRepository: PromotionRepository
    @Mock
    private lateinit var mainPageView: MainPageContract.MainPageView
    @Mock
    private lateinit var hotFragment: MainPageTabFragment
    @Mock
    private lateinit var newFragment: MainPageTabFragment

    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>
    @Captor
    private lateinit var getPromotionsCallbackArgumentCaptor: ArgumentCaptor<PromotionDataSource.GetPromotionsCallback>
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
        user = User().apply {
            userCode = "user_code"
            userID = "user id"
            userName = "user_name"
        }
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)

        // Then the presenter is set to the mainPageView
        verify<MainPageView>(mainPageView).setPresenter(presenter)
    }

    @Test
    fun getVotesAndPromotionFromRepositoryAndLoadIntoView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.start()
        presenter.setNewsFragmentView(newFragment)
        presenter.setHotsFragmentView(hotFragment)
        presenter.setHotVoteDataList(voteDataList.toMutableList())
        presenter.setNewVoteDataList(voteDataList)
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.eq(false))
        val inOrder = Mockito.inOrder(mainPageView)
        inOrder.verify<MainPageView>(mainPageView).showLoadingCircle()
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify<MainPageContract.MainPageView>(mainPageView).setUpTabsAdapter(any<User>())
        verify(promotionRepository).getPromotionList(any<User>()
                , capture(getPromotionsCallbackArgumentCaptor))
        getPromotionsCallbackArgumentCaptor.value.onPromotionsLoaded(ArrayList())
        verify<MainPageView>(mainPageView).setupPromotionAdmob(ArgumentMatchers.anyList(), any<User>())

        verify(voteDataRepository).getHotVoteList(ArgumentMatchers.eq(0)
                , any<User>(), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(hotFragment).setMaxCount(ArgumentMatchers.anyInt())
        verify(hotFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(hotFragment).hideSwipeLoadView()
        verify<MainPageView>(mainPageView).hideLoadingCircle()

        verify(voteDataRepository).getNewVoteList(ArgumentMatchers.eq(0), any<User>(), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(newFragment).setMaxCount(ArgumentMatchers.anyInt())
        verify(newFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(newFragment).hideSwipeLoadView()
        verify<MainPageView>(mainPageView, times(2)).hideLoadingCircle()

    }

    @Test
    fun refreshHotFragmentAndUpdateToView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setNewsFragmentView(newFragment)
        presenter.setHotsFragmentView(hotFragment)
        presenter.setHotVoteDataList(voteDataList)
        presenter.setNewVoteDataList(voteDataList)
        presenter.setUser(user)
        presenter.refreshHotList()
        verify(voteDataRepository).getHotVoteList(ArgumentMatchers.eq(0), any<User>()
                , capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(hotFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(hotFragment).hideSwipeLoadView()
        verify<MainPageView>(mainPageView).hideLoadingCircle()
    }

    @Test
    fun refreshNewFragmentAndUpdateToView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setUser(user)
        presenter.setNewsFragmentView(newFragment)
        presenter.setHotsFragmentView(hotFragment)
        presenter.setHotVoteDataList(voteDataList)
        presenter.setNewVoteDataList(voteDataList)
        presenter.refreshNewList()
        verify(voteDataRepository).getNewVoteList(ArgumentMatchers.eq(0), any<User>()
                , capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify(newFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(newFragment).hideSwipeLoadView()
        verify<MainPageView>(mainPageView, times(1)).hideLoadingCircle()
    }

    @Test
    fun refreshNewFragmentFailureUpdateToView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setUser(user)
        presenter.setNewsFragmentView(newFragment)
        presenter.reloadNewList(0)
        verify(voteDataRepository).getNewVoteList(ArgumentMatchers.anyInt(), any<User>(), capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListNotAvailable()
        verify<MainPageView>(mainPageView).showHintToast(ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong())
        verify<MainPageView>(mainPageView).hideLoadingCircle()
        verify(newFragment).hideSwipeLoadView()
    }

    @Test
    fun refreshHotFragmentFailureUpdateToView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setUser(user)
        presenter.setHotsFragmentView(hotFragment)
        presenter.reloadHotList(0)
        verify(voteDataRepository).getHotVoteList(ArgumentMatchers.anyInt(), any<User>()
                , capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListNotAvailable()
        verify<MainPageView>(mainPageView).showHintToast(ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong())
        verify<MainPageView>(mainPageView).hideLoadingCircle()
        verify(hotFragment).hideSwipeLoadView()
    }

    @Test
    fun pollVoteAndUpdateToView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setHotsFragmentView(hotFragment)
        presenter.setNewsFragmentView(newFragment)
        val voteData = VoteData()
        voteData.voteCode = "CODE_123"
        voteData.isNeedPassword = false
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")

        val inOrder = Mockito.inOrder(mainPageView)
        verify(voteDataRepository).pollVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()
                , ArgumentMatchers.anyList(), any<User>(), capture(pollVoteCallbackArgumentCaptor))
        inOrder.verify<MainPageView>(mainPageView).showLoadingCircle()

        pollVoteCallbackArgumentCaptor.value.onSuccess(voteData)

        inOrder.verify<MainPageView>(mainPageView).hideLoadingCircle()
        verify(hotFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(newFragment).refreshFragment(ArgumentMatchers.anyList())
    }

    @Test
    fun pollVoteNeedPWAndShakeDialogAfterShowPWDialog() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)

        val voteData = VoteData()
        voteData.voteCode = "CODE_123"
        voteData.isNeedPassword = true
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")

        val inOrder = Mockito.inOrder(mainPageView)
        inOrder.verify<MainPageView>(mainPageView).showPollPasswordDialog(voteData, "OPTION_CODE_123")
        `when`(mainPageView.isPasswordDialogShowing).thenReturn(true)
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")
        verify(voteDataRepository).pollVote(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyString(), ArgumentMatchers.anyList()
                , any<User>(), capture(pollVoteCallbackArgumentCaptor))
        inOrder.verify<MainPageView>(mainPageView).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onPasswordInvalid()
        inOrder.verify<MainPageView>(mainPageView).shakePollPasswordDialog()
        inOrder.verify<MainPageView>(mainPageView).hideLoadingCircle()
    }

    @Test
    fun pollVoteFailureAndShowPWDialog() {

        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)

        val voteData = VoteData()
        voteData.voteCode = "CODE_123"
        voteData.isNeedPassword = false
        val inOrder = Mockito.inOrder(mainPageView)
        presenter.pollVote(voteData, "OPTION_CODE_123", "password")
        verify(voteDataRepository).pollVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()
                , ArgumentMatchers.anyList(), any<User>(), capture(pollVoteCallbackArgumentCaptor))
        inOrder.verify<MainPageView>(mainPageView).showLoadingCircle()
        pollVoteCallbackArgumentCaptor.value.onFailure()
        inOrder.verify<MainPageView>(mainPageView).hideLoadingCircle()
    }

    @Test
    fun favoriteVoteAndUpdateToView() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)

        presenter.setNewsFragmentView(newFragment)
        presenter.setHotsFragmentView(hotFragment)
        presenter.setHotVoteDataList(voteDataList)
        presenter.setNewVoteDataList(voteDataList)
        voteData.voteCode = "CODE_123"
        presenter.favoriteVote(voteData)
        verify(voteDataRepository).favoriteVote(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean()
                , any<User>(), capture(favoriteVoteCallbackArgumentCaptor))
        favoriteVoteCallbackArgumentCaptor.value.onSuccess(voteData.isFavorite)
        verify(hotFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(newFragment).refreshFragment(ArgumentMatchers.anyList())
        verify<MainPageView>(mainPageView).showHintToast(ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong())
    }


    @Test
    fun clickOnMainBar_IntentToShareDialogTest() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.IntentToShareDialog(voteData)
        verify<MainPageView>(mainPageView).showShareDialog(any<VoteData>())
    }

    @Test
    fun clickOnMainBar_IntentToAuthorDetailTest() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.IntentToAuthorDetail(voteData)
        verify<MainPageView>(mainPageView).showAuthorDetail(any<VoteData>())
    }

    @Test
    fun clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.IntentToVoteDetail(voteData)
        verify<MainPageView>(mainPageView).showVoteDetail(any<VoteData>())
    }

    @Test
    fun clickOnNoVoteItem_IntentToCreateVoteTest() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.IntentToCreateVote()
        verify<MainPageView>(mainPageView).showCreateVote()
    }

    @Test
    fun refreshAllFragment_refreshSubFragment() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setNewsFragmentView(newFragment)
        presenter.setHotsFragmentView(hotFragment)
        presenter.refreshAllFragment()
        verify(hotFragment).refreshFragment(ArgumentMatchers.anyList())
        verify(newFragment).refreshFragment(ArgumentMatchers.anyList())
    }

    @Test
    fun refreshPromotion_resetPromotion() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.setUser(user)
        presenter.resetPromotion()
        verify(promotionRepository).getPromotionList(any<User>(), capture(getPromotionsCallbackArgumentCaptor))
        getPromotionsCallbackArgumentCaptor.value.onPromotionsLoaded(ArrayList())
        verify<MainPageView>(mainPageView).setupPromotionAdmob(ArgumentMatchers.anyList(), any<User>())
    }

    @Test
    fun notToAddOtherFragment() {
        presenter = MainPagePresenter(voteDataRepository, userDataRepository, promotionRepository, mainPageView)
        presenter.reloadCreateList(0)
        presenter.reloadParticipateList(0)
        presenter.reloadFavoriteList(0)
        verify<MainPageView>(mainPageView, never()).setUpTabsAdapter(any<User>(), any<User>())

        presenter.refreshCreateList()
        presenter.refreshFavoriteList()
        presenter.refreshParticipateList()
        verify<MainPageView>(mainPageView, never()).setUpTabsAdapter(any<User>(), any<User>())
    }

    companion object {

        private lateinit var user: User
        private val voteCode = "CODE_123"
        private val voteData = VoteData()
        private val voteDataList = ArrayList<VoteData>()
    }

}