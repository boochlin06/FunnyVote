/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.heaton.funnyvote.ui.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.HidingScrollListener
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter
import kotlinx.android.synthetic.main.fragment_main_page_tab.*

class MainPageTabFragment : Fragment(), MainPageContract.TabPageFragment {

    private var tab: String = TAB_HOT
    private var adapter: VoteWallItemAdapter? = null
    private var tracker: Tracker? = null
    private lateinit var presenter: MainPageContract.Presenter
    private lateinit var wallItemListener: VoteWallItemListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_main_page_tab
                , container, false) as RelativeLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments
        this.tab = argument!!.getString(KEY_TAB)
        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker
        fabTop.visibility = View.GONE
        wallItemListener = object : VoteWallItemListener {
            override fun onVoteFavoriteChange(voteData: VoteData) = presenter.favoriteVote(voteData)

            override fun onVoteItemClick(voteData: VoteData) =
                    presenter.IntentToVoteDetail(voteData)

            override fun onVoteAuthorClick(voteData: VoteData) =
                    presenter.IntentToAuthorDetail(voteData)

            override fun onVoteShare(voteData: VoteData) = presenter.IntentToShareDialog(voteData)

            override fun onVoteQuickPoll(voteData: VoteData, optionCode: String) =
                    presenter.pollVote(voteData, optionCode, "")

            override fun onNoVoteCreateNew() = presenter.IntentToCreateVote()

            override fun onReloadVote() {
                when (tab) {
                    TAB_HOT -> presenter.refreshHotList()
                    TAB_NEW -> presenter.refreshNewList()
                    TAB_CREATE -> presenter.refreshCreateList()
                    TAB_PARTICIPATE -> presenter.refreshParticipateList()
                    TAB_FAVORITE -> presenter.refreshFavoriteList()
                }
            }
        }
        swipeLayout.setOnRefreshListener(WallItemOnRefreshListener())
        this.setPresenter(presenter)
        when (tab) {
            TAB_HOT -> presenter.setHotsFragmentView(this)
            TAB_NEW -> presenter.setNewsFragmentView(this)
            TAB_CREATE -> presenter.setCreateFragmentView(this)
            TAB_PARTICIPATE -> presenter.setParticipateFragmentView(this)
            TAB_FAVORITE -> presenter.setFavoriteFragmentView(this)
        }
    }

    override fun onResume() {
        super.onResume()
        val manager = ryMainPage.layoutManager as LinearLayoutManager
        val position = manager.findFirstVisibleItemPosition()
        if (position == 0) {
            // TODO:AUTO UPDATE .
            //refreshData();
        }
        adapter!!.notifyDataSetChanged()
    }


    override fun setUpRecycleView(voteDataList: List<VoteData>) {
        //voteDataList = voteDataList;
        adapter = VoteWallItemAdapter(requireContext(), wallItemListener, voteDataList)
        // if max count is -1 , the list is init.
        adapter!!.setMaxCount(-1)
        when (tab) {
            TAB_HOT -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH)
            TAB_NEW -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH)
            TAB_CREATE -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_CREATE_NEW)
            TAB_PARTICIPATE -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_PARTICIPATE)
            TAB_FAVORITE -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_FAVORITE)
        }
        adapter!!.resetItemTypeList()
        val scaleInAnimationAdapter = ScaleInAnimationAdapter(adapter)
        scaleInAnimationAdapter.setDuration(1000)
        ryMainPage.adapter = adapter
        ryMainPage.addOnScrollListener(object : HidingScrollListener() {
            override fun onHide() {
                fabTop.animate().translationY(
                        (fabTop.height + 50).toFloat()).interpolator = AccelerateInterpolator(2f)
            }

            override fun onShow() {
                this.resetScrollDistance()
                fabTop.animate().translationY(0f).interpolator = DecelerateInterpolator(2f)
            }
        })
        fabTop.setOnClickListener {
            val manager = ryMainPage.layoutManager as LinearLayoutManager
            val position = manager.findFirstVisibleItemPosition()
            if (position > 5) {
                ryMainPage.scrollToPosition(5)
            }
            ryMainPage.smoothScrollToPosition(0)
        }
    }

    override fun refreshFragment(voteDataList: List<VoteData>) {
        if (adapter != null) {
            adapter!!.setVoteList(voteDataList)
            adapter!!.resetItemTypeList()
            adapter!!.notifyDataSetChanged()
        }
    }

    override fun setPresenter(presenter: MainPageContract.Presenter) {
        this.presenter = presenter
    }

    private inner class WallItemOnRefreshListener : SwipeRefreshLayout.OnRefreshListener {

        override fun onRefresh() {

            when (tab) {
                TAB_HOT -> presenter.reloadHotList(0)
                TAB_NEW -> presenter.reloadNewList(0)
                TAB_CREATE -> presenter.reloadCreateList(0)
                TAB_PARTICIPATE -> presenter.reloadParticipateList(0)
                TAB_FAVORITE -> presenter.reloadFavoriteList(0)
            }
        }
    }


    override fun setMaxCount(max: Int) {
        if (adapter != null) {
            adapter!!.setMaxCount(max.toLong())
        }
    }

    override fun setTab(tab: String) {
        this.tab = tab
    }

    override fun hideSwipeLoadView() {
        if (swipeLayout.isRefreshing) {
            swipeLayout.isRefreshing = false
        }
    }

    interface VoteWallItemListener {
        fun onVoteFavoriteChange(voteData: VoteData)

        fun onVoteItemClick(voteData: VoteData)

        fun onVoteAuthorClick(voteData: VoteData)

        fun onVoteShare(voteData: VoteData)

        fun onVoteQuickPoll(voteData: VoteData, optionCode: String)

        fun onNoVoteCreateNew()

        fun onReloadVote()
    }

    companion object {
        private val LIMIT = VoteDataRepository.PAGE_COUNT
        var TAG = MainPageTabFragment::class.java.simpleName

        const val KEY_TAB = "tab"
        const val KEY_LOGIN_USER = "key_login_user"
        const val KEY_TARGET_USER = "key_target_user"

        const val TAB_HOT = "HOT"
        const val TAB_NEW = "NEW"

        const val TAB_CREATE = "CREATE"
        const val TAB_PARTICIPATE = "PARTICIPATE"
        const val TAB_FAVORITE = "FAVORITE"

        @JvmOverloads
        fun newInstance(tab: String, loginUser: User, targetUser: User? = null): MainPageTabFragment {
            val fragment = MainPageTabFragment()
            val argument = Bundle()
            argument.putString(MainPageTabFragment.KEY_TAB, tab)
            argument.putParcelable(MainPageTabFragment.KEY_LOGIN_USER, loginUser)
            argument.putParcelable(MainPageTabFragment.KEY_TARGET_USER, targetUser)
            fragment.arguments = argument
            fragment.retainInstance = false
            return fragment
        }
    }
}
