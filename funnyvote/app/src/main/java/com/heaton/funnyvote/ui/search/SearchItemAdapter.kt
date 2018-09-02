package com.heaton.funnyvote.ui.search

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData


/**
 * Created by heaton on 2017/1/22.
 */

class SearchItemAdapter(private val context: Context
                        , private var voteList: List<VoteData>
                        , private val itemListener: SearchFragment.VoteSearchItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var showReload: Boolean = false
    private var maxCount: Long = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_TYPE_VOTE -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.item_list_search, parent, false)
                return VHSearchItem(v, itemListener)
            }
            ITEM_TYPE_RELOAD -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item_reload, parent, false)
                return ReloadViewHolder(v, itemListener)
            }
            ITEM_TYPE_NO_VOTE -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item_no_vote, parent, false)
                return VHNoVote(v)
            }
            else -> {
                return VHNoVote(parent)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VHSearchItem) {
            holder.setLayout(voteList[position])
        }
    }

    fun setVoteList(voteList: List<VoteData>) {
        this.voteList = voteList
    }

    override fun getItemCount(): Int {
        showReload = voteList.size in 1..(maxCount - 1)
        return if (showReload) {
            voteList.size + 1
        } else {
            if (voteList.isEmpty()) {
                1
            } else {
                voteList.size
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (showReload && position == itemCount - 1) {
            ITEM_TYPE_RELOAD
        } else {
            if (voteList.isEmpty()) {
                ITEM_TYPE_NO_VOTE
            } else {
                ITEM_TYPE_VOTE
            }
        }
    }

    fun setMaxCount(count: Long) {
        maxCount = count
    }


    private inner class ReloadViewHolder(itemView: View, itemListener: SearchFragment.VoteSearchItemListener) : RecyclerView.ViewHolder(itemView) {
        var reloadImage: ImageView = itemView.findViewById<View>(R.id.img_load_more) as ImageView


        init {
            itemView.setOnClickListener { v ->
                val animation = AnimationUtils.loadAnimation(context, R.anim.reload_rotate)
                v.findViewById<View>(R.id.img_load_more).startAnimation(animation)
                itemListener.onReloadVote()
            }
        }
    }


    private inner class VHNoVote(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgAddVote: ImageView = itemView.findViewById<View>(R.id.imgAddVote) as ImageView
        var imgRefreshVote: ImageView = itemView.findViewById<View>(R.id.imgRefreshVote) as ImageView
        var imgLogo: ImageView = itemView.findViewById<View>(R.id.imgLogo) as ImageView
        var txtNoVote: TextView = itemView.findViewById<View>(R.id.txtNoVote) as TextView

        init {
            imgAddVote.visibility = View.GONE
            imgRefreshVote.visibility = View.GONE
            imgLogo.visibility = View.VISIBLE
            txtNoVote.setText(R.string.wall_item_no_vote_search)
        }
    }

    companion object {
        const val ITEM_TYPE_VOTE = 41
        const val ITEM_TYPE_RELOAD = 42
        const val ITEM_TYPE_NO_VOTE = 43
    }
}
