package com.heaton.funnyvote.ui.search

import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.data.VoteData.VoteDataSource
import com.heaton.funnyvote.data.user.UserDataRepository
import com.heaton.funnyvote.data.user.UserDataSource
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import java.util.*

class SearchPresenter(
        private val voteDataRepository: VoteDataRepository
        , private val userDataRepository: UserDataRepository
        , private val view: SearchContract.View
) : SearchContract.Presenter {

    private var searchVoteDataList: MutableList<VoteData>? = null
    var user: User? = null

    var keyword: String? = null

    fun getSearchVoteDataList(): List<VoteData>? {
        return searchVoteDataList
    }

    fun setSearchVoteDataList(searchVoteDataList: MutableList<VoteData>) {
        this.searchVoteDataList = searchVoteDataList
    }


    init {
        this.searchVoteDataList = ArrayList()
        this.view.setPresenter(this)
    }

    override fun searchVote(keyword: String) {
        this.keyword = keyword
        reloadSearchList(0)
    }


    override fun reloadSearchList(offset: Int) {
        voteDataRepository.getSearchVoteList(keyword!!, offset, user!!
                , object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                updateSearchList(voteDataList.toMutableList(), offset)
                view.refreshFragment(searchVoteDataList!!)
            }

            override fun onVoteListNotAvailable() {
                view.showHintToast(R.string.toast_network_connect_error, 0)
            }
        })
    }

    override fun refreshSearchList() {
        reloadSearchList(searchVoteDataList!!.size)
    }

    override fun IntentToVoteDetail(voteData: VoteData) {
        view.showVoteDetail(voteData)
    }

    private fun updateSearchList(voteDataList: MutableList<VoteData>, offset: Int) {
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT
        if (offset == 0) {
            this.searchVoteDataList = voteDataList
        } else if (offset >= this.searchVoteDataList!!.size) {
            this.searchVoteDataList!!.addAll(voteDataList)
        }
        //Log.d(TAG, "searchVoteDataList:" + searchVoteDataList.size() + ",offset :" + offset);
        if (this.searchVoteDataList!!.size < VoteDataRepository.PAGE_COUNT * (pageNumber + 1)) {
            view.setMaxCount(this.searchVoteDataList!!.size)
            if (offset != 0) {
                view.showHintToast(R.string.wall_item_toast_no_vote_refresh, 0)
            }
        } else {
            view.setMaxCount(VoteDataRepository.PAGE_COUNT * (pageNumber + 2))
        }
    }

    override fun start(keyword: String) {
        this.keyword = keyword
        userDataRepository.getUser(object : UserDataSource.GetUserCallback {
            override fun onResponse(user: User) {
                this@SearchPresenter.user = user
                if (!keyword.isNullOrEmpty()) {
                    searchVote(keyword)
                }
            }

            override fun onFailure() {

            }
        }, false)
    }

    override fun start() {
        start("")
    }

    companion object {

        private val TAG = SearchPresenter::class.java.simpleName
    }
}
