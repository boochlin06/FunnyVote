/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.heaton.funnyvote.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.FragmentMainPageTabBinding
import com.heaton.funnyvote.ui.HidingScrollListener
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter

class MainPageTabFragment : Fragment(), MainPageContract.TabPageFragment {

    private var _binding: FragmentMainPageTabBinding? = null
    private val binding get() = _binding!!

    private var tab: String = TAB_HOT
    private var adapter: VoteWallItemAdapter? = null
    private var tracker: Tracker? = null
    private lateinit var presenter: MainPageContract.Presenter
    private lateinit var wallItemListener: VoteWallItemListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainPageTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val argument = arguments
        this.tab = argument!!.getString(KEY_TAB) ?: TAB_HOT
        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker
        binding.fabTop.visibility = View.GONE
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
        binding.swipeLayout.setOnRefreshListener(WallItemOnRefreshListener())
        this.setPresenter(presenter)
        when (tab) {
            TAB_HOT -> presenter.setHotsFragmentView(this)
            TAB_NEW -> presenter.setNewsFragmentView(this)
            TAB_CREATE -> presenter.setCreateFragmentView(this)
            TAB_PARTICIPATE -> presenter.setParticipateFragmentView(this)
            TAB_FAVORITE -> presenter.setFavoriteFragmentView(this)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        _binding?.let { b ->
            val manager = b.ryMainPage.layoutManager as? LinearLayoutManager
            val position = manager?.findFirstVisibleItemPosition() ?: 0
            if (position == 0) {
                // TODO:AUTO UPDATE
            }
        }
        adapter?.notifyDataSetChanged()
    }

    override fun setUpRecycleView(voteDataList: List<VoteData>) {
        val b = _binding ?: return
        adapter = VoteWallItemAdapter(requireContext(), wallItemListener, voteDataList)
        adapter!!.setMaxCount(-1)
        when (tab) {
            TAB_HOT -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH)
            TAB_NEW -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH)
            TAB_CREATE -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_CREATE_NEW)
            TAB_PARTICIPATE -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_PARTICIPATE)
            TAB_FAVORITE -> adapter!!.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_FAVORITE)
        }
        adapter!!.resetItemTypeList()
        val itemAdapter = adapter ?: return
        val scaleInAnimationAdapter = ScaleInAnimationAdapter(itemAdapter)
        scaleInAnimationAdapter.setDuration(1000)
        b.ryMainPage.adapter = itemAdapter
        b.ryMainPage.addOnScrollListener(object : HidingScrollListener() {
            override fun onHide() {
                _binding?.let {
                    it.fabTop.animate().translationY(
                            (it.fabTop.height + 50).toFloat()).interpolator = AccelerateInterpolator(2f)
                }
            }

            override fun onShow() {
                this.resetScrollDistance()
                _binding?.let {
                    it.fabTop.animate().translationY(0f).interpolator = DecelerateInterpolator(2f)
                }
            }
        })
        b.fabTop.setOnClickListener {
            val manager = b.ryMainPage.layoutManager as? LinearLayoutManager
            val position = manager?.findFirstVisibleItemPosition() ?: 0
            if (position > 5) {
                b.ryMainPage.scrollToPosition(5)
            }
            b.ryMainPage.smoothScrollToPosition(0)
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
        adapter?.setMaxCount(max.toLong())
    }

    override fun setTab(tab: String) {
        this.tab = tab
    }

    override fun hideSwipeLoadView() {
        _binding?.let {
            if (it.swipeLayout.isRefreshing) {
                it.swipeLayout.isRefreshing = false
            }
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
