package com.heaton.funnyvote.ui.votedetail

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData

interface VoteDetailContract {
    interface View : BaseView<Presenter> {

        val isPasswordDialogShowing: Boolean
        fun showLoadingCircle()

        fun hideLoadingCircle()

        fun showSortOptionDialog(data: VoteData)

        fun showPollPasswordDialog()

        fun hidePollPasswordDialog()

        fun shakePollPasswordDialog()

        fun shakeAddNewOptionPasswordDialog()

        fun showAddNewOptionPasswordDialog(newOptionText: String)

        fun hideAddNewOptionPasswordDialog()

        fun showResultOption(optionType: Int)

        fun showUnPollOption(optionType: Int)

        fun showExitCheckDialog()

        fun showVoteInfoDialog(data: VoteData)

        fun showTitleDetailDialog(data: VoteData)

        fun showCaseView()

        fun updateFavoriteView(isFavorite: Boolean)

        fun setUpAdMob(user: User)

        fun setUpViews(voteData: VoteData, optionType: Int)

        fun setUpSubmit(optionType: Int)

        fun setUpOptionAdapter(data: VoteData, optionType: Int, optionList: List<Option>)

        fun showHintToast(res: Int)

        fun showMultiChoiceToast(max: Int, min: Int)

        fun showMultiChoiceAtLeast(min: Int)

        fun showMultiChoiceOverMaxToast(max: Int)

        fun refreshOptions()

        fun updateChoiceOptions(choiceList: List<Long>)

        fun updateCurrentOptionsOrder(optionList: List<Option>)

        fun updateExpandOptions(expandList: List<String>)

        fun showShareDialog(data: VoteData)

        fun showAuthorDetail(data: VoteData)

        fun moveToTop()

        fun updateSearchView(searchList: List<Option>, isSearchMode: Boolean)

    }

    interface Presenter : BasePresenter {
        fun searchOption(newText: String)

        fun favoriteVote()

        fun pollVote(password: String)

        fun resetOptionChoiceStatus(optionId: Long, optionCode: String)

        fun resetOptionExpandStatus(optionCode: String)

        fun addNewOptionStart()

        fun addNewOptionContentRevise(optionId: Long, inputText: String)

        fun addNewOptionCompleted(password: String, newOptionText: String?)

        fun removeOption(optionId: Long)

        fun IntentToVoteInfo()

        fun IntentToTitleDetail()

        fun IntentToShareDialog()

        fun IntentToAuthorDetail()

        fun changeOptionType()

        fun sortOptions(sortType: Int)

        fun CheckSortOptionType()

    }
}
