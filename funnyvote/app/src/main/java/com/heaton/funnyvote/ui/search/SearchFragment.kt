package com.heaton.funnyvote.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import at.grabner.circleprogress.TextMode
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.FragmentSearchBinding
import com.heaton.funnyvote.utils.Util
import java.util.ArrayList

class SearchFragment : Fragment(), SearchContract.View {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private var adapter: SearchItemAdapter? = null
    private var searchView: SearchView? = null
    private val keyword = ""
    private var tracker: Tracker? = null
    private lateinit var presenter: SearchContract.Presenter

    private val queryListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextChange(newText: String): Boolean {
            if (newText.length > 1) {
                tracker?.send(
                    HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                        .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                        .setLabel(keyword).build()
                )
                Log.d(TAG, "Search page onQueryTextChange:$newText")
                presenter.searchVote(newText)
            }
            return false
        }

        override fun onQueryTextSubmit(query: String): Boolean {
            tracker?.send(
                HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                    .setLabel(query).build()
            )
            Log.d(TAG, "Search page onQueryTextSubmit:$query")
            presenter.searchVote(query)
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem = menu.findItem(R.id.menu_search)
        searchView = searchItem?.actionView as? SearchView
        searchView?.let {
            it.queryHint = getString(R.string.vote_detail_menu_search_hint)
            it.isSubmitButtonEnabled = true
            it.setOnQueryTextListener(queryListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker
        binding.rySearchResult.addItemDecoration(dividerItemDecoration)
        binding.circleLoad.setTextMode(TextMode.TEXT)
        binding.circleLoad.isShowTextWhileSpinning = true
        binding.circleLoad.setFillCircleColor(ContextCompat.getColor(requireActivity(), R.color.md_amber_50))
        hideLoadingCircle()
        initRecyclerView()
        val searchArgument = arguments
        var kw = ""
        if (searchArgument != null) {
            kw = searchArgument.getString(KEY_SEARCH_KEYWORD, "")
        }

        presenter = SearchPresenter(
            Injection.provideVoteDataRepository(requireContext()),
            Injection.provideUserRepository(requireContext()),
            this
        )
        presenter.start(kw)
    }

    private fun initRecyclerView() {
        _binding?.let { b ->
            adapter = SearchItemAdapter(requireContext(), ArrayList(), object : VoteSearchItemListener {
                override fun onVoteItemClick(voteData: VoteData) {
                    presenter.IntentToVoteDetail(voteData)
                }

                override fun onReloadVote() {
                    presenter.refreshSearchList()
                }
            })
            b.rySearchResult.adapter = adapter
        }
    }

    override fun showLoadingCircle() {
        _binding?.let { b ->
            b.circleLoad.visibility = View.VISIBLE
            b.circleLoad.setText(getString(R.string.vote_detail_circle_loading))
            b.circleLoad.spin()
        }
    }

    override fun hideLoadingCircle() {
        _binding?.let { b ->
            b.circleLoad.stopSpinning()
            b.circleLoad.visibility = View.GONE
        }
    }

    override fun showHintToast(res: Int, arg: Long) {
        if (isAdded) {
            Toast.makeText(activity, getString(res, arg), Toast.LENGTH_SHORT).show()
        }
    }

    override fun showVoteDetail(data: VoteData) {
        Util.startActivityToVoteDetail(requireContext(), data.voteCode)
    }

    override fun setMaxCount(max: Int) {
        adapter?.setMaxCount(max.toLong())
    }

    override fun refreshFragment(voteDataList: List<VoteData>) {
        adapter?.setVoteList(voteDataList)
        adapter?.notifyDataSetChanged()
    }

    override fun setPresenter(presenter: SearchContract.Presenter) {
        this.presenter = presenter
    }

    interface VoteSearchItemListener {
        fun onVoteItemClick(voteData: VoteData)
        fun onReloadVote()
    }

    companion object {
        private val TAG = SearchFragment::class.java.simpleName
        private val LIMIT = VoteDataRepository.PAGE_COUNT
        const val KEY_SEARCH_KEYWORD = "key_search_keyword"
    }
}
