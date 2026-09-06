package com.heaton.funnyvote.ui.search

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.ItemListSearchBinding

class VHSearchItem(
    val binding: ItemListSearchBinding,
    private val itemListener: SearchFragment.VoteSearchItemListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    constructor(
        itemView: View,
        itemListener: SearchFragment.VoteSearchItemListener
    ) : this(ItemListSearchBinding.bind(itemView), itemListener)

    private lateinit var data: VoteData

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        itemListener.onVoteItemClick(data)
    }

    fun setLayout(data: VoteData) {
        this.data = data
        binding.txtTitle.text = data.title
        binding.txtAuthorName.text = data.authorName
        binding.txtBarPollCount.text = String.format(
            itemView.context.getString(R.string.wall_item_bar_vote_count), data.pollCount
        )
        if (data.voteImage.isNullOrEmpty()) {
            binding.imgMain.setImageResource(data.localImage)
        } else {
            Glide.with(itemView.context)
                .load(data.voteImage)
                .override(
                    itemView.resources.getDimension(R.dimen.search_image_width).toInt(),
                    itemView.resources.getDimension(R.dimen.search_image_high).toInt()
                )
                .centerCrop()
                .into(binding.imgMain)
        }
        if (data.endTime < System.currentTimeMillis()) {
            binding.txtHint.setText(R.string.search_item_time_end)
            binding.txtHint.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_red_500))
        } else {
            binding.txtHint.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_blue_500))
            binding.txtHint.setText(R.string.search_item_time_voting)
        }
    }
}
