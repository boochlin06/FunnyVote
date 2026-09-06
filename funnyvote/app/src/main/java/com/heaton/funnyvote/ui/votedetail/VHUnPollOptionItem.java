package com.heaton.funnyvote.ui.votedetail;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.databinding.CardViewItemUnpollOptionsBinding;

/**
 * Created by heaton on 2016/8/22.
 */

public class VHUnPollOptionItem extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final CardViewItemUnpollOptionsBinding binding;
    private Option option;
    private boolean isChoice = false;
    private boolean isMultiChoice = false;
    private boolean isExpand = false;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public VHUnPollOptionItem(View itemView, boolean isMultiChoice
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        this(CardViewItemUnpollOptionsBinding.bind(itemView), isMultiChoice, itemListener);
    }

    public VHUnPollOptionItem(CardViewItemUnpollOptionsBinding binding, boolean isMultiChoice
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.isMultiChoice = isMultiChoice;
        this.itemListener = itemListener;

        binding.imgChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionChoice();
            }
        });

        binding.cardOption.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return VHUnPollOptionItem.this.onLongClick(v);
            }
        });
    }

    public void setLayout(boolean isChoice, boolean isExpand, Option option) {
        this.option = option;
        this.isChoice = isChoice;
        this.isExpand = isExpand;
        binding.txtOptionTitle.setText(option.getTitle());
        binding.txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        setUpOptionExpandLayout();
        setUpImgChoiceLayout();
        this.itemView.setOnClickListener(this);
    }

    public void onOptionChoice() {
        itemListener.onOptionChoice(option.getId(), option.getCode());
    }

    private void setUpImgChoiceLayout() {
        if (!isMultiChoice) {
            binding.imgChoice.setImageResource(isChoice ? R.drawable.ic_radio_button_checked_40dp
                    : R.drawable.ic_radio_button_unchecked_40dp);
        } else {
            binding.imgChoice.setImageResource(isChoice ? R.drawable.ic_check_box_40dp
                    : R.drawable.ic_check_box_outline_blank_40dp);
        }
        binding.cardOption.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(),
                isChoice ? R.color.md_red_100 : R.color.md_blue_100));
    }

    @Override
    public void onClick(View v) {
        if (binding.txtOptionTitle.getLineCount() == 1) {
            onOptionChoice();
        } else {
            itemListener.onOptionExpand(option.getCode());
        }
    }

    public boolean onLongClick(View v) {
        if (!isMultiChoice) {
            itemListener.onOptionQuickPoll(option.getId(), option.getCode());
        } else {
            onClick(v);
        }
        return true;
    }

    private void setUpOptionExpandLayout() {
        if (isExpand) {
            binding.txtOptionTitle.setMaxLines(20);
        } else {
            binding.txtOptionTitle.setMaxLines(1);
        }
    }
}
