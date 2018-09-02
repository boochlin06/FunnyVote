package com.heaton.funnyvote.ui.main

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.Glide
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.utils.Util
import kotlinx.android.synthetic.main.card_view_wall_item.view.*
import kotlinx.android.synthetic.main.include_author.view.*
import kotlinx.android.synthetic.main.include_function_bar.view.*

class VHVoteWallItem(var v: View
                     , private val wallItemListener: MainPageTabFragment.VoteWallItemListener)
    : RecyclerView.ViewHolder(v) {

    private lateinit var data: VoteData
    private val moveToVoteDetailOnClickListener = View.OnClickListener { wallItemListener.onVoteItemClick(data) }

    @SuppressLint("SetTextI18n")
    fun setLayout(data: VoteData) {
        this.data = data
        v.txtTitle.text = data.title
        v.imgBarFavorite.setImageResource(if (data.isFavorite)
            R.drawable.ic_star_24dp
        else
            R.drawable.ic_star_border_24dp)

        if (data.authorIcon == null || data.authorIcon.isEmpty()) {
            if (data.authorName != null && !data.authorName.isEmpty()) {
                val drawable = TextDrawable.builder().beginConfig().width(36).height(36).endConfig()
                        .buildRound(data.authorName.substring(0, 1), R.color.primary_light)
                v.imgAuthorIcon.setImageDrawable(drawable)
            } else {
                v.imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp)
            }
        } else {
            Glide.with(itemView.context)
                    .load(data.authorIcon)
                    .override(itemView.context.resources
                            .getDimension(R.dimen.vote_image_author_size).toInt(), itemView.context.resources
                            .getDimension(R.dimen.vote_image_author_size).toInt())
                    .fitCenter()
                    .crossFade()
                    .into(v.imgAuthorIcon)
        }

        v.txtAuthorName.text = data.authorName

        if (VoteData.SECURITY_PUBLIC == data.security) {
            v.imgLock.visibility = View.INVISIBLE
        } else {
            v.imgLock.visibility = View.VISIBLE
        }

        if (data.voteImage == null || data.voteImage.isEmpty()) {
            v.imgMain.setImageResource(data.localImage)
        } else {
            Glide.with(itemView.context)
                    .load(data.voteImage)
                    .override(itemView.context.resources
                            .getDimension(R.dimen.vote_image_main_width).toInt(), itemView.context.resources
                            .getDimension(R.dimen.vote_image_main_height).toInt())
                    .crossFade()
                    .into(v.imgMain)
        }

        // Check vote is end.
        if (data.endTime < System.currentTimeMillis()) {
            v.txtPubTime.text = itemView.context.getString(R.string.wall_item_vote_end)
            v.txtPubTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_red_700))
            //txtPubTime.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.md_red_700));
        } else {
            v.txtPubTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_text))
            //txtPubTime.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.secondary_text));
            v.txtPubTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd hh:mm")
                    + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd hh:mm"))
        }

        v.txtBarPollCount.text = String.format(itemView.context
                .getString(R.string.wall_item_bar_vote_count), data.pollCount)

        setUpOptionArea()

        itemView.setOnClickListener(moveToVoteDetailOnClickListener)

        v.relBarFavorite.setOnClickListener {
            if (!Util.isNetworkConnected(itemView.context)) {
                Toast.makeText(itemView.context
                        , R.string.toast_network_connect_error_favorite, Toast.LENGTH_SHORT).show()
            }
            data.isFavorite = !data.isFavorite
            wallItemListener.onVoteFavoriteChange(data)
        }
        v.relBarShare.setOnClickListener { wallItemListener.onVoteShare(data) }

        v.imgAuthorIcon.setOnClickListener {
            wallItemListener.onVoteAuthorClick(data)
        }
        v.txtAuthorName.setOnClickListener {
            wallItemListener.onVoteAuthorClick(data)
        }
        v.btnFirstOption.setOnLongClickListener { optionButton: View -> onOptionLongClick(optionButton) }
        v.btnSecondOption.setOnLongClickListener { optionButton: View -> onOptionLongClick(optionButton) }
        v.btnThirdOption.setOnLongClickListener { optionButton: View -> onOptionLongClick(optionButton) }
    }

    private fun setUpOptionArea() {
        setUpOptionArea(false)
    }

    @SuppressLint("DefaultLocale")
    private fun setUpOptionArea(isQuickPoll: Boolean) {
        // More than 3 options.
        if (data.optionCount > 2) {
            if (data.isPolled || data.endTime < System.currentTimeMillis()) {
                val isShowTopOption: Boolean

                v.progressFirstOption.visibility = View.VISIBLE
                v.progressFirstOption.max = data.pollCount.toFloat()

                v.progressSecondOption.visibility = View.VISIBLE
                v.progressSecondOption.max = data.pollCount.toFloat()

                v.txtFirstPollCountPercent.visibility = View.VISIBLE
                v.txtSecondPollCountPercent.visibility = View.VISIBLE

                if (isQuickPoll) {
                    // show option 1 at first button , and option 2 at second button.
                    v.progressFirstOption.visibility = View.VISIBLE
                    v.progressFirstOption.progress = data.option1Count.toFloat()

                    v.progressSecondOption.visibility = View.VISIBLE
                    v.progressSecondOption.progress = data.option2Count.toFloat()

                    v.txtFirstPollCountPercent.visibility = View.VISIBLE
                    v.txtSecondPollCountPercent.visibility = View.VISIBLE

                    val percent1: Double = if (data.pollCount == 0)
                        0.0
                    else
                        data.option1Count.toDouble() / data.pollCount * 100
                    val percent2: Double = if (data.pollCount == 0)
                        0.0
                    else
                        data.option2Count.toDouble() / data.pollCount * 100
                    v.txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)
                    v.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)
                    setUpFirstButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                            && data.optionTopCount == data.option1Count
                            && data.option1Count != 0)
                    setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                            && data.optionTopCount == data.option2Count
                            && data.option2Count != 0)
                    setUpFirstButtonProgressLayout(data.option1Polled)
                    setUpSecondButtonProgressLayout(data.option2Polled)
                    return
                }

                // If not quick poll First button is top option , if no one poll , show first option.
                if (!TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount != 0) {
                    // show top top option at second button.
                    isShowTopOption = true
                    v.txtFirstOptionTitle.text = data.optionTopTitle
                    v.progressFirstOption.progress = data.optionTopCount.toFloat()
                    val percentTop: Double = if (data.pollCount == 0)
                        0.0
                    else
                        data.optionTopCount.toDouble() / data.pollCount * 100
                    v.txtFirstPollCountPercent.text = String.format("%3.1f%%", percentTop)

                    setUpFirstButtonTopLayout(data.optionTopCount != 0)
                    setUpFirstButtonProgressLayout(data.optionTopPolled)
                } else {
                    // show option 1 at second button.
                    isShowTopOption = false
                    v.txtFirstOptionTitle.text = data.option1Title
                    v.progressFirstOption.progress = data.option1Count.toFloat()
                    val percent1: Double = if (data.pollCount == 0)
                        0.0
                    else
                        data.option1Count.toDouble() / data.pollCount * 100
                    v.txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)

                    setUpFirstButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                            && data.optionTopCount == data.option1Count
                            && data.option1Count != 0)
                    setUpFirstButtonProgressLayout(data.option1Polled)
                }

                // Second button is user choice if user no choice show one or second option.
                // should be different with first button.
                // if first button show first option , so no one poll ,user choice and top is 0 vote.
                // set button 2 layout.
                if (isShowTopOption) {
                    if (data.optionTopCode != data.optionUserChoiceCode && !TextUtils.isEmpty(data.optionUserChoiceCode)) {
                        // show user choice option at second button.
                        v.txtSecondOptionTitle.text = data.optionUserChoiceTitle
                        v.progressSecondOption.progress = data.optionUserChoiceCount.toFloat()
                        val percentUserChoice = if (data.pollCount == 0)
                            0.0
                        else
                            data.optionUserChoiceCount.toDouble() / data.pollCount * 100
                        v.txtSecondPollCountPercent.text = String.format("%3.1f%%", percentUserChoice)

                        setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                                && data.optionTopCount == data.optionUserChoiceCount
                                && data.optionUserChoiceCount != 0)
                        setUpSecondButtonProgressLayout(true)
                    } else if (data.optionTopCode != data.option1Code) {
                        // show option 1 at second button.
                        v.txtSecondOptionTitle.text = data.option1Title
                        v.progressSecondOption.progress = data.option1Count.toFloat()
                        val percent1 = if (data.pollCount == 0)
                            0.0
                        else {
                            data.option1Count.toDouble() / data.pollCount * 100
                        }
                        v.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent1)

                        setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                                && data.optionTopCount == data.option1Count
                                && data.option1Count != 0)
                        setUpSecondButtonProgressLayout(data.option1Polled)
                    } else if (data.optionTopCode != data.option2Code) {
                        // show option 2 at second button.
                        v.txtSecondOptionTitle.text = data.option2Title
                        v.progressSecondOption.progress = data.option2Count.toFloat()
                        val percent2 = if (data.pollCount == 0)
                            0.0
                        else {
                            data.option2Count.toDouble() / data.pollCount * 100
                        }
                        v.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)

                        setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                                && data.optionTopCount == data.option2Count
                                && data.option2Count != 0)
                        setUpSecondButtonProgressLayout(data.option2Polled)
                    }

                } else {
                    // show option 2 at second button.
                    v.txtSecondOptionTitle.text = data.option2Title
                    v.progressSecondOption.progress = data.option2Count.toFloat()
                    val percent2: Double = if (data.pollCount == 0)
                        0.0
                    else {
                        data.option2Count.toDouble() / data.pollCount * 100
                    }
                    v.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)

                    setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                            && data.optionTopCount == data.option2Count
                            && data.option2Count != 0)
                    setUpSecondButtonProgressLayout(data.option2Polled)
                }
                v.imgThirdOption.visibility = View.GONE
            } else {
                // vote is not end or not poll.
                v.txtFirstOptionTitle.text = data.option1Title

                v.txtSecondOptionTitle.text = data.option2Title

                v.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
                v.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))

                v.txtFirstPollCountPercent.visibility = View.GONE
                v.txtSecondPollCountPercent.visibility = View.GONE

                v.progressFirstOption.visibility = View.GONE
                v.progressSecondOption.visibility = View.GONE

                v.imgChampion1.visibility = View.GONE
                v.imgChampion2.visibility = View.GONE

                if (data.isUserCanAddOption) {
                    v.imgThirdOption.visibility = View.VISIBLE
                } else {
                    v.imgThirdOption.visibility = View.GONE
                }
            }
            v.txtThirdOption.text = String.format(itemView.context.getString(R.string.wall_item_other_option), data.optionCount - 2)
            v.txtThirdOption.visibility = View.VISIBLE
            v.btnThirdOption.visibility = View.VISIBLE
            v.btnThirdOption.setOnClickListener(moveToVoteDetailOnClickListener)
        } else {
            // 2 option type.
            v.txtFirstOptionTitle.text = data.option1Title
            v.txtSecondOptionTitle.text = data.option2Title
            v.progressFirstOption.max = data.pollCount.toFloat()
            v.progressSecondOption.max = data.pollCount.toFloat()

            if (data.isPolled || data.endTime < System.currentTimeMillis()) {
                // show option 1 at first button , and option 2 at second button.
                v.progressFirstOption.visibility = View.VISIBLE
                v.progressFirstOption.progress = data.option1Count.toFloat()

                v.progressSecondOption.visibility = View.VISIBLE
                v.progressSecondOption.progress = data.option2Count.toFloat()

                v.txtFirstPollCountPercent.visibility = View.VISIBLE
                v.txtSecondPollCountPercent.visibility = View.VISIBLE

                val percent1: Double = if (data.pollCount == 0)
                    0.0
                else
                    data.option1Count.toDouble() / data.pollCount * 100
                val percent2: Double = if (data.pollCount == 0)
                    0.0
                else
                    data.option2Count.toDouble() / data.pollCount * 100
                v.txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)
                v.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)
                setUpFirstButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                        && data.optionTopCount == data.option1Count
                        && data.option1Count != 0)
                setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.optionTopCode)
                        && data.optionTopCount == data.option2Count
                        && data.option2Count != 0)
                setUpFirstButtonProgressLayout(data.option1Polled)
                setUpSecondButtonProgressLayout(data.option2Polled)

                v.btnThirdOption.visibility = View.GONE
            } else {
                v.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
                v.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))

                v.txtFirstPollCountPercent.visibility = View.GONE
                v.txtSecondPollCountPercent.visibility = View.GONE

                v.progressFirstOption.visibility = View.GONE
                v.progressSecondOption.visibility = View.GONE

                v.imgChampion1.visibility = View.GONE
                v.imgChampion2.visibility = View.GONE

                if (data.isUserCanAddOption) {
                    v.imgThirdOption.visibility = View.VISIBLE
                    v.btnThirdOption.visibility = View.VISIBLE
                    v.txtThirdOption.visibility = View.GONE
                } else {
                    v.txtThirdOption.visibility = View.GONE
                    v.imgThirdOption.visibility = View.GONE
                    v.btnThirdOption.visibility = View.GONE
                }
            }
            v.btnThirdOption.setOnClickListener(moveToVoteDetailOnClickListener)
        }
    }

    private fun setUpFirstButtonTopLayout(isTop: Boolean) {
        if (isTop) {
            v.imgChampion1.visibility = View.VISIBLE
        } else {
            v.imgChampion1.visibility = View.INVISIBLE
        }
    }

    private fun setUpSecondButtonTopLayout(isTop: Boolean) {
        if (isTop) {
            v.imgChampion2.visibility = View.VISIBLE
        } else {
            v.imgChampion2.visibility = View.INVISIBLE
        }
    }

    private fun setUpFirstButtonProgressLayout(isPolled: Boolean) {
        if (isPolled) {
            v.progressFirstOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_red_600)
            v.progressFirstOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_red_200)
            v.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
        } else {
            v.progressFirstOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_blue_600)
            v.progressFirstOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_blue_200)
            v.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
        }
    }

    private fun setUpSecondButtonProgressLayout(isPolled: Boolean) {
        if (isPolled) {
            v.progressSecondOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_red_600)
            v.progressSecondOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_red_200)
            v.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
        } else {
            v.progressSecondOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_blue_600)
            v.progressSecondOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_blue_200)
            v.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
        }
    }

    private fun onOptionLongClick(optionButton: View): Boolean {
        if (!(data.minOption == 1 && data.maxOption == 1)
                || data.isPolled || data.endTime < System.currentTimeMillis()
                || optionButton.id == R.id.btnThirdOption) {
            wallItemListener.onVoteItemClick(data)
            //startActivityToVoteDetail(optionButton.getContext(), data.getVoteCode());
            return true
        }
        if (!data.isPolled) {
            if (!Util.isNetworkConnected(itemView.context)) {
                Toast.makeText(itemView.context, R.string.toast_network_connect_error_quick_poll, Toast.LENGTH_SHORT).show()
                return true
            } else {
                if (optionButton.id == R.id.btnFirstOption) {
                    wallItemListener.onVoteQuickPoll(data, data.option1Code)

                } else {
                    wallItemListener.onVoteQuickPoll(data, data.option2Code)
                }

            }
        }
        return true
    }

}