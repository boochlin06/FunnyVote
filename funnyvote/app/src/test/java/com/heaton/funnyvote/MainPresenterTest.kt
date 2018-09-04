package com.heaton.funnyvote

import com.heaton.funnyvote.MainPageContract.View
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*

/**
 * Unit tests for the implementation of [MainActivity]
 */
class MainPresenterTest {
    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Mock
    private lateinit var view: MainPageContract.View

    private lateinit var presenter: MainPagePresenter
    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>

    @Captor
    private lateinit var updateUserArgumentCaptor: ArgumentCaptor<User>

    @Before
    fun setupMainPresenter() {
        MockitoAnnotations.initMocks(this)

        presenter = MainPagePresenter(userDataRepository, view)
        user = mock(User::class.java)
        `when`(user.getUserName()).thenReturn("Heaton")
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = MainPagePresenter(userDataRepository, view)

        // Then the presenter is set to the view
        verify(view).setPresenter(presenter)
    }

    @Test
    fun loadUserFromRepositoryAndLoadIntoView() {
        presenter.start()

        verify<UserDataRepository>(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.eq(false))
        getUserCallbackArgumentCaptor.value.onResponse(user)

        verify<MainPageContract.View>(view).updateUserView(capture(updateUserArgumentCaptor))
        assertTrue(updateUserArgumentCaptor.value.getUserName().isNotEmpty())
    }

    @Test
    fun clickOnToolbar_showCreateVoteUi() {
        presenter.IntentToCreatePage()
        verify<MainPageContract.View>(view).showCreatePage()
    }

    @Test
    fun clickOnToolbar_showSearchUi() {
        presenter.IntentToSearchPage("")
        verify<View>(view).showSearchPage("")
    }

    @Test
    fun clickOnDrawer_showUserBoxUi() {
        presenter.IntentToUserPage()
        verify<View>(view).showUserPage()
    }

    @Test
    fun clickOnDrawer_showAccountUi() {
        presenter.IntentToAccountPage()
        verify<View>(view).showAccountPage()
    }

    @Test
    fun clickOnDrawer_showAboutUi() {
        presenter.IntentToAboutPage()
        verify<MainPageContract.View>(view).showAboutPage()
    }

    @Test
    fun clickOnUserIcon_showUserBoxUi() {
        presenter.IntentToUserPage()
        verify<MainPageContract.View>(view).showUserPage()
    }

    @Test
    fun clickOnDrawer_showMainPageUi() {
        presenter.IntentToMainPage()
        verify<MainPageContract.View>(view).showMainPage()
    }

    companion object {
        private lateinit var user: User
    }
}
