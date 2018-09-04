package com.heaton.funnyvote.data.user

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.RemoteServiceApi
import com.heaton.funnyvote.data.user.UserDataSource.ChangeUserNameCallback
import com.heaton.funnyvote.data.user.UserDataSource.GetUserCallback
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.retrofit.Server
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserDataRepositoryTest {
    private lateinit var userDataRepository: UserDataRepository

    @Mock
    private lateinit var remoteUserDataSource: UserDataSource
    @Mock
    private lateinit var localUserDataSource: UserDataSource
    @Mock
    private lateinit var userService: Server.UserService
    @Mock
    private lateinit var getUserCallback: UserDataSource.GetUserCallback
    @Mock
    private lateinit var changeUserNameCallback: UserDataSource.ChangeUserNameCallback
    @Mock
    private lateinit var registerUserCallback: UserDataSource.RegisterUserCallback
    @Mock
    private lateinit var getUserCodeCallback: UserDataSource.GetUserCodeCallback
    @Mock
    private lateinit var getUserInfoCallback: UserDataSource.GetUserInfoCallback
    @Mock
    private lateinit var userQueryCallback: Callback<Server.UserDataQuery>


    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>

    @Captor
    private lateinit var changeUserNameCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.ChangeUserNameCallback>
    @Captor
    private lateinit var registerUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.RegisterUserCallback>
    @Captor
    private lateinit var getUserCodeCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCodeCallback>
    @Captor
    private lateinit var getUserInfoCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserInfoCallback>
    @Captor
    private lateinit var callbackUserQueryArgumentCaptor: ArgumentCaptor<Callback<Server.UserDataQuery>>

    @Captor
    private lateinit var callbackRespondedArgumentCaptor: ArgumentCaptor<Callback<ResponseBody>>

    @Before
    fun setUpUserRepository() {
        MockitoAnnotations.initMocks(this)
        userDataRepository = UserDataRepository.getInstance(localUserDataSource, remoteUserDataSource)
        user = mock(User::class.java)
    }

    @After
    fun destroyRepositoryInstance() {
        UserDataRepository.destroyInstance()
    }

    @Test
    fun getUserWithRemoteDataSource_FirstTimeApiCall() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_GUEST)
        `when`(user.userCode).thenReturn("")

        userDataRepository.getUser(getUserCallback, true)
        verify<UserDataSource>(remoteUserDataSource).getGuestUserCode(capture(getUserCodeCallbackArgumentCaptor), ArgumentMatchers.anyString())
        getUserCodeCallbackArgumentCaptor.value.onSuccess("userCode")

        verify(localUserDataSource).user = any<User>()
        verify<UserDataSource.GetUserCallback>(getUserCallback).onResponse(any<User>())
    }

    @Test
    fun getUserWithRemoteDataSourceFailure_FirstTimeApiCall() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_GUEST)
        `when`(user.userCode).thenReturn("")

        userDataRepository.getUser(getUserCallback, true)
        verify<UserDataSource>(remoteUserDataSource).getGuestUserCode(capture(getUserCodeCallbackArgumentCaptor), ArgumentMatchers.anyString())
        getUserCodeCallbackArgumentCaptor.value.onFalure()

        verify<UserDataSource.GetUserCallback>(getUserCallback).onFailure()
    }

    @Test
    fun getUserWithRemoteDataSource_forceUpdateAfterFirstTimeApiCall() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_GUEST)
        `when`(user.userCode).thenReturn("UserCode")

        userDataRepository.getUser(getUserCallback, true)
        verify<UserDataSource>(remoteUserDataSource).getUserInfo(capture(callbackUserQueryArgumentCaptor)
                , any<User>())
        val userDataQuery = Server.UserDataQuery()
        userDataQuery.guestCode = "guestCode"
        userDataQuery.otp = "otp"
        var call: retrofit2.Call<Server.UserDataQuery> = RemoteServiceApi.getInstance()
                .userService.getUserInfo(userDataQuery.guestCode!!, userDataQuery.otp!!);
        response = Response.success(userDataQuery)
        callbackUserQueryArgumentCaptor.value.onResponse(call, response)
        verify(localUserDataSource).user = any<User>()
        verify<GetUserCallback>(getUserCallback).onResponse(any<User>())
    }

    @Test
    fun getUserWithRemoteDataSourceFailure_forceUpdateAfterFirstTimeApiCall() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_GUEST)
        `when`(user.userCode).thenReturn("UserCode")

        userDataRepository.getUser(getUserCallback, true)
        verify<UserDataSource>(remoteUserDataSource).getUserInfo(capture(callbackUserQueryArgumentCaptor), any<User>())
        val userDataQuery = Server.UserDataQuery()
        userDataQuery.guestCode = "guestCode"
        userDataQuery.otp = "otp"
        var call: retrofit2.Call<Server.UserDataQuery> = RemoteServiceApi.getInstance()
                .userService.getUserInfo(userDataQuery.guestCode!!, userDataQuery.otp!!);
        response = Response.error(500, ResponseBody.create(null, "123"))
        callbackUserQueryArgumentCaptor.value.onResponse(call, response)
        verify<GetUserCallback>(getUserCallback).onFailure()
    }

    @Test
    fun getUser_requestUserFromLocalUserDataSource() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_GUEST)
        `when`(user.userCode).thenReturn("User Code")

        userDataRepository.getUser(getUserCallback, false)

        verify<UserDataSource>(remoteUserDataSource, never()).getGuestUserCode(
                capture(getUserCodeCallbackArgumentCaptor), ArgumentMatchers.anyString())
        verify(getUserCallback).onResponse(any<User>())
    }

    @Test
    fun registerUserMergeGuest_requestUserToThirdParty() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_FACEBOOK)
        `when`(user.userCode).thenReturn("User Code")

        userDataRepository.registerUser("appId", user, true, registerUserCallback)
        verify(localUserDataSource).user
        verify<UserDataSource>(remoteUserDataSource).getUserCode(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyString(), any<User>()
                , capture(getUserCodeCallbackArgumentCaptor))
        getUserCodeCallbackArgumentCaptor.value.onSuccess("User Code")
        verify<UserDataSource>(remoteUserDataSource)
                .linkGuestToLoginUser(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()
                        , capture(callbackRespondedArgumentCaptor))
        var call: Call<ResponseBody>? = RemoteServiceApi.getInstance().userService
                .linkGuestLoginUser("", "")
        var responseBody: Response<ResponseBody>? = Response.success(null)
        callbackRespondedArgumentCaptor.value.onResponse(call, responseBody)
        verify(localUserDataSource).user = any<User>()
        verify<UserDataSource.RegisterUserCallback>(registerUserCallback).onSuccess()

    }

    @Test
    fun registerUserMergeGuestFailure_requestUserToThirdParty() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_FACEBOOK)
        `when`(user.userCode).thenReturn("User Code")

        userDataRepository.registerUser("appId", user, true, registerUserCallback)
        verify(localUserDataSource).user
        verify<UserDataSource>(remoteUserDataSource).getUserCode(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyString(), any<User>()
                , capture(getUserCodeCallbackArgumentCaptor))
        getUserCodeCallbackArgumentCaptor.value.onSuccess("User Code")
        verify<UserDataSource>(remoteUserDataSource).linkGuestToLoginUser(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()
                , capture(callbackRespondedArgumentCaptor))
        var call: Call<ResponseBody>? = RemoteServiceApi.getInstance().userService
                .linkGuestLoginUser("", "")
        var responseBody: Response<ResponseBody>? = Response.error(500
                , ResponseBody.create(null, "123"))
        callbackRespondedArgumentCaptor.value.onResponse(call, responseBody)
        verify<UserDataSource.RegisterUserCallback>(registerUserCallback).onFailure()
    }

    @Test
    fun registerUserNoMergeGuest_requestUserToThirdParty() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_FACEBOOK)
        `when`(user.userCode).thenReturn("User Code")

        userDataRepository.registerUser("appId", user, false, registerUserCallback)
        verify(localUserDataSource).user
        verify<UserDataSource>(remoteUserDataSource).getUserCode(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyString(), any<User>()
                , capture(getUserCodeCallbackArgumentCaptor))
        getUserCodeCallbackArgumentCaptor.value.onSuccess("User Code")
        verify<UserDataSource>(remoteUserDataSource, never()).linkGuestToLoginUser(ArgumentMatchers.anyString()
                , ArgumentMatchers.anyString(), capture(callbackRespondedArgumentCaptor))

        verify(localUserDataSource).user = any<User>()
        verify(registerUserCallback).onSuccess()

    }

    @Test
    fun changeCurrentUserName_requestToRemoteTest() {
        `when`(localUserDataSource.user).thenReturn(user)
        `when`(user.type).thenReturn(User.TYPE_FACEBOOK)
        `when`(user.userCode).thenReturn("User Code")
        `when`(user.tokenType).thenReturn("otp")
        userDataRepository.changeCurrentUserName("name", changeUserNameCallback)
        verify<UserDataSource>(remoteUserDataSource).getUserInfo(capture(callbackUserQueryArgumentCaptor), any<User>())
        val userDataQuery = Server.UserDataQuery()
        userDataQuery.guestCode = "guestCode"
        userDataQuery.otp = "otp"
        var call: retrofit2.Call<Server.UserDataQuery> = RemoteServiceApi.getInstance()
                .userService.getUserInfo("guestCode", "otp");
        //localUserDataSource.user = user
        response = Response.success(userDataQuery)
        callbackUserQueryArgumentCaptor.value.onResponse(call, response)
        verify(localUserDataSource, times(1)).user = any<User>()

        verify<UserDataSource>(remoteUserDataSource).changeUserName(capture(callbackRespondedArgumentCaptor)
                , ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        response = Response.success(Server.UserDataQuery().apply {
            guestCode = "guest"
            otp = "otp"
        })
        var callBody: Call<ResponseBody>? = RemoteServiceApi.getInstance().userService
                .linkGuestLoginUser("", "")
        var responseBody: Response<ResponseBody>? = Response.success(null)
        callbackRespondedArgumentCaptor.value.onResponse(callBody, responseBody)
        verify(localUserDataSource, times(2)).user = any<User>()
        verify<ChangeUserNameCallback>(changeUserNameCallback).onSuccess()
    }

    @Test
    fun removeUser_onlyFromLocal() {
        userDataRepository.removeUser()
        verify<UserDataSource>(localUserDataSource).removeUser()
        verify<UserDataSource>(remoteUserDataSource, never()).removeUser()
    }

    companion object {

        private lateinit var response: Response<Server.UserDataQuery>

        private lateinit var user: User
    }


}
