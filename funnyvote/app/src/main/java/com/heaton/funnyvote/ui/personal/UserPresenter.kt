package com.heaton.funnyvote.ui.personal

import android.util.Log
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.main.MainPageContract
import com.heaton.funnyvote.ui.main.MainPagePresenter
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import java.util.*

class UserPresenter(
        private val voteDataRepository: VoteDataRepository,
        private val userDataRepository: UserDataRepository,
        private val userPageView: PersonalContract.UserPageView
) : PersonalContract.Presenter {
    private var createFragment: MainPageContract.TabPageFragment? = null
    private var participateFragment: MainPageContract.TabPageFragment? = null
    private var favoriteFragment: MainPageContract.TabPageFragment? = null

    private var createVoteDataList: MutableList<VoteData>
    private var participateVoteDataList: MutableList<VoteData>
    private var favoriteVoteDataList: MutableList<VoteData>

    var loginUser: User = User()
    private var targetUser: User = User()

    fun getCreateVoteDataList(): List<VoteData>? {
        return createVoteDataList
    }

    fun setCreateVoteDataList(createVoteDataList: MutableList<VoteData>) {
        this.createVoteDataList = createVoteDataList
    }

    fun getParticipateVoteDataList(): List<VoteData>? {
        return participateVoteDataList
    }

    fun setParticipateVoteDataList(participateVoteDataList: MutableList<VoteData>) {
        this.participateVoteDataList = participateVoteDataList
    }

    fun getFavoriteVoteDataList(): List<VoteData>? {
        return favoriteVoteDataList
    }

    fun setFavoriteVoteDataList(favoriteVoteDataList: MutableList<VoteData>) {
        this.favoriteVoteDataList = favoriteVoteDataList
    }

    fun getTargetUser(): User? {
        return targetUser
    }

    init {
        createVoteDataList = ArrayList()
        participateVoteDataList = ArrayList()
        favoriteVoteDataList = ArrayList()
        this.userPageView.setPresenter(this)
    }

    override fun resetPromotion() {

    }

    override fun setHotsFragmentView(hotsFragmentView: MainPageContract.TabPageFragment) {

    }

    override fun setNewsFragmentView(newsFragmentView: MainPageContract.TabPageFragment) {

    }

    override fun setCreateFragmentView(fragmentView: MainPageContract.TabPageFragment) {
        this.createFragment = fragmentView
        reloadCreateList(0)
        createFragment!!.setTab(MainPageTabFragment.TAB_CREATE)
        createFragment!!.setUpRecycleView(createVoteDataList.toList())
    }

    override fun setParticipateFragmentView(fragmentView: MainPageContract.TabPageFragment) {
        this.participateFragment = fragmentView
        reloadParticipateList(0)
        participateFragment!!.setTab(MainPageTabFragment.TAB_PARTICIPATE)
        participateFragment!!.setUpRecycleView(participateVoteDataList.toList())
    }

    override fun setFavoriteFragmentView(fragmentView: MainPageContract.TabPageFragment) {
        this.favoriteFragment = fragmentView
        reloadFavoriteList(0)
        favoriteFragment!!.setTab(MainPageTabFragment.TAB_FAVORITE)
        favoriteFragment!!.setUpRecycleView(favoriteVoteDataList.toList())
    }

    override fun favoriteVote(voteData: VoteData) {
        voteDataRepository.favoriteVote(voteData.voteCode, voteData.isFavorite, loginUser
                , object : VoteDataSource.FavoriteVoteCallback {
            override fun onSuccess(isFavorite: Boolean) {
                voteData.isFavorite = isFavorite
                updateVoteDataToAllList(voteData)
                refreshAllFragment()
                if (voteData.isFavorite) {
                    userPageView.showHintToast(R.string.vote_detail_toast_add_favorite, 0)
                } else {
                    userPageView.showHintToast(R.string.vote_detail_toast_remove_favorite, 0)
                }
            }

            override fun onFailure() {
                userPageView.showHintToast(R.string.toast_network_connect_error_favorite, 0)
            }
        })
    }


    override fun IntentToShareDialog(voteData: VoteData) {
        userPageView.showShareDialog(voteData)
    }

    override fun IntentToCreateVote() {
        userPageView.showCreateVote()
    }

    override fun IntentToAuthorDetail(voteData: VoteData) {
        userPageView.showAuthorDetail(voteData)
    }

    override fun IntentToVoteDetail(voteData: VoteData) {
        userPageView.showVoteDetail(voteData)
    }

    override fun pollVote(voteData: VoteData, optionCode: String, password: String) {
        if (voteData.isNeedPassword && !userPageView.isPasswordDialogShowing) {
            userPageView.showPollPasswordDialog(voteData, optionCode)
        } else {
            userPageView.showLoadingCircle()
            val choiceCodeList = ArrayList<String>()
            choiceCodeList.add(optionCode)
            voteDataRepository.pollVote(voteData.voteCode, password, choiceCodeList, loginUser, object : VoteDataSource.PollVoteCallback {
                override fun onSuccess(voteData: VoteData) {
                    userPageView.hideLoadingCircle()
                    updateVoteDataToAllList(voteData)
                    refreshAllFragment()
                }

                override fun onFailure() {
                    userPageView.showHintToast(R.string.toast_network_connect_error_quick_poll, 0)
                    userPageView.hidePollPasswordDialog()
                    userPageView.hideLoadingCircle()
                }

                override fun onPasswordInvalid() {
                    userPageView.shakePollPasswordDialog()
                    userPageView.hideLoadingCircle()
                    userPageView.showHintToast(R.string.vote_detail_dialog_password_toast_retry, 0)
                }
            })
        }
    }

    override fun reloadHotList(offset: Int) {

    }

    override fun reloadNewList(offset: Int) {

    }

    override fun refreshNewList() {

    }

    override fun refreshHotList() {

    }

    private fun updateVoteDataToList(voteDataList: MutableList<VoteData>, updateData: VoteData) {
        for (i in voteDataList.indices) {
            if (updateData.voteCode == voteDataList[i].voteCode) {
                voteDataList[i] = updateData
                break
            }
        }
    }

    private fun updateVoteDataToAllList(updateData: VoteData) {
        updateVoteDataToList(createVoteDataList, updateData)
        updateVoteDataToList(favoriteVoteDataList, updateData)
        updateVoteDataToList(participateVoteDataList, updateData)
    }

    override fun refreshAllFragment() {
        if (createFragment != null)
            createFragment!!.refreshFragment(createVoteDataList)
        if (participateFragment != null)
            participateFragment!!.refreshFragment(participateVoteDataList)
        if (favoriteFragment != null)
            favoriteFragment!!.refreshFragment(favoriteVoteDataList)
    }

    override fun reloadCreateList(offset: Int) {
        if (loginUser.userCode.isNullOrEmpty()) {
            start()
            return
        }
        voteDataRepository.getCreateVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                updateCreateList(voteDataList.toMutableList(), offset)
                createFragment!!.refreshFragment(createVoteDataList)
                createFragment!!.hideSwipeLoadView()
                userPageView.hideLoadingCircle()
            }

            override fun onVoteListNotAvailable() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
                userPageView.hideLoadingCircle()
                createFragment!!.hideSwipeLoadView()
            }
        })
    }

    override fun reloadParticipateList(offset: Int) {
        if (loginUser.userCode.isNullOrEmpty()) {
            start()
            return
        }
        voteDataRepository.getParticipateVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                updateParticipateList(voteDataList.toMutableList(), offset)
                participateFragment!!.refreshFragment(participateVoteDataList.toList())
                participateFragment!!.hideSwipeLoadView()
                userPageView.hideLoadingCircle()
            }

            override fun onVoteListNotAvailable() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
                userPageView.hideLoadingCircle()
                participateFragment!!.hideSwipeLoadView()
            }
        })
    }

    override fun reloadFavoriteList(offset: Int) {
        if (loginUser.userCode.isNullOrEmpty()) {
            start()
            return
        }
        voteDataRepository.getFavoriteVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                updateFavoriteList(voteDataList.toMutableList(), offset)
                favoriteFragment!!.refreshFragment(favoriteVoteDataList)
                favoriteFragment!!.hideSwipeLoadView()
                userPageView.hideLoadingCircle()
            }

            override fun onVoteListNotAvailable() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
                userPageView.hideLoadingCircle()
                favoriteFragment!!.hideSwipeLoadView()
            }
        })
    }

    private fun updateFavoriteList(voteDataList: MutableList<VoteData>, offset: Int) {
        val pageNumber = offset / LIMIT
        if (offset == 0) {
            this.favoriteVoteDataList = voteDataList
        } else if (offset >= this.favoriteVoteDataList.size) {
            this.favoriteVoteDataList.addAll(voteDataList)
        }
        Log.d(TAG, "favoriteVoteDataList:" + favoriteVoteDataList.size + ",offset :" + offset)
        if (this.favoriteVoteDataList.size < LIMIT * (pageNumber + 1)) {
            favoriteFragment!!.setMaxCount(this.favoriteVoteDataList.size)
            if (offset != 0) {
                userPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0)
            }
        } else {
            favoriteFragment!!.setMaxCount(LIMIT * (pageNumber + 2))
        }
    }

    private fun updateCreateList(voteDataList: MutableList<VoteData>, offset: Int) {
        val pageNumber = offset / LIMIT
        if (offset == 0) {
            this.createVoteDataList = voteDataList
        } else if (offset >= this.createVoteDataList.size) {
            this.createVoteDataList.addAll(voteDataList)
        }
        Log.d(TAG, "createVoteDataList:" + favoriteVoteDataList.size + ",offset :" + offset)
        if (this.createVoteDataList.size < LIMIT * (pageNumber + 1)) {
            createFragment!!.setMaxCount(this.favoriteVoteDataList.size)
            if (offset != 0) {
                userPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0)
            }
        } else {
            createFragment!!.setMaxCount(LIMIT * (pageNumber + 2))
        }
    }

    private fun updateParticipateList(voteDataList: MutableList<VoteData>, offset: Int) {
        val pageNumber = offset / LIMIT
        if (offset == 0) {
            this.participateVoteDataList = voteDataList
        } else if (offset >= this.participateVoteDataList.size) {
            this.participateVoteDataList.addAll(voteDataList)
        }
        Log.d(TAG, "participateVoteDataList:" + participateVoteDataList.size + ",offset :" + offset)
        if (this.participateVoteDataList.size < LIMIT * (pageNumber + 1)) {
            participateFragment!!.setMaxCount(this.participateVoteDataList.size)
            if (offset != 0) {
                userPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0)
            }
        } else {
            participateFragment!!.setMaxCount(LIMIT * (pageNumber + 2))
        }
    }

    override fun refreshCreateList() {
        reloadCreateList(createVoteDataList.size)
    }

    override fun refreshParticipateList() {
        reloadParticipateList(participateVoteDataList.size)
    }

    override fun refreshFavoriteList() {
        reloadFavoriteList(favoriteVoteDataList.size)
    }

    override fun setTargetUser(targetUser: User) {
        this.targetUser = targetUser
    }

    override fun start() {
        userPageView.showLoadingCircle()
        userPageView.showIntroductionDialog()
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                this@UserPresenter.loginUser = user
                Log.d(TAG, "getUserCallback loginUser:" + user.type)
                userPageView.setUpUserView(if (targetUser.userCode.isNullOrEmpty()) user else targetUser)
                userPageView.setUpTabsAdapter(user, targetUser)
                reloadAllList(0)
            }

            override fun onFailure() {
                userPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
                userPageView.setUpTabsAdapter(User(), targetUser)
                userPageView.hideLoadingCircle()
                Log.d(TAG, "getUserCallback loginUser failure:$loginUser")
            }
        }, false)
    }

    private fun reloadAllList(offset: Int) {
        if (createFragment != null)
            reloadCreateList(offset)
        if (participateFragment != null)
            reloadParticipateList(offset)
        if (favoriteFragment != null)
            reloadFavoriteList(offset)
    }

    companion object {
        var TAG = MainPagePresenter::class.java.simpleName
        private val LIMIT = VoteDataRepository.PAGE_COUNT
    }

}
