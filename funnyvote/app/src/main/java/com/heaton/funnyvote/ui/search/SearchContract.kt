package com.heaton.funnyvote.ui.search

import com.heaton.funnyvote.BasePresenter
import com.heaton.funnyvote.BaseView
import com.heaton.funnyvote.database.VoteData

interface SearchContract {
    interface Presenter : BasePresenter {
        fun searchVote(keyword: String)

        fun reloadSearchList(offset: Int)

        fun refreshSearchList()

        fun IntentToVoteDetail(voteData: VoteData)

        fun start(keyword: String)
    }

    interface View : BaseView<Presenter> {
        fun showLoadingCircle()

        fun hideLoadingCircle()

        fun showHintToast(res: Int, arg: Long)

        fun showVoteDetail(data: VoteData)

        fun setMaxCount(max: Int)

        fun refreshFragment(voteDataList: List<VoteData>)
    }
}
