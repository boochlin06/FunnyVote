package com.heaton.funnyvote.ui.main

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.bumptech.glide.Glide
import com.heaton.funnyvote.R
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.databinding.CardViewWallItemBinding
import com.heaton.funnyvote.utils.Util

class VHVoteWallItem(
    val binding: CardViewWallItemBinding,
    private val wallItemListener: MainPageTabFragment.VoteWallItemListener
) : RecyclerView.ViewHolder(binding.root) {

    constructor(v: View, wallItemListener: MainPageTabFragment.VoteWallItemListener) :
            this(CardViewWallItemBinding.bind(v), wallItemListener)

    private lateinit var data: VoteData
    private val moveToVoteDetailOnClickListener = View.OnClickListener { wallItemListener.onVoteItemClick(data) }

    @SuppressLint("SetTextI18n")
    fun setLayout(data: VoteData) {
        this.data = data
        binding.txtTitle.text = data.title
        binding.functionBar.imgBarFavorite.setImageResource(
            if (data.isFavorite) R.drawable.ic_star_24dp else R.drawable.ic_star_border_24dp
        )

        if (data.authorIcon.isNullOrEmpty()) {
            if (!data.authorName.isNullOrEmpty()) {
                val drawable = Util.createRoundTextDrawable(data.authorName.substring(0, 1), ContextCompat.getColor(itemView.context, R.color.primary_light), 36)
                binding.authorBar.imgAuthorIcon.setImageDrawable(drawable)
            } else {
                binding.authorBar.imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp)
            }
        } else {
            Glide.with(itemView.context)
                .load(data.authorIcon)
                .override(
                    itemView.context.resources.getDimension(R.dimen.vote_image_author_size).toInt(),
                    itemView.context.resources.getDimension(R.dimen.vote_image_author_size).toInt()
                )
                .fitCenter()
                .into(binding.authorBar.imgAuthorIcon)
        }

        binding.authorBar.txtAuthorName.text = data.authorName

        if (VoteData.SECURITY_PUBLIC == data.security) {
            binding.authorBar.imgLock.visibility = View.INVISIBLE
        } else {
            binding.authorBar.imgLock.visibility = View.VISIBLE
        }

        if (data.voteImage.isNullOrEmpty()) {
            binding.imgMain.setImageResource(data.localImage)
        } else {
            Glide.with(itemView.context)
                .load(data.voteImage)
                .override(
                    itemView.context.resources.getDimension(R.dimen.vote_image_main_width).toInt(),
                    itemView.context.resources.getDimension(R.dimen.vote_image_main_height).toInt()
                )
                .into(binding.imgMain)
        }

        // Check vote is end.
        if (data.endTime < System.currentTimeMillis()) {
            binding.authorBar.txtPubTime.text = itemView.context.getString(R.string.wall_item_vote_end)
            binding.authorBar.txtPubTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.md_red_700))
        } else {
            binding.authorBar.txtPubTime.setTextColor(ContextCompat.getColor(itemView.context, R.color.secondary_text))
            binding.authorBar.txtPubTime.text = (Util.getDate(data.startTime, "yyyy/MM/dd HH:mm")
                    + " ~ " + Util.getDate(data.endTime, "yyyy/MM/dd HH:mm"))
        }

        binding.functionBar.txtBarPollCount.text = String.format(
            itemView.context.getString(R.string.wall_item_bar_vote_count), data.pollCount
        )

        setUpOptionArea()

        itemView.setOnClickListener(moveToVoteDetailOnClickListener)

        binding.functionBar.relBarFavorite.setOnClickListener {
            if (!Util.isNetworkConnected(itemView.context)) {
                Toast.makeText(
                    itemView.context,
                    R.string.toast_network_connect_error_favorite,
                    Toast.LENGTH_SHORT
                ).show()
            }
            data.isFavorite = !data.isFavorite
            wallItemListener.onVoteFavoriteChange(data)
        }
        binding.functionBar.relBarShare.setOnClickListener { wallItemListener.onVoteShare(data) }

        binding.authorBar.imgAuthorIcon.setOnClickListener {
            wallItemListener.onVoteAuthorClick(data)
        }
        binding.authorBar.txtAuthorName.setOnClickListener {
            wallItemListener.onVoteAuthorClick(data)
        }
        binding.btnFirstOption.setOnLongClickListener { optionButton: View -> onOptionLongClick(optionButton) }
        binding.btnSecondOption.setOnLongClickListener { optionButton: View -> onOptionLongClick(optionButton) }
        binding.btnThirdOption.setOnLongClickListener { optionButton: View -> onOptionLongClick(optionButton) }
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

                binding.progressFirstOption.visibility = View.VISIBLE
                binding.progressFirstOption.max = data.pollCount.toFloat()

                binding.progressSecondOption.visibility = View.VISIBLE
                binding.progressSecondOption.max = data.pollCount.toFloat()

                binding.txtFirstPollCountPercent.visibility = View.VISIBLE
                binding.txtSecondPollCountPercent.visibility = View.VISIBLE

                if (isQuickPoll) {
                    binding.progressFirstOption.visibility = View.VISIBLE
                    binding.progressFirstOption.progress = data.option1Count.toFloat()

                    binding.progressSecondOption.visibility = View.VISIBLE
                    binding.progressSecondOption.progress = data.option2Count.toFloat()

                    binding.txtFirstPollCountPercent.visibility = View.VISIBLE
                    binding.txtSecondPollCountPercent.visibility = View.VISIBLE

                    val percent1: Double = if (data.pollCount == 0) 0.0 else data.option1Count.toDouble() / data.pollCount * 100
                    val percent2: Double = if (data.pollCount == 0) 0.0 else data.option2Count.toDouble() / data.pollCount * 100
                    binding.txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)
                    binding.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)
                    setUpFirstButtonTopLayout(
                        !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option1Count && data.option1Count != 0
                    )
                    setUpSecondButtonTopLayout(
                        !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option2Count && data.option2Count != 0
                    )
                    setUpFirstButtonProgressLayout(data.option1Polled)
                    setUpSecondButtonProgressLayout(data.option2Polled)
                    return
                }

                if (!TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount != 0) {
                    isShowTopOption = true
                    binding.txtFirstOptionTitle.text = data.optionTopTitle
                    binding.progressFirstOption.progress = data.optionTopCount.toFloat()
                    val percentTop: Double = if (data.pollCount == 0) 0.0 else data.optionTopCount.toDouble() / data.pollCount * 100
                    binding.txtFirstPollCountPercent.text = String.format("%3.1f%%", percentTop)

                    setUpFirstButtonTopLayout(data.optionTopCount != 0)
                    setUpFirstButtonProgressLayout(data.optionTopPolled)
                } else {
                    isShowTopOption = false
                    binding.txtFirstOptionTitle.text = data.option1Title
                    binding.progressFirstOption.progress = data.option1Count.toFloat()
                    val percent1: Double = if (data.pollCount == 0) 0.0 else data.option1Count.toDouble() / data.pollCount * 100
                    binding.txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)

                    setUpFirstButtonTopLayout(
                        !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option1Count && data.option1Count != 0
                    )
                    setUpFirstButtonProgressLayout(data.option1Polled)
                }

                if (isShowTopOption) {
                    if (data.optionTopCode != data.optionUserChoiceCode && !TextUtils.isEmpty(data.optionUserChoiceCode)) {
                        binding.txtSecondOptionTitle.text = data.optionUserChoiceTitle
                        binding.progressSecondOption.progress = data.optionUserChoiceCount.toFloat()
                        val percentUserChoice = if (data.pollCount == 0) 0.0 else data.optionUserChoiceCount.toDouble() / data.pollCount * 100
                        binding.txtSecondPollCountPercent.text = String.format("%3.1f%%", percentUserChoice)

                        setUpSecondButtonTopLayout(
                            !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.optionUserChoiceCount && data.optionUserChoiceCount != 0
                        )
                        setUpSecondButtonProgressLayout(true)
                    } else if (data.optionTopCode != data.option1Code) {
                        binding.txtSecondOptionTitle.text = data.option1Title
                        binding.progressSecondOption.progress = data.option1Count.toFloat()
                        val percent1 = if (data.pollCount == 0) 0.0 else data.option1Count.toDouble() / data.pollCount * 100
                        binding.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent1)

                        setUpSecondButtonTopLayout(
                            !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option1Count && data.option1Count != 0
                        )
                        setUpSecondButtonProgressLayout(data.option1Polled)
                    } else if (data.optionTopCode != data.option2Code) {
                        binding.txtSecondOptionTitle.text = data.option2Title
                        binding.progressSecondOption.progress = data.option2Count.toFloat()
                        val percent2 = if (data.pollCount == 0) 0.0 else data.option2Count.toDouble() / data.pollCount * 100
                        binding.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)

                        setUpSecondButtonTopLayout(
                            !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option2Count && data.option2Count != 0
                        )
                        setUpSecondButtonProgressLayout(data.option2Polled)
                    }
                } else {
                    binding.txtSecondOptionTitle.text = data.option2Title
                    binding.progressSecondOption.progress = data.option2Count.toFloat()
                    val percent2: Double = if (data.pollCount == 0) 0.0 else data.option2Count.toDouble() / data.pollCount * 100
                    binding.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)

                    setUpSecondButtonTopLayout(
                        !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option2Count && data.option2Count != 0
                    )
                    setUpSecondButtonProgressLayout(data.option2Polled)
                }
                binding.imgThirdOption.visibility = View.GONE
            } else {
                binding.txtFirstOptionTitle.text = data.option1Title
                binding.txtSecondOptionTitle.text = data.option2Title

                binding.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
                binding.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))

                binding.txtFirstPollCountPercent.visibility = View.GONE
                binding.txtSecondPollCountPercent.visibility = View.GONE

                binding.progressFirstOption.visibility = View.GONE
                binding.progressSecondOption.visibility = View.GONE

                binding.imgChampion1.visibility = View.GONE
                binding.imgChampion2.visibility = View.GONE

                if (data.isUserCanAddOption) {
                    binding.imgThirdOption.visibility = View.VISIBLE
                } else {
                    binding.imgThirdOption.visibility = View.GONE
                }
            }
            binding.txtThirdOption.text = String.format(itemView.context.getString(R.string.wall_item_other_option), data.optionCount - 2)
            binding.txtThirdOption.visibility = View.VISIBLE
            binding.btnThirdOption.visibility = View.VISIBLE
            binding.btnThirdOption.setOnClickListener(moveToVoteDetailOnClickListener)
        } else {
            // 2 option type.
            binding.txtFirstOptionTitle.text = data.option1Title
            binding.txtSecondOptionTitle.text = data.option2Title
            binding.progressFirstOption.max = data.pollCount.toFloat()
            binding.progressSecondOption.max = data.pollCount.toFloat()

            if (data.isPolled || data.endTime < System.currentTimeMillis()) {
                binding.progressFirstOption.visibility = View.VISIBLE
                binding.progressFirstOption.progress = data.option1Count.toFloat()

                binding.progressSecondOption.visibility = View.VISIBLE
                binding.progressSecondOption.progress = data.option2Count.toFloat()

                binding.txtFirstPollCountPercent.visibility = View.VISIBLE
                binding.txtSecondPollCountPercent.visibility = View.VISIBLE

                val percent1: Double = if (data.pollCount == 0) 0.0 else data.option1Count.toDouble() / data.pollCount * 100
                val percent2: Double = if (data.pollCount == 0) 0.0 else data.option2Count.toDouble() / data.pollCount * 100
                binding.txtFirstPollCountPercent.text = String.format("%3.1f%%", percent1)
                binding.txtSecondPollCountPercent.text = String.format("%3.1f%%", percent2)
                setUpFirstButtonTopLayout(
                    !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option1Count && data.option1Count != 0
                )
                setUpSecondButtonTopLayout(
                    !TextUtils.isEmpty(data.optionTopCode) && data.optionTopCount == data.option2Count && data.option2Count != 0
                )
                setUpFirstButtonProgressLayout(data.option1Polled)
                setUpSecondButtonProgressLayout(data.option2Polled)

                binding.btnThirdOption.visibility = View.GONE
            } else {
                binding.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
                binding.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))

                binding.txtFirstPollCountPercent.visibility = View.GONE
                binding.txtSecondPollCountPercent.visibility = View.GONE

                binding.progressFirstOption.visibility = View.GONE
                binding.progressSecondOption.visibility = View.GONE

                binding.imgChampion1.visibility = View.GONE
                binding.imgChampion2.visibility = View.GONE

                if (data.isUserCanAddOption) {
                    binding.imgThirdOption.visibility = View.VISIBLE
                    binding.btnThirdOption.visibility = View.VISIBLE
                    binding.txtThirdOption.visibility = View.GONE
                } else {
                    binding.txtThirdOption.visibility = View.GONE
                    binding.imgThirdOption.visibility = View.GONE
                    binding.btnThirdOption.visibility = View.GONE
                }
            }
            binding.btnThirdOption.setOnClickListener(moveToVoteDetailOnClickListener)
        }
    }

    private fun setUpFirstButtonTopLayout(isTop: Boolean) {
        if (isTop) {
            binding.imgChampion1.visibility = View.VISIBLE
        } else {
            binding.imgChampion1.visibility = View.INVISIBLE
        }
    }

    private fun setUpSecondButtonTopLayout(isTop: Boolean) {
        if (isTop) {
            binding.imgChampion2.visibility = View.VISIBLE
        } else {
            binding.imgChampion2.visibility = View.INVISIBLE
        }
    }

    private fun setUpFirstButtonProgressLayout(isPolled: Boolean) {
        if (isPolled) {
            binding.progressFirstOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_red_600)
            binding.progressFirstOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_red_200)
            binding.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
        } else {
            binding.progressFirstOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_blue_600)
            binding.progressFirstOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_blue_200)
            binding.btnFirstOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
        }
    }

    private fun setUpSecondButtonProgressLayout(isPolled: Boolean) {
        if (isPolled) {
            binding.progressSecondOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_red_600)
            binding.progressSecondOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_red_200)
            binding.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_red_100))
        } else {
            binding.progressSecondOption.progressColor = ContextCompat.getColor(itemView.context, R.color.md_blue_600)
            binding.progressSecondOption.progressBackgroundColor = ContextCompat.getColor(itemView.context, R.color.md_blue_200)
            binding.btnSecondOption.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.md_blue_100))
        }
    }

    private fun onOptionLongClick(optionButton: View): Boolean {
        if (!(data.minOption == 1 && data.maxOption == 1)
            || data.isPolled || data.endTime < System.currentTimeMillis()
            || optionButton.id == R.id.btnThirdOption
        ) {
            wallItemListener.onVoteItemClick(data)
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
