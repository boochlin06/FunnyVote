package com.heaton.funnyvote.ui.createvote

import android.net.Uri

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData

import java.io.File

interface CreateVoteContract {
    interface Presenter : BasePresenter {
        fun submitCreateVote()

        fun addNewOption(): Long

        fun removeOption(optionId: Long)

        fun reviseOption(optionId: Long, optionText: String)

        fun setActivityView(view: ActivityView)

        fun setOptionFragmentView(view: OptionFragmentView)

        fun setSettingFragmentView(view: SettingFragmentView)

        fun updateVoteSecurity(security: String)

        fun updateVoteEndTime(timeInMill: Long)

        fun updateVoteImage(image: File)

        fun updateVoteTitle(title: String)

    }

    interface ActivityView : BaseView<Presenter> {
        fun showExitCheckDialog()

        fun showLoadingCircle()

        fun hideLoadingCircle()

        fun showCreateVoteError(errorMap: Map<String, Boolean>)

        fun showHintToast(res: Int)

        fun showHintToast(res: Int, arg: Long)

        fun IntentToVoteDetail(voteData: VoteData)
    }

    interface SettingFragmentView : BaseView<Presenter> {
        fun setUpVoteSettings(voteSettings: VoteData)

        fun updateUserSetting(user: User)

        fun getFinalVoteSettings(oldVoteData: VoteData): VoteData

        fun updateSwtNeedPwd(isChecked: Boolean)
    }

    interface OptionFragmentView : BaseView<Presenter> {
        fun setUpOptionAdapter(optionList: List<Option>)

        fun refreshOptions()

        fun setVoteImage(imageUri: Uri)

    }
}
