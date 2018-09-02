package com.heaton.funnyvote.ui.search

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData
import kotlinx.android.synthetic.main.item_list_search.view.*

/**
 * Created by heaton on 2017/1/22.
 */

class VHSearchItem(itemView: View
                   , private val itemListener: SearchFragment.VoteSearchItemListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
    private lateinit var data: VoteData

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        itemListener.onVoteItemClick(data)
    }

    fun setLayout(data: VoteData) {
        this.data = data
        itemView.txtTitle.text = data.title
        itemView.txtAuthorName.text = data.authorName
        itemView.txtBarPollCount.text = String.format(itemView.context
                .getString(R.string.wall_item_bar_vote_count), data.pollCount)
        if (data.voteImage == null || data.voteImage.isEmpty()) {
            itemView.imgMain.setImageResource(data.localImage)
        } else {
            Glide.with(itemView.context)
                    .load(data.voteImage)
                    .override(itemView.resources.getDimension(R.dimen.search_image_width).toInt(), itemView.resources.getDimension(R.dimen.search_image_high).toInt())
                    .centerCrop()
                    .crossFade()
                    .into(itemView.imgMain)
        }
        if (data.endTime < System.currentTimeMillis()) {
            itemView.txtHint.setText(R.string.search_item_time_end)
            itemView.txtHint.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_red_500))
        } else {
            itemView.txtHint.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_blue_500))
            itemView.txtHint.setText(R.string.search_item_time_voting)
        }
    }

}
