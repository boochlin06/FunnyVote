package com.heaton.funnyvote.ui.search

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import at.grabner.circleprogress.TextMode
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.heaton.funnyvote.FunnyVoteApplication
import com.heaton.funnyvote.R
import com.heaton.funnyvote.analytics.AnalyzticsTag
import com.heaton.funnyvote.data.Injection
import com.heaton.funnyvote.data.VoteData.VoteDataRepository
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.utils.Util
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.*

/**
 * Created by heaton on 2017/1/22.
 */

class SearchFragment : Fragment(), SearchContract.View {
    private var adapter: SearchItemAdapter? = null
    private var searchView: SearchView? = null

    private val keyword = ""
    private var tracker: Tracker? = null

    private lateinit var presenter: SearchContract.Presenter

    private val queryListener = object : SearchView.OnQueryTextListener {

        override fun onQueryTextChange(newText: String): Boolean {
            if (newText.length > 1) {
                tracker!!.send(HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                        .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                        .setLabel(keyword).build())

                Log.d(TAG, "Search page onQueryTextChange:$newText")
                presenter.searchVote(newText)
            }
            return false
        }

        override fun onQueryTextSubmit(query: String): Boolean {

            tracker!!.send(HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_SEARCH_VOTE)
                    .setAction(AnalyzticsTag.ACTION_SEARCH_VOTE)
                    .setLabel(query).build())
            Log.d(TAG, "Search page onQueryTextSubmit:$query")
            presenter.searchVote(query)
            return false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        searchView = menu!!.findItem(R.id.menu_search).actionView as SearchView
        if (searchView != null) {
            searchView!!.queryHint = getString(R.string.vote_detail_menu_search_hint)
            searchView!!.isSubmitButtonEnabled = true
            searchView!!.setOnQueryTextListener(queryListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        return inflater.inflate(R.layout.fragment_search, container, false) as RelativeLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = DividerItemDecoration(rySearchResult.context,
                DividerItemDecoration.VERTICAL)
        val application = requireActivity().application as FunnyVoteApplication
        tracker = application.defaultTracker
        rySearchResult.addItemDecoration(dividerItemDecoration)
        circleLoad.setTextMode(TextMode.TEXT)
        circleLoad.isShowTextWhileSpinning = true
        circleLoad.setFillCircleColor(ContextCompat.getColor(activity!!, R.color.md_amber_50))
        hideLoadingCircle()
        initRecyclerView()
        val searchArgument = arguments
        var keyword = ""
        if (searchArgument != null) {
            keyword = searchArgument.getString(KEY_SEARCH_KEYWORD, "")
        }

        presenter = SearchPresenter(Injection.provideVoteDataRepository(context!!), Injection.provideUserRepository(context!!), this)
        presenter.start(keyword)

    }

    private fun initRecyclerView() {
        adapter = SearchItemAdapter(requireContext(), ArrayList(), object : VoteSearchItemListener {
            override fun onVoteItemClick(voteData: VoteData) {
                presenter.IntentToVoteDetail(voteData)
            }

            override fun onReloadVote() {
                presenter.refreshSearchList()
            }
        })
        rySearchResult.adapter = adapter

    }

    override fun showLoadingCircle() {
        circleLoad.visibility = View.VISIBLE
        circleLoad.setText(getString(R.string.vote_detail_circle_loading))
        circleLoad.spin()
    }

    override fun hideLoadingCircle() {
        circleLoad.stopSpinning()
        circleLoad.visibility = View.GONE
    }

    override fun showHintToast(res: Int, arg: Long) {
        if (isAdded)
            Toast.makeText(activity, getString(res, arg), Toast.LENGTH_SHORT).show()
    }

    override fun showVoteDetail(data: VoteData) {
        Util.startActivityToVoteDetail(context!!, data.voteCode)
    }

    override fun setMaxCount(max: Int) {
        if (adapter != null) {
            adapter!!.setMaxCount(max.toLong())
        }
    }

    override fun refreshFragment(voteDataList: List<VoteData>) {
        adapter!!.setVoteList(voteDataList)
        adapter!!.notifyDataSetChanged()
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
        val KEY_SEARCH_KEYWORD = "key_search_keyword"
    }
}
