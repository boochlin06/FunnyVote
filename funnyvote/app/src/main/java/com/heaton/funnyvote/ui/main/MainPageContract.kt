package com.heaton.funnyvote.ui.main

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData

interface MainPageContract {
    interface Presenter : BasePresenter {
        fun resetPromotion()

        fun setHotsFragmentView(hotsFragmentView: TabPageFragment)

        fun setNewsFragmentView(newsFragmentView: TabPageFragment)

        fun setCreateFragmentView(fragmentView: MainPageContract.TabPageFragment)

        fun setParticipateFragmentView(fragmentView: MainPageContract.TabPageFragment)

        fun setFavoriteFragmentView(fragmentView: MainPageContract.TabPageFragment)

        fun favoriteVote(voteData: VoteData)

        fun IntentToShareDialog(voteData: VoteData)

        fun IntentToCreateVote()

        fun IntentToAuthorDetail(voteData: VoteData)

        fun IntentToVoteDetail(voteData: VoteData)

        fun pollVote(voteData: VoteData, optionCode: String, password: String)

        fun reloadHotList(offset: Int)

        fun reloadNewList(offset: Int)

        fun refreshNewList()

        fun refreshHotList()

        fun reloadCreateList(offset: Int)

        fun reloadParticipateList(offset: Int)

        fun reloadFavoriteList(offset: Int)

        fun refreshCreateList()

        fun refreshParticipateList()

        fun refreshFavoriteList()

        fun setTargetUser(targetUser: User)

        fun refreshAllFragment()
    }

    interface MainPageView : BaseView<Presenter> {

        val isPasswordDialogShowing: Boolean
        fun showShareDialog(data: VoteData)

        fun showAuthorDetail(data: VoteData)

        fun showCreateVote()

        fun showVoteDetail(data: VoteData)

        fun showIntroductionDialog()

        fun showLoadingCircle()

        fun hideLoadingCircle()

        fun setupPromotionAdmob(promotionList: List<Promotion>, user: User)

        fun setUpTabsAdapter(user: User)

        fun setUpTabsAdapter(user: User, targetUser: User)

        fun showHintToast(res: Int, arg: Long)

        fun showPollPasswordDialog(data: VoteData, optionCode: String)

        fun hidePollPasswordDialog()

        fun shakePollPasswordDialog()

    }

    interface TabPageFragment : BaseView<Presenter> {
        fun setUpRecycleView(voteDataList: List<VoteData>)

        fun refreshFragment(voteDataList: List<VoteData>)

        fun setTab(tab: String)

        fun hideSwipeLoadView()

        fun setMaxCount(max: Int)
    }
}
