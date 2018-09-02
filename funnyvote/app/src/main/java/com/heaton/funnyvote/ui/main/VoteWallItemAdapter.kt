package com.heaton.funnyvote.ui.main

import android.content.Context
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData
import java.util.*

/**
 * Created by heaton on 16/4/1.
 */
class VoteWallItemAdapter(private val context: Context, private val wallItemListener: MainPageTabFragment.VoteWallItemListener, private var voteList: List<VoteData>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var itemTypeList: MutableList<ListTypeItem>
    private var maxCount: Int = 0
    private var showReload: Boolean = false
    private var tagNoVote = TAG_NO_VOTE_NOPE
    private var bannerAdmob: View? = null

    private inner class ListTypeItem(var viewType: Int, var voteData: VoteData?)

    init {
        //ENABLE_ADMOB = context.getResources().getBoolean(R.bool.enable_list_admob);
        //ADMOB_FREQUENCE = context.getResources().getInteger(R.integer.list_admob_frequence);
        Log.d(TAG, "LIST SIZE:" + voteList!!.size)
        Log.d(TAG, "Banner Admob ENABLE:$ENABLE_ADMOB Frequency:$ADMOB_FREQUENCE")
        itemTypeList = ArrayList()
    }

    fun setVoteList(voteList: List<VoteData>) {
        this.voteList = voteList
    }

    fun setMaxCount(count: Long) {
        maxCount = count.toInt()
    }

    fun resetItemTypeList() {
        itemTypeList.clear()
        for (i in voteList!!.indices) {
            if (i % ADMOB_FREQUENCE == ADMOB_FREQUENCE - 1 && ENABLE_ADMOB) {
                itemTypeList.add(ListTypeItem(ITEM_TYPE_ADMOB, null))
            }
            itemTypeList.add(ListTypeItem(ITEM_TYPE_VOTE, this.voteList!![i]))
        }
        showReload = this.voteList!!.size in 1..(maxCount - 1)
        if (showReload) {
            itemTypeList.add(ListTypeItem(ITEM_TYPE_RELOAD, null))
        } else if (!showReload && voteList!!.isEmpty() && maxCount != -1) {
            itemTypeList.add(ListTypeItem(ITEM_TYPE_NO_VOTE, null))
        }
    }

    fun setNoVoteTag(tag: String) {
        this.tagNoVote = tag
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            ITEM_TYPE_VOTE -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_wall_item, parent, false)
                return VHVoteWallItem(v, wallItemListener)
            }
            ITEM_TYPE_RELOAD -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item_reload, parent, false)
                return ReloadViewHolder(v, wallItemListener)
            }
            ITEM_TYPE_NO_VOTE -> {
                val v = LayoutInflater.from(parent.context).inflate(R.layout.card_view_item_no_vote, parent, false)
                return VHNoVote(v, wallItemListener)
            }
            ITEM_TYPE_ADMOB -> {
                if (bannerAdmob == null) {
                    bannerAdmob = LayoutInflater.from(context).inflate(R.layout.item_list_admob, parent, false)
                }
                return VHAdMob(bannerAdmob!!)
            }
            else -> return VHAdMob(bannerAdmob!!)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VHVoteWallItem) {
            Log.d(TAG, "favorite:" + itemTypeList[position].voteData!!.isFavorite)
            holder.setLayout(itemTypeList[position].voteData!!)
        } else if (holder is VHAdMob) {
            holder.setLayout()
        }
    }

    override fun getItemCount(): Int {
        return itemTypeList.size
    }

    override fun getItemViewType(position: Int): Int {
        return itemTypeList[position].viewType
    }

    private inner class ReloadViewHolder(itemView: View, wallItemListener: MainPageTabFragment.VoteWallItemListener) : RecyclerView.ViewHolder(itemView) {
        var reloadImage: ImageView = itemView.findViewById<View>(R.id.img_load_more) as ImageView

        init {
            itemView.setOnClickListener { v ->
                val animation = AnimationUtils.loadAnimation(context, R.anim.reload_rotate)
                v.findViewById<View>(R.id.img_load_more).startAnimation(animation)
                wallItemListener.onReloadVote()
            }
        }
    }

    private inner class VHNoVote(itemView: View, wallItemListener: MainPageTabFragment.VoteWallItemListener) : RecyclerView.ViewHolder(itemView) {

        var imgAddVote: ImageView = itemView.findViewById<View>(R.id.imgAddVote) as ImageView
        var imgRefreshVote: ImageView = itemView.findViewById<View>(R.id.imgRefreshVote) as ImageView
        var imgLogo: ImageView = itemView.findViewById<View>(R.id.imgLogo) as ImageView
        var txtNoVote: TextView = itemView.findViewById<View>(R.id.txtNoVote) as TextView

        init {//wallItemListener.onReloadVote();
            //context.startActivity(new Intent(context, CreateVoteActivity.class));
            when (tagNoVote) {
                TAG_NO_VOTE_CREATE_NEW -> {
                    imgAddVote.visibility = View.VISIBLE
                    imgRefreshVote.visibility = View.GONE
                    txtNoVote.setText(R.string.wall_item_no_vote_create_new)
                    imgLogo.visibility = View.GONE
                }
                TAG_NO_VOTE_NOPE -> {
                    imgAddVote.visibility = View.GONE
                    txtNoVote.setText(R.string.wall_item_no_vote)
                    imgRefreshVote.visibility = View.GONE
                    imgLogo.visibility = View.VISIBLE
                }
                TAG_NO_VOTE_REFRESH -> {
                    imgAddVote.visibility = View.GONE
                    imgRefreshVote.visibility = View.VISIBLE
                    txtNoVote.setText(R.string.wall_item_no_vote_refresh)
                    imgLogo.visibility = View.GONE
                }
                TAG_NO_VOTE_FAVORITE -> {
                    imgAddVote.visibility = View.GONE
                    txtNoVote.setText(R.string.wall_item_no_vote_favorite)
                    imgRefreshVote.visibility = View.GONE
                    imgLogo.visibility = View.VISIBLE
                }
                TAG_NO_VOTE_PARTICIPATE -> {
                    imgAddVote.visibility = View.GONE
                    txtNoVote.setText(R.string.wall_item_no_vote_participate)
                    imgRefreshVote.visibility = View.GONE
                    imgLogo.visibility = View.VISIBLE
                }
                TAG_NO_VOTE_CREATE_NEW_OTHER -> {
                    imgAddVote.visibility = View.GONE
                    txtNoVote.setText(R.string.wall_item_no_vote_create_new_other)
                    imgRefreshVote.visibility = View.GONE
                    imgLogo.visibility = View.VISIBLE
                }
            }
            itemView.setOnClickListener { v ->
                if (tagNoVote == TAG_NO_VOTE_CREATE_NEW) {
                    wallItemListener.onNoVoteCreateNew()
                    //context.startActivity(new Intent(context, CreateVoteActivity.class));
                } else if (tagNoVote == TAG_NO_VOTE_REFRESH) {
                    //wallItemListener.onReloadVote();
                    val animation = AnimationUtils.loadAnimation(context, R.anim.reload_rotate)
                    v.findViewById<View>(R.id.imgRefreshVote).startAnimation(animation)
                    wallItemListener.onReloadVote()
                } else if (tagNoVote == TAG_NO_VOTE_NOPE) {

                }
            }
        }
    }

    private inner class VHAdMob(view: View) : RecyclerView.ViewHolder(view) {

        private var isLoaded = false
        private val adView: AdView = view.findViewById<View>(R.id.adView) as AdView

        fun setLayout() {
            if (!isLoaded) {
                isLoaded = true
                Handler().postDelayed({
                    Log.d(TAG, "Build admob request.")
                    val adRequest = AdRequest.Builder().build()
                    adView.loadAd(adRequest)
                    adView.pause()
                }, 1000)
            }
        }
    }

    companion object {

        var TAG = VoteWallItemAdapter::class.java.simpleName
        val ITEM_TYPE_VOTE = 41
        val ITEM_TYPE_RELOAD = 42
        val ITEM_TYPE_NO_VOTE = 43
        val ITEM_TYPE_ADMOB = 44

        val TAG_NO_VOTE_CREATE_NEW = "CREATE_NEW"
        val TAG_NO_VOTE_CREATE_NEW_OTHER = "CREATE_NEW_OTHER"
        val TAG_NO_VOTE_REFRESH = "REFRESH"
        val TAG_NO_VOTE_NOPE = "CREATE_NOPE"
        val TAG_NO_VOTE_PARTICIPATE = "PARTICIPATE"
        val TAG_NO_VOTE_FAVORITE = "FAVORITE"

        var ADMOB_FREQUENCE = 10
        var ENABLE_ADMOB = false
    }
}
