package com.heaton.funnyvote.ui.search

import com.google.common.collect.Lists
import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.eq
import com.heaton.funnyvote.ui.search.SearchContract.View
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.*

class searchPresenterTest {

    private lateinit var presenter: SearchPresenter

    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Mock
    private lateinit var voteDataRepository: VoteDataRepository
    @Mock
    private lateinit var view: SearchContract.View

    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>
    @Captor
    private lateinit var getVoteListCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteListCallback>

    private var user: User = User().apply {
        userCode = "user_code"
        userID = "user id"
        userName = "user_name"
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = SearchPresenter(voteDataRepository, userDataRepository, view)

        // Then the presenter is set to the userPageView
        verify(view).setPresenter(presenter)
    }

    @Test
    fun getSearchVotesFromRepositoryAndLoadIntoView() {
        presenter = SearchPresenter(voteDataRepository, userDataRepository, view)
        presenter.start("keyword")
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor)
                , ArgumentMatchers.eq(false))
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify(voteDataRepository).getSearchVoteList(ArgumentMatchers.anyString()
                , ArgumentMatchers.eq(0), eq(user)
                , capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(voteDataList)
        verify<SearchContract.View>(view).refreshFragment(voteDataList)
        verify<View>(view).setMaxCount(ArgumentMatchers.anyInt())
    }

    @Test
    fun getEmptySearchKeywordVotesFromRepositoryAndLoadIntoView() {
        presenter = SearchPresenter(voteDataRepository, userDataRepository, view)
        presenter.start()
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.eq(false))
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify(voteDataRepository, never()).getSearchVoteList(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyInt(), any<User>(), capture(getVoteListCallbackArgumentCaptor))
    }

    @Test
    fun clickOnVoteItem_IntentToVoteDetailTest() {
        presenter = SearchPresenter(voteDataRepository, userDataRepository, view)
        presenter.IntentToVoteDetail(voteData)
        verify<View>(view).showVoteDetail(any<VoteData>())
    }

    @Test
    fun refreshSearchVotesFromRepositoryAndLoadIntoView() {
        presenter = SearchPresenter(voteDataRepository, userDataRepository, view)
        presenter.keyword = "keyword"
        presenter.user = user
        val fullList = Lists.newArrayList<VoteData>()
        for (i in 0 until VoteDataRepository.PAGE_COUNT) {
            fullList.add(voteData)
        }
        presenter.setSearchVoteDataList(fullList)
        presenter.refreshSearchList()
        verify(voteDataRepository).getSearchVoteList(ArgumentMatchers.anyString(), ArgumentMatchers.eq(20), any<User>()
                , capture(getVoteListCallbackArgumentCaptor))
        getVoteListCallbackArgumentCaptor.value.onVoteListLoaded(fullList)
        verify<View>(view).refreshFragment(fullList)
        verify<View>(view).setMaxCount(ArgumentMatchers.anyInt())
    }

    companion object {

        private val voteCode = "CODE_123"
        private val voteData = VoteData()
        private val voteDataList = ArrayList<VoteData>()
    }
}
