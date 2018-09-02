package com.heaton.funnyvote.ui.createvote

import android.text.TextUtils
import android.util.Log
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import java.io.File
import java.util.*

class CreateVoteActivityPresenter(
        private val voteDataRepository: VoteDataRepository
        , private val userDataRepository: UserDataRepository
        , private var activityView: CreateVoteContract.ActivityView
        , private var optionFragmentView: CreateVoteContract.OptionFragmentView?
        , private var settingFragmentView: CreateVoteContract.SettingFragmentView?
) : CreateVoteContract.Presenter {
    private lateinit var errorCheckMap: MutableMap<String, Boolean>


    var optionList: MutableList<Option> = ArrayList()


    var voteSettings: VoteData
    private lateinit var user: User

    private var newOptionIdAuto: Long = 2

    init {
        this.voteSettings = VoteData()
        this.activityView.setPresenter(this)
    }

    override fun start() {
        for (i in 0..1) {
            val option = Option()
            option.id = newOptionIdAuto
            option.count = 0
            optionList.add(option)
            newOptionIdAuto++
        }

        voteSettings = VoteData()
        voteSettings.maxOption = 1
        voteSettings.minOption = 1
        voteSettings.isUserCanAddOption = false
        voteSettings.isCanPreviewResult = false
        voteSettings.isNeedPassword = false
        voteSettings.security = VoteData.SECURITY_PUBLIC
        voteSettings.endTime = System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                this@CreateVoteActivityPresenter.user = user
                voteSettings.author = user
                val name = user.userName
                val code = user.userCode
                val icon = user.userIcon
                voteSettings.authorName = name
                voteSettings.authorCode = code
                voteSettings.authorIcon = icon
            }

            override fun onFailure() {

            }
        }, false)
    }


    override fun submitCreateVote() {
        activityView.showLoadingCircle()
        errorCheckMap = HashMap()
        voteSettings = settingFragmentView!!.getFinalVoteSettings(voteSettings)
        voteSettings.startTime = System.currentTimeMillis()
        val optionTitles = ArrayList<String>()
        var errorNumber = 0
        val optionCount = optionList.size
        for (i in 0 until optionList.size) {
            if (optionList[i].title == null || optionList[i].title.isEmpty()) {
                errorNumber++
                errorCheckMap[ERROR_FILL_ALL_OPTION] = true
                break
            }
        }
        for (i in 0 until optionList.size) {
            if (optionTitles.contains(optionList[i].title)) {
                errorNumber++
                errorCheckMap[ERROR_OPTION_DUPLICATE] = true
                break
            } else {
                optionTitles.add(optionList[i].title)
                Log.d(TAG, "option " + i + " title:" + optionTitles[i])
            }
        }
        if (voteSettings.title == null || voteSettings.title.isEmpty()) {
            errorNumber++
            errorCheckMap[ERROR_TITLE_EMPTY] = true
        }
        if (voteSettings.authorCode == null || TextUtils.isEmpty(voteSettings.authorCode)) {
            errorNumber++
            errorCheckMap[ERROR_USER_CODE_ERROR] = true
        }
        if (voteSettings.maxOption == 0) {
            errorNumber++
            errorCheckMap[ERROR_OPTION_MAX_0] = true
        }
        if (voteSettings.minOption == 0) {
            errorNumber++
            errorCheckMap[ERROR_OPTION_MIN_0] = true
        }
        if (voteSettings.maxOption < voteSettings.minOption) {
            errorNumber++
            errorCheckMap[ERROR_OPTION_MAX_SAMLL_THAN_MIN] = true
        }
        if (voteSettings.maxOption > optionCount) {
            errorNumber++
            errorCheckMap[ERROR_OPTION_MAX_SMALL_THAN_TOTAL] = true
        }
        if (voteSettings.endTime < System.currentTimeMillis()) {
            errorNumber++
            errorCheckMap[ERROR_ENDTIME_MORE_THAN_NOW] = true
        }
        if (voteSettings.isNeedPassword && voteSettings.password.isEmpty()) {
            errorNumber++
            errorCheckMap[ERROR_PASSWORD_EMPTY] = true
        }
        Log.d(TAG, "ERROR NUMBER:$errorNumber")
        if (errorNumber == 0) {
            voteDataRepository.createVote(voteSettings, optionTitles, voteSettings.imageFile, object : VoteDataSource.GetVoteDataCallback {
                override fun onVoteDataLoaded(voteData: VoteData) {
                    activityView.showHintToast(R.string.create_vote_create_successful)
                    activityView.hideLoadingCircle()
                    activityView.IntentToVoteDetail(voteData)
                    Log.d(TAG, "create vote success:" + voteData.voteCode
                            + " image:" + voteSettings.voteImage)
                }

                override fun onVoteDataNotAvailable() {
                    activityView.showHintToast(R.string.create_vote_toast_create_fail)
                    activityView.hideLoadingCircle()
                    Log.d(TAG, "create vote false:")
                }
            })
        } else {
            activityView.hideLoadingCircle()
            activityView.showCreateVoteError(errorCheckMap)
        }
    }

    override fun addNewOption(): Long {
        val option = Option()
        option.count = 0
        option.id = newOptionIdAuto++
        optionList.add(option)
        optionFragmentView!!.refreshOptions()
        return option.id
    }

    override fun removeOption(optionId: Long) {
        if (optionList.size <= 2) {
            activityView.showHintToast(R.string.create_vote_toast_less_than_2_option)
            return
        }
        var removePosition = -1
        for (i in 0 until optionList.size) {
            if (optionList[i].id == optionId) {
                removePosition = i
                break
            }
        }
        if (removePosition >= 0) {
            optionList.removeAt(removePosition)
        }
        optionFragmentView!!.refreshOptions()
    }

    override fun reviseOption(optionId: Long, optionText: String) {
        var targetPosition = -1
        for (i in 0 until optionList.size) {
            if (optionList[i].id == optionId) {
                targetPosition = i
                break
            }
        }
        if (targetPosition >= 0) {
            optionList[targetPosition].title = optionText
        }
    }

    override fun setActivityView(view: CreateVoteContract.ActivityView) {
        this.activityView = view
    }

    override fun setOptionFragmentView(view: CreateVoteContract.OptionFragmentView) {
        this.optionFragmentView = view
        optionFragmentView!!.setUpOptionAdapter(optionList)
    }

    override fun setSettingFragmentView(view: CreateVoteContract.SettingFragmentView) {
        this.settingFragmentView = view
        settingFragmentView!!.setUpVoteSettings(voteSettings)
        settingFragmentView!!.updateUserSetting(user)
    }

    override fun updateVoteSecurity(security: String) {
        voteSettings.security = security
    }

    override fun updateVoteEndTime(timeInMill: Long) {
        when {
            timeInMill < System.currentTimeMillis() -> {
                activityView.showHintToast(R.string.create_vote_toast_endtime_more_than_current)
                return
            }
            timeInMill - System.currentTimeMillis() > DEFAULT_END_TIME_MAX * 86400 * 1000 -> {
                activityView.showHintToast(R.string.create_vote_error_hint_endtime_more_than_max, DEFAULT_END_TIME_MAX)
                return
            }
            else -> {
                voteSettings.endTime = timeInMill
                settingFragmentView!!.setUpVoteSettings(voteSettings)
            }
        }
    }

    override fun updateVoteImage(image: File) {
        voteSettings.imageFile = image
    }

    override fun updateVoteTitle(title: String) {
        voteSettings.title = title
    }

    companion object {

        val ERROR_FILL_ALL_OPTION = "ERROR_FILL_ALL_OPTION"
        val ERROR_OPTION_DUPLICATE = "ERROR_OPTION_DUPLICATE"
        val ERROR_USER_CODE_ERROR = "ERROR_USER_CODE_ERROR"
        val ERROR_OPTION_MAX_0 = "ERROR_OPTION_MAX_0"
        val ERROR_OPTION_MIN_0 = "ERROR_OPTION_MIN_0"
        val ERROR_OPTION_MAX_SAMLL_THAN_MIN = "ERROR_OPTION_MAX_SAMLL_THAN_MIN"
        val ERROR_OPTION_MAX_SMALL_THAN_TOTAL = "ERROR_OPTION_MAX_SMALL_THAN_TOTAL"
        val ERROR_TITLE_EMPTY = "ERROR_TITLE_EMPTY"
        val ERROR_PASSWORD_EMPTY = "ERROR_PASSWORD_EMPTY"
        val ERROR_ENDTIME_MORE_THAN_NOW = "ERROR_ENDTIME_MORE_THAN_NOW"
        val ERROR_ENDTIME_MORE_THAN_MAX = "ERROR_ENDTIME_MORE_THAN_MAX"
        val DEFAULT_END_TIME: Long = 30
        val DEFAULT_END_TIME_MAX: Long = 90
        private val TAG = CreateVoteActivityPresenter::class.java.simpleName
    }
}
