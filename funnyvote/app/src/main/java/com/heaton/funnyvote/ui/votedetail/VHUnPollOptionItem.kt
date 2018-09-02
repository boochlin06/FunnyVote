package com.heaton.funnyvote.ui.votedetail

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option
import kotlinx.android.synthetic.main.card_view_item_unpoll_options.view.*

/**
 * Created by heaton on 2016/8/22.
 */

class VHUnPollOptionItem(
        itemView: View
        , private val isMultiChoice: Boolean
        , private val itemListener: VoteDetailContentActivity.OptionItemListener
) : RecyclerView.ViewHolder(itemView) {
    private lateinit var option: Option
    private var isChoice = false
    private var isExpand = false

    fun setLayout(isChoice: Boolean, isExpand: Boolean, option: Option) {
        this.option = option
        this.isChoice = isChoice
        this.isExpand = isExpand
        itemView.txtOptionTitle.text = option.title
        itemView.txtOptionNumber.text = (adapterPosition + 1).toString()
        setUpOptionExpandLayout()
        setUpImgChoiceLayout()
        itemView.imgChoice.setOnClickListener { itemListener.onOptionChoice(option.id, option.code) }
        itemView.setOnClickListener {
            if (itemView.txtOptionTitle.lineCount == 1) {
                itemListener.onOptionChoice(option.id, option.code)
            } else {
                itemListener.onOptionExpand(option.code)
            }
        }
        itemView.cardOption.setOnLongClickListener {
            itemListener.onOptionQuickPoll(option.id, option.code)
            true
        }
    }

    private fun setUpImgChoiceLayout() {
        if (!isMultiChoice) {
            itemView.imgChoice.setImageResource(if (isChoice)
                R.drawable.ic_radio_button_checked_40dp
            else
                R.drawable.ic_radio_button_unchecked_40dp)
        } else {
            itemView.imgChoice.setImageResource(if (isChoice)
                R.drawable.ic_check_box_40dp
            else
                R.drawable.ic_check_box_outline_blank_40dp)
        }
        itemView.cardOption.setCardBackgroundColor(
                ContextCompat.getColor(itemView.context
                        , if (isChoice) R.color.md_red_100 else R.color.md_blue_100))
    }

    private fun setUpOptionExpandLayout() = if (isExpand) {
        itemView.txtOptionTitle.maxLines = 20
    } else {
        itemView.txtOptionTitle.maxLines = 1
    }
}
