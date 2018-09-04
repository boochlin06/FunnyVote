package com.heaton.funnyvote.ui.votedetail

import android.text.TextUtils
import android.util.Log
import com.google.common.base.Strings
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import java.util.*

class VoteDetailPresenter(
        private val voteId: String?,
        private val voteDataRepository: VoteDataRepository?,
        private val userDataRepository: UserDataRepository?,
        private val view: VoteDetailContract.View
) : VoteDetailContract.Presenter {
    private var user: User = User()

    var voteData = VoteData()

    private var optionList: MutableList<Option> = arrayListOf()
    private val searchList: List<Option>
    private val choiceList: MutableList<Long>
    private val choiceCodeList: MutableList<String>
    private val expandOptionList: MutableList<String>

    var isMultiChoice = false
    var isUserPreResult = false
    var isUserOnAddNewOption = false
    var isSearchMode = false
    var optionType = OptionItemAdapter.OPTION_UNPOLL
    // all new option id is negative auto increment.
    private var newOptionIdAuto: Long = -1

    private val currentList: List<Option>?
        get() = if (isSearchMode) {
            searchList
        } else optionList

    init {
        this.optionList = ArrayList()
        this.choiceList = ArrayList()
        this.choiceCodeList = ArrayList()
        this.expandOptionList = ArrayList()
        this.searchList = ArrayList()
        this.view.setPresenter(this)
    }

    override fun searchOption(newText: String) {
        val searchList = ArrayList<Option>()
        isSearchMode = if (newText.isNotEmpty()) {
            for (i in optionList.indices) {
                if (optionList[i].title.contains(newText)) {
                    searchList.add(optionList[i])
                }
            }
            true
        } else {
            false
        }
        view.updateSearchView(searchList, isSearchMode)
    }

    override fun favoriteVote() {
        voteDataRepository!!.favoriteVote(voteData.voteCode, !voteData.isFavorite, user, object : VoteDataSource.FavoriteVoteCallback {
            override fun onSuccess(isFavorite: Boolean) {
                voteData.isFavorite = isFavorite
                view.updateFavoriteView(isFavorite)
                if (voteData.isFavorite) {
                    view.showHintToast(R.string.vote_detail_toast_add_favorite)
                } else {
                    view.showHintToast(R.string.vote_detail_toast_remove_favorite)
                }
            }

            override fun onFailure() {
                view.showHintToast(R.string.toast_network_connect_error)
            }
        })
    }

    override fun pollVote(password: String) {
        if (choiceCodeList.size < voteData.minOption) {
            view.showMultiChoiceAtLeast(voteData.minOption)
        } else if (choiceCodeList.size > voteData.maxOption) {
            view.showMultiChoiceOverMaxToast(voteData.maxOption)
        } else if (isUserOnAddNewOption) {
            view.showHintToast(R.string.vote_detail_toast_fill_new_option)
        } else {
            if (voteData.isNeedPassword && !view.isPasswordDialogShowing) {
                view.showPollPasswordDialog()
            } else {
                view.showLoadingCircle()
                voteDataRepository!!.pollVote(voteData.voteCode, password, choiceCodeList, user, object : VoteDataSource.PollVoteCallback {
                    override fun onSuccess(voteData: VoteData) {
                        view.hideLoadingCircle()
                        this@VoteDetailPresenter.voteData = voteData
                        this@VoteDetailPresenter.optionList = voteData.netOptions
                        checkCurrentOptionType()
                        view.setUpViews(this@VoteDetailPresenter.voteData, optionType)
                        view.setUpOptionAdapter(this@VoteDetailPresenter.voteData, optionType, optionList)
                        view.setUpSubmit(optionType)
                        view.refreshOptions()
                        view.hidePollPasswordDialog()
                    }

                    override fun onFailure() {
                        view.showHintToast(R.string.toast_network_connect_error_quick_poll)
                        view.hidePollPasswordDialog()
                        view.hideLoadingCircle()
                    }

                    override fun onPasswordInvalid() {
                        view.shakePollPasswordDialog()
                        view.hideLoadingCircle()
                        view.showHintToast(R.string.vote_detail_dialog_password_toast_retry)
                    }
                })
            }
        }
    }

    override fun resetOptionChoiceStatus(optionId: Long, optionCode: String) {
        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
            return
        }
        if (!isMultiChoice) {
            choiceList.clear()
            choiceList.add(optionId)
            choiceCodeList.clear()
            choiceCodeList.add(optionCode)
            view.updateChoiceOptions(choiceList)
        } else {
            if (choiceList.contains(optionId)) {
                choiceList.removeAt(choiceList
                        .indexOf(optionId))
                choiceCodeList.remove(optionCode)
                view.updateChoiceOptions(choiceList)
            } else {
                if (choiceList.size < voteData.maxOption) {
                    choiceList.add(optionId)
                    choiceCodeList.add(optionCode)
                    view.updateChoiceOptions(choiceList)
                } else {
                    view.showMultiChoiceOverMaxToast(voteData.maxOption)
                }
            }
        }
    }

    override fun resetOptionExpandStatus(optionCode: String) {
        if (expandOptionList.contains(optionCode)) {
            expandOptionList.removeAt(expandOptionList
                    .indexOf(optionCode))
        } else {
            expandOptionList.add(optionCode)
        }
        view.updateExpandOptions(expandOptionList)
    }

    override fun addNewOptionStart() {
        if (isUserOnAddNewOption) {
            view.showHintToast(R.string.vote_detail_toast_confirm_new_option)
        } else {
            isUserOnAddNewOption = true
            val option = Option()
            option.count = 0
            option.id = newOptionIdAuto--
            option.code = "add$newOptionIdAuto"
            optionList.add(option)
            view.refreshOptions()
        }
    }

    override fun addNewOptionContentRevise(optionId: Long, inputText: String) {
        var targetPosition = -1
        for (i in optionList.indices) {
            if (optionList[i].id == optionId) {
                targetPosition = i
                break
            }
        }
        if (targetPosition >= 0) {
            optionList[targetPosition].title = inputText
        }
    }

    override fun addNewOptionCompleted(password: String, newOptionText: String?) {
        if (newOptionText != null && !TextUtils.isEmpty(newOptionText)) {
            if (voteData.isNeedPassword && !view.isPasswordDialogShowing) {
                view.showAddNewOptionPasswordDialog(newOptionText)
            } else {
                view.showLoadingCircle()
                val newOptions = ArrayList<String>()
                newOptions.add(newOptionText)
                voteDataRepository!!.addNewOption(voteData.voteCode, password, newOptions, user, object : VoteDataSource.AddNewOptionCallback {
                    override fun onSuccess(voteData: VoteData) {
                        Log.e(TAG, "onSuccess")
                        isUserOnAddNewOption = false
                        view.hideLoadingCircle()
                        this@VoteDetailPresenter.voteData = voteData
                        this@VoteDetailPresenter.optionList = voteData.netOptions
                        checkCurrentOptionType()
                        view.setUpViews(this@VoteDetailPresenter.voteData, optionType)
                        view.setUpOptionAdapter(this@VoteDetailPresenter.voteData, optionType, optionList)
                        view.setUpSubmit(optionType)
                        view.refreshOptions()
                        view.hideAddNewOptionPasswordDialog()
                    }

                    override fun onFailure() {
                        Log.e(TAG, "onFailure")
                        view.showHintToast(R.string.toast_network_connect_error_quick_poll)
                        view.hideAddNewOptionPasswordDialog()
                        view.hideLoadingCircle()
                    }

                    override fun onPasswordInvalid() {
                        Log.e(TAG, "onPasswordInvalid")
                        view.shakeAddNewOptionPasswordDialog()
                        view.showHintToast(R.string.vote_detail_dialog_password_toast_retry)
                        view.hideLoadingCircle()
                    }
                })
            }
        } else {
            view.showHintToast(R.string.vote_detail_toast_fill_new_option)
        }
    }

    override fun removeOption(optionId: Long) {
        isUserOnAddNewOption = false
        var removePosition = -1
        for (i in optionList.indices) {
            if (optionList[i].id == optionId) {
                removePosition = i
                break
            }
        }
        if (removePosition >= 0) {
            optionList.removeAt(removePosition)
        }
        view.refreshOptions()
    }

    override fun IntentToVoteInfo() {
        view.showVoteInfoDialog(voteData)
    }

    override fun IntentToTitleDetail() {
        view.showTitleDetailDialog(voteData)
    }

    override fun IntentToShareDialog() {
        view.showShareDialog(voteData)
    }

    override fun IntentToAuthorDetail() {
        view.showAuthorDetail(voteData)
    }

    override fun changeOptionType() {
        isUserPreResult = !isUserPreResult
        if (isUserPreResult) {
            optionType = OptionItemAdapter.OPTION_SHOW_RESULT
            view.showResultOption(optionType)
        } else {
            optionType = OptionItemAdapter.OPTION_UNPOLL
            view.showUnPollOption(optionType)
        }
        view.setUpSubmit(optionType)
    }

    override fun sortOptions(sortType: Int) {
        var comparator: Comparator<Option>? = null
        when (sortType) {
            0 -> comparator = Comparator { option1, option2 ->
                // TODO:Add user add new option case id compare.
                if (option1.id < 0 || option2.id < 0) {
                    (Math.abs(option1.id!!) + 100000)
                            .compareTo(Math.abs(option2.id!!) + 100000)
                } else {
                    option1.id!!.compareTo(option2.id)
                }
            }
            1 -> comparator = Comparator { option1, option2 -> option1.title.compareTo(option2.title) }
            2 -> comparator = Comparator { option1, option2 -> option2.count!!.compareTo(option1.count) }
        }
        Collections.sort(currentList!!, comparator)
        if (!isSearchMode) {
            Collections.sort(optionList, comparator)
        }
        view.updateCurrentOptionsOrder(optionList)
    }

    override fun CheckSortOptionType() {
        view.showSortOptionDialog(voteData)
    }

    override fun start() {
        openVoteData()
    }

    private fun openVoteData() {
        if (Strings.isNullOrEmpty(voteId)) {
            return
        }
        checkCurrentOptionType()
        view.showLoadingCircle()
        userDataRepository!!.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                this@VoteDetailPresenter.user = user
                view.setUpAdMob(user)
                voteDataRepository!!.getVoteData(voteId!!, user, object : VoteDataSource.GetVoteDataCallback {
                    override fun onVoteDataLoaded(voteData: VoteData) {
                        this@VoteDetailPresenter.voteData = voteData
                        this@VoteDetailPresenter.optionList = voteData.netOptions
                        checkCurrentOptionType()
                        view.setUpViews(voteData, optionType)
                        view.setUpSubmit(optionType)
                        if (optionType == OptionItemAdapter.OPTION_UNPOLL) {
                            view.showCaseView()
                        }
                        view.hideLoadingCircle()
                        voteDataRepository.getOptions(voteData, object : VoteDataSource.GetVoteOptionsCallback {
                            override fun onVoteOptionsLoaded(optionList: List<Option>) {
                                this@VoteDetailPresenter.optionList = optionList.toMutableList();
                                view.setUpOptionAdapter(this@VoteDetailPresenter.voteData, optionType, optionList)
                                if (this@VoteDetailPresenter.voteData.endTime > System.currentTimeMillis() && !this@VoteDetailPresenter.voteData.isPolled && this@VoteDetailPresenter.voteData.isMultiChoice) {
                                    view.showMultiChoiceToast(this@VoteDetailPresenter.voteData.maxOption, this@VoteDetailPresenter.voteData.minOption)
                                } else if (this@VoteDetailPresenter.voteData.endTime < System.currentTimeMillis()) {
                                    if (this@VoteDetailPresenter.voteData.isPolled) {
                                        view.showHintToast(R.string.vote_detail_toast_vote_end_polled)
                                    } else {
                                        view.showHintToast(R.string.vote_detail_toast_vote_end_not_poll)
                                    }
                                }
                            }

                            override fun onVoteOptionsNotAvailable() {
                                view.showHintToast(R.string.create_vote_toast_create_fail)
                            }
                        })

                    }

                    override fun onVoteDataNotAvailable() {
                        view.hideLoadingCircle()
                        view.showHintToast(R.string.create_vote_toast_create_fail)
                    }
                })
            }

            override fun onFailure() {
                view.showHintToast(R.string.create_vote_toast_create_fail)
            }
        }, false)
    }

    private fun checkCurrentOptionType() {
        if (this.voteData.endTime < System.currentTimeMillis()
                || voteData.isPolled || isUserPreResult) {
            this.optionType = OptionItemAdapter.OPTION_SHOW_RESULT
        } else {
            this.optionType = OptionItemAdapter.OPTION_UNPOLL
        }
        this.isMultiChoice = voteData.isMultiChoice
    }

    companion object {

        private val TAG = VoteDetailPresenter::class.java.simpleName
    }

}
