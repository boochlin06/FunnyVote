package com.heaton.funnyvote.ui.votedetail;

import android.animation.ObjectAnimator;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.databinding.CardViewItemResultOptionBinding;

/**
 * Created by heaton on 2016/10/20.
 */

public class VHResultOptionItem extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final CardViewItemResultOptionBinding binding;
    private boolean isChoice = false;
    private boolean isExpand = false;
    private int totalPollCount;
    private Option option;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public VHResultOptionItem(View itemView, int totalPollCount
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        this(CardViewItemResultOptionBinding.bind(itemView), totalPollCount, itemListener);
    }

    public VHResultOptionItem(CardViewItemResultOptionBinding binding, int totalPollCount
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.totalPollCount = totalPollCount;
        this.itemListener = itemListener;
        binding.progressPollCount.setMax(totalPollCount);
    }

    public void setLayout(boolean isChoice, boolean isExpand, boolean isTop, Option option) {
        this.isChoice = isChoice;
        this.isExpand = isExpand;
        this.option = option;
        binding.txtOptionTitle.setText(option.getTitle());
        binding.txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        binding.txtPollCount.setText(Integer.toString(option.getCount()));
        double percent = totalPollCount == 0 ? 0 : (double) option.getCount() / totalPollCount * 100;
        binding.txtPollCountPercent.setText(String.format("%3.1f%%", percent));
        setUpImgChampion(isTop);
        setUpOptionExpandLayout();
        setUpOptionChoiceLayout();
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.progressPollCount, "progress", 0, option.getCount());
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1000);
        animator.start();
        itemView.setOnClickListener(this);
    }

    private void setUpImgChampion(boolean isChampion) {
        if (isChampion) {
            binding.imgChampion.setVisibility(View.VISIBLE);
        } else {
            binding.imgChampion.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpOptionChoiceLayout() {
        if (option.getIsUserChoiced() || isChoice) {
            binding.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.md_red_100));
            binding.progressPollCount.setProgressColor(ContextCompat.getColor(itemView.getContext(), R.color.md_red_600));
            binding.progressPollCount.setProgressBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.md_red_200));
        } else {
            binding.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.md_blue_100));
            binding.progressPollCount.setProgressColor(ContextCompat.getColor(itemView.getContext(), R.color.md_blue_600));
            binding.progressPollCount.setProgressBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.md_blue_200));
        }
    }

    private void setUpOptionExpandLayout() {
        if (isExpand) {
            binding.txtOptionTitle.setMaxLines(20);
        } else {
            binding.txtOptionTitle.setMaxLines(1);
        }
    }

    @Override
    public void onClick(View v) {
        itemListener.onOptionExpand(option.getCode());
    }
}
