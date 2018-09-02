package com.heaton.funnyvote.ui.personal

import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.main.MainPageContract

interface PersonalContract {
    interface Presenter : MainPageContract.Presenter {
        override fun setCreateFragmentView(fragmentView: MainPageContract.TabPageFragment)

        override fun setParticipateFragmentView(fragmentView: MainPageContract.TabPageFragment)

        override fun setFavoriteFragmentView(fragmentView: MainPageContract.TabPageFragment)

        override fun favoriteVote(voteData: VoteData)

        override fun IntentToShareDialog(voteData: VoteData)

        override fun IntentToCreateVote()

        override fun IntentToAuthorDetail(voteData: VoteData)

        override fun IntentToVoteDetail(voteData: VoteData)

        override fun pollVote(voteData: VoteData, optionCode: String, password: String)

        override fun reloadCreateList(offset: Int)

        override fun reloadParticipateList(offset: Int)

        override fun reloadFavoriteList(offset: Int)

        override fun refreshCreateList()

        override fun refreshParticipateList()

        override fun refreshFavoriteList()

        override fun setTargetUser(targetUser: User)

        override fun refreshAllFragment()
    }

    interface UserPageView : MainPageContract.MainPageView {
        fun setUpUserView(user: User)
    }
}
