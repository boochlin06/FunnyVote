package com.heaton.funnyvote.ui.createvote

import com.heaton.funnyvote.any
import com.heaton.funnyvote.capture
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.createvote.CreateVoteContract.*
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*
import java.io.File
import java.util.*

class CreateVoteActivityPresenterTest {

    @Mock
    private lateinit var userDataRepository: UserDataRepository
    @Mock
    private lateinit var voteDataRepository: VoteDataRepository
    @Mock
    private lateinit var activityView: CreateVoteContract.ActivityView
    @Mock
    private lateinit var settingFragmentView: CreateVoteContract.SettingFragmentView
    @Mock
    private lateinit var optionFragmentView: CreateVoteContract.OptionFragmentView

    private lateinit var presenter: CreateVoteActivityPresenter

    @Captor
    private lateinit var getVoteDataCallbackArgumentCaptor: ArgumentCaptor<VoteDataSource.GetVoteDataCallback>

    @Captor
    private lateinit var getUserCallbackArgumentCaptor: ArgumentCaptor<UserDataSource.GetUserCallback>

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        user = mock(User::class.java)
        `when`(user.getUserName()).thenReturn("Heaton")
    }

    @Test
    fun createPresenter_setsThePresenterToView() {
        // Get a reference to the class under test
        presenter = CreateVoteActivityPresenter(voteDataRepository, userDataRepository, activityView, optionFragmentView, settingFragmentView)

        // Then the presenter is set to the view
        verify(activityView).setPresenter(presenter)
    }

    @Test
    fun getUserAndInitialDefaultView() {
        presenter = CreateVoteActivityPresenter(voteDataRepository, userDataRepository, activityView, optionFragmentView, settingFragmentView)
        presenter.start()
        verify(userDataRepository).getUser(capture(getUserCallbackArgumentCaptor), ArgumentMatchers.eq(false))
        getUserCallbackArgumentCaptor.value.onResponse(user)
    }

    @Test
    fun submitCreateVoteSuccessAndShowVoteDetail() {
        presenter = CreateVoteActivityPresenter(voteDataRepository, userDataRepository, activityView, optionFragmentView, settingFragmentView)
        presenter.voteSettings = voteData

        optionList.clear()
        for (i in 0..1) {
            val option = Option()
            option.title = "OPTION_CODE_$i"
            option.id = i.toLong()
            option.count = 0
            optionList.add(option)
        }
        presenter.optionList = optionList
        presenter.voteSettings.title = "TITLE_1"
        presenter.voteSettings.authorCode = "AUTHOR_1"
        presenter.voteSettings.maxOption = 1
        presenter.voteSettings.minOption = 1
        presenter.voteSettings.isUserCanAddOption = false
        presenter.voteSettings.isCanPreviewResult = false
        presenter.voteSettings.isNeedPassword = false
        presenter.voteSettings.security = VoteData.SECURITY_PUBLIC
        presenter.voteSettings.endTime = System.currentTimeMillis() + 3000 * 86400 * 1000
        `when`(settingFragmentView.getFinalVoteSettings(voteData)).thenReturn(voteData)

        presenter.submitCreateVote()
        verify<ActivityView>(activityView).showLoadingCircle()
        verify(voteDataRepository).createVote(any<VoteData>(), ArgumentMatchers.anyList(), any<File>(), capture(getVoteDataCallbackArgumentCaptor))
        getVoteDataCallbackArgumentCaptor.value.onVoteDataLoaded(voteData)
        verify<ActivityView>(activityView).showHintToast(ArgumentMatchers.anyInt())
        verify<ActivityView>(activityView).hideLoadingCircle()
        verify<ActivityView>(activityView).IntentToVoteDetail(voteData)
    }

    @Test
    fun submitCreateVoteRemoteFailureAndShowErrorToast() {
        presenter = CreateVoteActivityPresenter(voteDataRepository, userDataRepository, activityView, optionFragmentView, settingFragmentView)
        presenter.voteSettings = voteData

        optionList.clear()
        for (i in 0..1) {
            val option = Option()
            option.title = "OPTION_CODE_$i"
            option.id = i.toLong()
            option.count = 0
            optionList.add(option)
        }
        presenter.optionList = optionList
        presenter.voteSettings.title = "TITLE_1"
        presenter.voteSettings.authorCode = "AUTHOR_1"
        presenter.voteSettings.maxOption = 1
        presenter.voteSettings.minOption = 1
        presenter.voteSettings.isUserCanAddOption = false
        presenter.voteSettings.isCanPreviewResult = false
        presenter.voteSettings.isNeedPassword = false
        presenter.voteSettings.security = VoteData.SECURITY_PUBLIC
        presenter.voteSettings.endTime = System.currentTimeMillis() + 3000 * 86400 * 1000
        `when`(settingFragmentView.getFinalVoteSettings(voteData)).thenReturn(voteData)

        presenter.submitCreateVote()
        verify<ActivityView>(activityView).showLoadingCircle()
        verify(voteDataRepository).createVote(any<VoteData>(), ArgumentMatchers.anyList()
                , any<File>(), capture(getVoteDataCallbackArgumentCaptor))
        getVoteDataCallbackArgumentCaptor.value.onVoteDataNotAvailable()
        verify<ActivityView>(activityView).showHintToast(ArgumentMatchers.anyInt())
        verify<ActivityView>(activityView).hideLoadingCircle()
        verify<ActivityView>(activityView, never()).IntentToVoteDetail(voteData)
    }

    @Test
    fun submitCreateVoteLocalFailureAndShowErrorDialog() {
        presenter = CreateVoteActivityPresenter(voteDataRepository, userDataRepository, activityView, optionFragmentView, settingFragmentView)
        presenter.voteSettings = voteData

        for (i in 0..1) {
            val option = Option()
            option.title = "OPTION_CODE_$i"
            option.id = i.toLong()
            option.count = 0
            optionList.add(option)
        }
        presenter.optionList = optionList
        //Only no title case
        presenter.voteSettings.title = ""
        presenter.voteSettings.authorCode = "AUTHOR_1"
        presenter.voteSettings.maxOption = 1
        presenter.voteSettings.minOption = 1
        presenter.voteSettings.isUserCanAddOption = false
        presenter.voteSettings.isCanPreviewResult = false
        presenter.voteSettings.isNeedPassword = false
        presenter.voteSettings.security = VoteData.SECURITY_PUBLIC
        presenter.voteSettings.endTime = System.currentTimeMillis() + 3000 * 86400 * 1000
        `when`(settingFragmentView.getFinalVoteSettings(voteData)).thenReturn(voteData)

        presenter.submitCreateVote()
        verify<ActivityView>(activityView).showLoadingCircle()
        verify(voteDataRepository, never()).createVote(any<VoteData>(), ArgumentMatchers.anyList()
                , any<File>(), capture(getVoteDataCallbackArgumentCaptor))
        verify<ActivityView>(activityView).hideLoadingCircle()
        verify<ActivityView>(activityView).showCreateVoteError(ArgumentMatchers.anyMap())
    }

    @Test
    fun addAndReviseAndRemoveOptionUpdateToView() {
        presenter = CreateVoteActivityPresenter(voteDataRepository
                , userDataRepository, activityView, optionFragmentView, settingFragmentView)
        val optionId = presenter.addNewOption()
        presenter.addNewOption()
        presenter.addNewOption()
        presenter.reviseOption(optionId, "newText")
        var equalText = ""
        for (option in presenter.optionList) {
            if (option.id == optionId) {
                equalText = option.title
            }
        }
        Assert.assertEquals(equalText, "newText")
        verify<OptionFragmentView>(optionFragmentView, times(3)).refreshOptions()
        val optionNumber = presenter.optionList.size
        presenter.removeOption(optionId)
        verify<OptionFragmentView>(optionFragmentView, times(4)).refreshOptions()
        Assert.assertEquals(presenter.optionList.size, optionNumber - 1)
    }

    @Test
    fun updateVoteSecurityAndEndTimeAndTitleAndUpdateToView() {
        presenter = CreateVoteActivityPresenter(voteDataRepository, userDataRepository, activityView, optionFragmentView, settingFragmentView)
        presenter.updateVoteSecurity(VoteData.SECURITY_PRIVATE)
        Assert.assertEquals(presenter.voteSettings.security, VoteData.SECURITY_PRIVATE)
        presenter.updateVoteEndTime(System.currentTimeMillis() - CreateVoteActivityPresenter.DEFAULT_END_TIME * 1000 * 86400)
        verify<ActivityView>(activityView).showHintToast(ArgumentMatchers.anyInt())
        presenter.updateVoteEndTime(System.currentTimeMillis() + (CreateVoteActivityPresenter.DEFAULT_END_TIME_MAX + 10) * 1000 * 86400)
        verify<ActivityView>(activityView).showHintToast(ArgumentMatchers.anyInt())
        presenter.updateVoteEndTime(System.currentTimeMillis() + CreateVoteActivityPresenter.DEFAULT_END_TIME_MAX * 1000 * 86400)
        verify<SettingFragmentView>(settingFragmentView).setUpVoteSettings(any<VoteData>())
        presenter.updateVoteImage(File("test"))
        Assert.assertNotNull(presenter.voteSettings.imageFile)
        presenter.updateVoteTitle("title")
        Assert.assertEquals(presenter.voteSettings.title, "title")
    }

    companion object {
        private lateinit var user: User
        private val voteCode = "CODE_123"
        private val voteData = VoteData()
        private val optionList = ArrayList<Option>()
    }
}
