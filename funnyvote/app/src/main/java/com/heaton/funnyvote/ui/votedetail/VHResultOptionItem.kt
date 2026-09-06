package com.heaton.funnyvote.ui.votedetail

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.databinding.CardViewItemResultOptionBinding

class VHResultOptionItem(
    val binding: CardViewItemResultOptionBinding,
    private val totalPollCount: Int,
    private val itemListener: VoteDetailContentActivity.OptionItemListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    constructor(
        itemView: View,
        totalPollCount: Int,
        itemListener: VoteDetailContentActivity.OptionItemListener
    ) : this(CardViewItemResultOptionBinding.bind(itemView), totalPollCount, itemListener)

    private var isChoice = false
    private var isExpand = false
    private lateinit var option: Option

    fun setLayout(isChoice: Boolean, isExpand: Boolean, isTop: Boolean, option: Option) {
        this.isChoice = isChoice
        this.isExpand = isExpand
        this.option = option
        binding.progressPollCount.max = totalPollCount.toFloat()
        binding.txtOptionTitle.text = option.title
        binding.txtOptionNumber.text = (adapterPosition + 1).toString()
        binding.txtPollCount.text = option.count.toString()
        val percent = if (totalPollCount == 0) 0.0 else option.count.toDouble() / totalPollCount * 100
        binding.txtPollCountPercent.text = String.format("%3.1f%%", percent)
        setUpImgChampion(isTop)
        setUpOptionExpandLayout()
        setUpOptionChoiceLayout()
        val animator = ObjectAnimator.ofFloat(binding.progressPollCount, "progress", 0.0f, option.count.toFloat())
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 1000
        animator.start()
        itemView.setOnClickListener(this)
    }

    private fun setUpImgChampion(isChampion: Boolean) {
        if (isChampion) {
            binding.imgChampion.visibility = View.VISIBLE
        } else {
            binding.imgChampion.visibility = View.INVISIBLE
        }
    }

    private fun setUpOptionChoiceLayout() {
        if (option.isUserChoiced || isChoice) {
            binding.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
            binding.progressPollCount.progressColor = ContextCompat.getColor(itemView.context, R.color.md_red_600)
            binding.progressPollCount.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_red_200)
        } else {
            binding.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
            binding.progressPollCount.progressColor = ContextCompat.getColor(itemView.context, R.color.md_blue_600)
            binding.progressPollCount.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_blue_200)
        }
    }

    private fun setUpOptionExpandLayout() {
        if (isExpand) {
            binding.txtOptionTitle.maxLines = 20
        } else {
            binding.txtOptionTitle.maxLines = 1
        }
    }

    override fun onClick(v: View) {
        itemListener.onOptionExpand(option.code ?: "")
        isExpand = !isExpand
        setUpOptionExpandLayout()
    }
}
