package com.heaton.funnyvote.ui.votedetail

import android.animation.ObjectAnimator
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.Option
import kotlinx.android.synthetic.main.card_view_item_result_option.view.*

/**
 * Created by heaton on 2016/10/20.
 */

class VHResultOptionItem(
        itemView: View, private val totalPollCount: Int,
        private val itemListener: VoteDetailContentActivity.OptionItemListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    private var isChoice = false
    private var isExpand = false
    private lateinit var option: Option

    fun setLayout(isChoice: Boolean, isExpand: Boolean, isTop: Boolean, option: Option) {
        this.isChoice = isChoice
        this.isExpand = isExpand
        this.option = option
        itemView.progressPollCount.max = totalPollCount.toFloat()
        itemView.txtOptionTitle.text = option.title
        itemView.txtOptionNumber.text = (adapterPosition + 1).toString()
        itemView.txtPollCount.text = option.count.toString()
        val percent = if (totalPollCount == 0) 0.0 else option.count.toDouble() / totalPollCount * 100
        itemView.txtPollCountPercent!!.text = String.format("%3.1f%%", percent)
        setUpImgChampion(isTop)
        setUpOptionExpandLayout()
        setUpOptionChoiceLayout()
        val animator = ObjectAnimator
                .ofFloat(itemView.progressPollCount, "progress", 0.0f, option.count.toFloat())
        animator.interpolator = DecelerateInterpolator()
        animator.duration = 1000
        animator.start()
        itemView.setOnClickListener(this)
    }

    private fun setUpImgChampion(isChampion: Boolean) {
        if (isChampion) {
            itemView.imgChampion.visibility = View.VISIBLE
        } else {
            itemView.imgChampion.visibility = View.INVISIBLE
        }
    }

    private fun setUpOptionChoiceLayout() = if (option.isUserChoiced || isChoice) {
        itemView.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
        itemView.progressPollCount.progressColor = ContextCompat.getColor(itemView.context, R.color.md_red_600)
        itemView.progressPollCount.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_red_200)
    } else {
        itemView.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
        itemView.progressPollCount.progressColor = ContextCompat.getColor(itemView.context, R.color.md_blue_600)
        itemView.progressPollCount.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_blue_200)
    }

    private fun setUpOptionExpandLayout() = if (isExpand) {
        itemView.txtOptionTitle.maxLines = 20
    } else {
        itemView.txtOptionTitle.maxLines = 1
    }

    override fun onClick(v: View) {
        itemListener.onOptionExpand(option.code)
    }
}
