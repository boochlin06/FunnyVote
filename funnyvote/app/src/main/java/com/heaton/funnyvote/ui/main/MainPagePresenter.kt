package com.heaton.funnyvote.ui.main

import android.util.Log
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.promotion.PromotionDataSource
import com.heaton.funnyvote.data.promotion.PromotionRepository
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import java.util.*

class MainPagePresenter(
        private val voteDataRepository: VoteDataRepository
        , private val userDataRepository: UserDataRepository
        , private val promotionRepository: PromotionRepository
        , private val mainPageView: MainPageContract.MainPageView
) : MainPageContract.Presenter {
    private var hotsFragment: MainPageContract.TabPageFragment? = null
    private var newsFragment: MainPageContract.TabPageFragment? = null

    private var hotVoteDataList: MutableList<VoteData>
    private var newVoteDataList: MutableList<VoteData>

    private var user: User = User()

    private var promotionList: List<Promotion>? = null

    fun getHotVoteDataList(): List<VoteData>? {
        return hotVoteDataList
    }

    fun setHotVoteDataList(hotVoteDataList: MutableList<VoteData>) {
        this.hotVoteDataList = hotVoteDataList
    }

    fun getNewVoteDataList(): List<VoteData>? {
        return newVoteDataList
    }

    fun setNewVoteDataList(newVoteDataList: MutableList<VoteData>) {
        this.newVoteDataList = newVoteDataList
    }

    fun setUser(user: User) {
        this.user = user
    }

    init {
        hotVoteDataList = ArrayList()
        newVoteDataList = ArrayList()
        mainPageView.setPresenter(this)
    }

    override fun resetPromotion() {
        if (!user.userCode.isNullOrEmpty()) {
            promotionRepository.getPromotionList(user, object : PromotionDataSource.GetPromotionsCallback {
                override fun onPromotionsLoaded(promotionList: List<Promotion>) {
                    this@MainPagePresenter.promotionList = promotionList
                    mainPageView.setupPromotionAdmob(promotionList, user)
                    Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size
                            + ",type list size:")
                }

                override fun onPromotionsNotAvailable() {

                }
            })
        }
    }

    override fun setHotsFragmentView(hotsFragmentView: MainPageContract.TabPageFragment) {
        this.hotsFragment = hotsFragmentView
        hotsFragment!!.setTab(MainPageTabFragment.TAB_HOT)
        hotsFragment!!.setUpRecycleView(hotVoteDataList.toList())
    }

    override fun setNewsFragmentView(newsFragmentView: MainPageContract.TabPageFragment) {
        this.newsFragment = newsFragmentView
        newsFragment!!.setTab(MainPageTabFragment.TAB_NEW)
        newsFragment!!.setUpRecycleView(newVoteDataList.toList())
    }

    override fun setCreateFragmentView(fragmentView: MainPageContract.TabPageFragment) {

    }

    override fun setParticipateFragmentView(fragmentView: MainPageContract.TabPageFragment) {

    }

    override fun setFavoriteFragmentView(fragmentView: MainPageContract.TabPageFragment) {

    }

    override fun favoriteVote(voteData: VoteData) {
        Log.d(TAG, "favoriteVote")
        voteDataRepository.favoriteVote(voteData.voteCode, voteData.isFavorite, user, object : VoteDataSource.FavoriteVoteCallback {
            override fun onSuccess(isFavorite: Boolean) {
                Log.d(TAG, "favoriteVote SUCCESS")
                voteData.isFavorite = isFavorite
                updateVoteDataToList(hotVoteDataList, voteData)
                updateVoteDataToList(newVoteDataList, voteData)
                hotsFragment!!.refreshFragment(hotVoteDataList)
                newsFragment!!.refreshFragment(newVoteDataList)
                if (voteData.isFavorite) {
                    mainPageView.showHintToast(R.string.vote_detail_toast_add_favorite, 0)
                } else {
                    mainPageView.showHintToast(R.string.vote_detail_toast_remove_favorite, 0)
                }
            }

            override fun onFailure() {
                Log.d(TAG, "favoriteVote onFailure")
                mainPageView.showHintToast(R.string.toast_network_connect_error_favorite, 0)

            }
        })
    }

    override fun IntentToShareDialog(voteData: VoteData) {
        mainPageView.showShareDialog(voteData)
    }

    override fun IntentToCreateVote() {
        mainPageView.showCreateVote()
    }

    override fun IntentToAuthorDetail(voteData: VoteData) {
        mainPageView.showAuthorDetail(voteData)
    }

    override fun IntentToVoteDetail(voteData: VoteData) {
        mainPageView.showVoteDetail(voteData)
    }

    override fun pollVote(voteData: VoteData, optionCode: String, password: String) {
        if (voteData.isNeedPassword && !mainPageView.isPasswordDialogShowing) {
            mainPageView.showPollPasswordDialog(voteData, optionCode)
        } else {
            mainPageView.showLoadingCircle()
            val choiceCodeList = ArrayList<String>()
            choiceCodeList.add(optionCode)
            voteDataRepository.pollVote(voteData.voteCode, password, choiceCodeList, user, object : VoteDataSource.PollVoteCallback {
                override fun onSuccess(voteData: VoteData) {
                    mainPageView.hideLoadingCircle()
                    updateVoteDataToList(hotVoteDataList, voteData)
                    updateVoteDataToList(newVoteDataList, voteData)
                    hotsFragment!!.refreshFragment(hotVoteDataList)
                    newsFragment!!.refreshFragment(newVoteDataList)
                }

                override fun onFailure() {
                    mainPageView.showHintToast(R.string.toast_network_connect_error_quick_poll, 0)
                    mainPageView.hidePollPasswordDialog()
                    mainPageView.hideLoadingCircle()
                }

                override fun onPasswordInvalid() {
                    mainPageView.shakePollPasswordDialog()
                    mainPageView.hideLoadingCircle()
                    mainPageView.showHintToast(R.string.vote_detail_dialog_password_toast_retry, 0)
                }
            })
        }
    }

    override fun reloadHotList(offset: Int) {
        if (user.userCode.isNullOrEmpty()) {
            start()
            return
        }
        voteDataRepository.getHotVoteList(offset, user, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                //hotVoteDataList = voteDataList;
                updateHotList(voteDataList.toMutableList(), offset)
                if (hotsFragment != null) {
                    hotsFragment!!.refreshFragment(hotVoteDataList.toList())
                    hotsFragment!!.hideSwipeLoadView()
                }
                mainPageView.hideLoadingCircle()
            }

            override fun onVoteListNotAvailable() {
                mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
                mainPageView.hideLoadingCircle()
                if (hotsFragment != null) {
                    hotsFragment!!.hideSwipeLoadView()
                }
            }
        })
    }

    override fun reloadNewList(offset: Int) {
        if (user.userCode.isNullOrEmpty()) {
            start()
            return
        }
        voteDataRepository.getNewVoteList(offset, user, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                //newVoteDataList = voteDataList;
                updateNewList(voteDataList.toMutableList(), offset)
                Log.d(TAG, "2NEW LIST offset:" + offset + " , size;" + newVoteDataList.size)
                if (newsFragment != null) {
                    newsFragment!!.hideSwipeLoadView()
                    newsFragment!!.refreshFragment(newVoteDataList)
                }
                mainPageView.hideLoadingCircle()
            }

            override fun onVoteListNotAvailable() {
                mainPageView.hideLoadingCircle()
                if (newsFragment != null) {
                    newsFragment!!.hideSwipeLoadView()
                }
                mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
            }
        })
    }

    private fun updateHotList(voteDataList: MutableList<VoteData>, offset: Int) {
        val pageNumber = offset / LIMIT
        if (offset == 0) {
            this.hotVoteDataList = voteDataList
        } else if (offset >= this.hotVoteDataList.size) {
            this.hotVoteDataList.addAll(voteDataList)
        }
        Log.d(TAG, "hotVoteDataList:" + hotVoteDataList.size + ",offset :" + offset)
        if (this.hotVoteDataList.size < LIMIT * (pageNumber + 1)) {
            if (hotsFragment != null) {
                hotsFragment!!.setMaxCount(this.hotVoteDataList.size)
            }
            if (offset != 0) {
                mainPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0)
            }
        } else {
            if (hotsFragment != null) {
                hotsFragment!!.setMaxCount(LIMIT * (pageNumber + 2))
            }
        }
    }

    private fun updateNewList(voteDataList: MutableList<VoteData>, offset: Int) {
        val pageNumber = offset / LIMIT
        if (offset == 0) {
            this.newVoteDataList = voteDataList
        } else if (offset >= this.newVoteDataList.size) {
            this.newVoteDataList.addAll(voteDataList)
        }
        Log.d(TAG, "newVoteDataList:" + newVoteDataList.size + ",offset :" + offset)
        if (this.newVoteDataList.size < LIMIT * (pageNumber + 1)) {
            if (newsFragment != null) {
                newsFragment!!.setMaxCount(this.newVoteDataList.size)
            }
            if (offset != 0) {
                mainPageView.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0)
            }
        } else {
            if (newsFragment != null) {
                newsFragment!!.setMaxCount(LIMIT * (pageNumber + 2))
            }
        }
    }

    override fun refreshNewList() {
        Log.d(TAG, "1NEW LIST size;" + newVoteDataList.size)
        reloadNewList(newVoteDataList.size)
    }

    override fun refreshHotList() {
        reloadHotList(hotVoteDataList.size)
    }

    override fun reloadCreateList(offset: Int) {

    }

    override fun reloadParticipateList(offset: Int) {

    }

    override fun reloadFavoriteList(offset: Int) {

    }

    override fun refreshCreateList() {

    }

    override fun refreshParticipateList() {

    }

    override fun refreshFavoriteList() {

    }

    override fun setTargetUser(targetUser: User) {

    }

    override fun refreshAllFragment() {
        if (hotsFragment != null)
            hotsFragment!!.refreshFragment(hotVoteDataList)
        if (newsFragment != null)
            newsFragment!!.refreshFragment(newVoteDataList)
    }

    private fun updateVoteDataToList(voteDataList: MutableList<VoteData>, updateData: VoteData) {
        for (i in voteDataList.indices) {
            if (updateData.voteCode == voteDataList[i].voteCode) {
                voteDataList[i] = updateData
                break
            }
        }
    }

    override fun start() {
        mainPageView.showLoadingCircle()
        mainPageView.showIntroductionDialog()
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                this@MainPagePresenter.user = user
                Log.d(TAG, "getUserCallback user:" + user.type)
                mainPageView.setUpTabsAdapter(user)
                promotionRepository.getPromotionList(user, object : PromotionDataSource.GetPromotionsCallback {
                    override fun onPromotionsLoaded(promotionList: List<Promotion>) {
                        this@MainPagePresenter.promotionList = promotionList
                        mainPageView.setupPromotionAdmob(promotionList, user)
                        Log.d(TAG, "GET_PROMOTION_LIST:" + promotionList.size)
                    }

                    override fun onPromotionsNotAvailable() {

                    }
                })
                reloadHotList(0)
                reloadNewList(0)
            }

            override fun onFailure() {
                mainPageView.showHintToast(R.string.toast_network_connect_error_get_list, 0)
                mainPageView.setUpTabsAdapter(user)
                mainPageView.hideLoadingCircle()
                Log.d(TAG, "getUserCallback user failure:" + user)
            }
        }, false)
    }

    companion object {
        var TAG = MainPagePresenter::class.java.simpleName
        private val LIMIT = VoteDataRepository.PAGE_COUNT
    }
}
