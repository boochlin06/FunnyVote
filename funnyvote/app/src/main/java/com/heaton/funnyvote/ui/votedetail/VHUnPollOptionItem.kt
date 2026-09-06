package com.heaton.funnyvote.ui.votedetail

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.databinding.CardViewItemUnpollOptionsBinding

class VHUnPollOptionItem(
    val binding: CardViewItemUnpollOptionsBinding,
    private val isMultiChoice: Boolean,
    private val itemListener: VoteDetailContentActivity.OptionItemListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        itemView: View,
        isMultiChoice: Boolean,
        itemListener: VoteDetailContentActivity.OptionItemListener
    ) : this(CardViewItemUnpollOptionsBinding.bind(itemView), isMultiChoice, itemListener)

    private lateinit var option: Option
    private var isChoice = false
    private var isExpand = false

    fun setLayout(isChoice: Boolean, isExpand: Boolean, option: Option) {
        this.option = option
        this.isChoice = isChoice
        this.isExpand = isExpand
        binding.txtOptionTitle.text = option.title
        binding.txtOptionNumber.text = (adapterPosition + 1).toString()
        setUpOptionExpandLayout()
        setUpImgChoiceLayout()
        binding.imgChoice.setOnClickListener { itemListener.onOptionChoice(option.id, option.code) }
        itemView.setOnClickListener {
            if (binding.txtOptionTitle.lineCount == 1) {
                itemListener.onOptionChoice(option.id, option.code)
            } else {
                itemListener.onOptionExpand(option.code)
            }
        }
        binding.cardOption.setOnLongClickListener {
            itemListener.onOptionQuickPoll(option.id, option.code)
            true
        }
    }

    private fun setUpImgChoiceLayout() {
        if (!isMultiChoice) {
            binding.imgChoice.setImageResource(
                if (isChoice) R.drawable.ic_radio_button_checked_40dp else R.drawable.ic_radio_button_unchecked_40dp
            )
        } else {
            binding.imgChoice.setImageResource(
                if (isChoice) R.drawable.ic_check_box_40dp else R.drawable.ic_check_box_outline_blank_40dp
            )
        }
        binding.cardOption.setCardBackgroundColor(
            ContextCompat.getColor(
                itemView.context,
                if (isChoice) R.color.md_red_100 else R.color.md_blue_100
            )
        )
    }

    private fun setUpOptionExpandLayout() {
        if (isExpand) {
            binding.txtOptionTitle.maxLines = 20
        } else {
            binding.txtOptionTitle.maxLines = 1
        }
    }
}
