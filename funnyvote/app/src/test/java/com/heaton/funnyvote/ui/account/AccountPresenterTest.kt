package com.heaton.funnyvote.ui.account

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.account.AccountContract.View

import org.junit.Before
import org.junit.Test
import org.mockito.*

import java.util.ArrayList

import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class AccountPresenterTest {

    private lateinit var presenter: AccountPresenter

    @Mock
    private lateinit var view: AccountContract.View

    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>
    @Captor
    private lateinit var registerUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.RegisterUserCallback>
    @Captor
    private lateinit var changeUserNameCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.ChangeUserNameCallback>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        user = mock(User::class.java)
        `when`(user.getUserName()).thenReturn("Heaton")
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = AccountPresenter(userDataRepository, view)

        // Then the presenter is set to the userPageView
        verify<View>(view).setPresenter(presenter)
    }

    @Test
    fun getUserFromRepositoryAndUpdateToView() {
        presenter = AccountPresenter(userDataRepository, view)
        presenter.start()
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.eq(false))
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify<View>(view).showUser(any<User>())
    }

    @Test
    fun logoutUserAndUpdateToView() {
        presenter = AccountPresenter(userDataRepository, view)

        `when`(user.type).thenReturn(User.TYPE_FACEBOOK)
        presenter.user = user
        presenter.logout()
        verify<View>(view).facebookLogout()

        `when`(user.type).thenReturn(User.TYPE_GOOGLE)
        presenter.user = user
        presenter.logout()
        verify<View>(view).googleSignOut()

        `when`(user.type).thenReturn(User.TYPE_TWITTER)
        presenter.user = user
        presenter.logout()
        verify<View>(view).twitterlogout()
    }

    @Test
    fun loginUserAndUpdateToView() {
        presenter = AccountPresenter(userDataRepository, view)
        presenter.login(AccountPresenter.LOGIN_FB, true)
        verify<View>(view).facebookLogin()

        presenter.login(AccountPresenter.LOGIN_GOOGLE, true)
        verify<View>(view).googleSignIn()

        presenter.login(AccountPresenter.LOGIN_TWITTER, true)
        verify<View>(view).twitterLogin()
    }

    @Test
    fun registerUserAndUpdateToView() {
        presenter = AccountPresenter(userDataRepository, view)
        presenter.registerUser(user, "appId")
        verify(userDataRepository).registerUser(ArgumentMatchers.anyString(), any<User>(), ArgumentMatchers.anyBoolean(), capture(registerUserCallbackArgumentCaptor))
        registerUserCallbackArgumentCaptor.value.onSuccess()
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.anyBoolean())
        getUserCallbackArgumentCaptor.value.onResponse(user)
        verify<View>(view).showUser(any<User>())
    }

    @Test
    fun registerUserFailureAndUpdateToView() {
        presenter = AccountPresenter(userDataRepository, view)
        presenter.registerUser(user, "appId")
        verify(userDataRepository).registerUser(ArgumentMatchers.anyString(), any<User>(), ArgumentMatchers.anyBoolean(), capture(registerUserCallbackArgumentCaptor))
        registerUserCallbackArgumentCaptor.value.onFailure()
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.anyBoolean())
        getUserCallbackArgumentCaptor.value.onFailure()
        verify<View>(view).showLoginView(ArgumentMatchers.anyString())
    }

    @Test
    fun changeUserNameFailureAndUpdateToView() {
        presenter = AccountPresenter(userDataRepository, view)
        presenter.changeCurrentUserName("new name")
        verify(userDataRepository).changeCurrentUserName(ArgumentMatchers.anyString(), capture(changeUserNameCallbackArgumentCaptor))
        changeUserNameCallbackArgumentCaptor.value.onSuccess()
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.anyBoolean())
        getUserCallbackArgumentCaptor.value.onFailure()
        verify<View>(view).showLoginView(ArgumentMatchers.anyString())
    }

    @Test
    fun unregisterUser() {
        presenter = AccountPresenter(userDataRepository, view)
        presenter.unregisterUser()
        verify(userDataRepository).unregisterUser()
    }

    companion object {

        private lateinit var user: User
        private val voteCode = "CODE_123"
        private val voteData = VoteData()
        private val voteDataList = ArrayList<VoteData>()
    }

}
