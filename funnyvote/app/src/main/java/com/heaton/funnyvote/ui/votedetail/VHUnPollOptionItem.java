package com.heaton.funnyvote.ui.votedetail;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;


/**
 * Created by heaton on 2016/8/22.
 */

public class VHUnPollOptionItem extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.txtOptionNumber)
    TextView txtOptionNumber;
    @BindView(R.id.imgChoice)
    ImageView imgChoice;
    @BindView(R.id.txtOptionTitle)
    TextView txtOptionTitle;
    @BindView(R.id.cardOption)
    CardView cardOption;
    private Option option;
    private boolean isChoice = false;
    private boolean isMultiChoice = false;
    private boolean isExpand = false;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public VHUnPollOptionItem(View itemView, boolean isMultiChoice
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        super(itemView);
        this.isMultiChoice = isMultiChoice;
        this.itemListener = itemListener;
        ButterKnife.bind(this, itemView);
    }

    public void setLayout(boolean isChoice, boolean isExpand, Option option) {
        this.option = option;
        this.isChoice = isChoice;
        this.isExpand = isExpand;
        txtOptionTitle.setText(option.getTitle());
        txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        setUpOptionExpandLayout();
        setUpImgChoiceLayout();
        this.itemView.setOnClickListener(this);
    }

    @OnClick(R.id.imgChoice)
    public void onOptionChoice() {
        itemListener.onOptionChoice(option.getId(), option.getCode());
    }

    private void setUpImgChoiceLayout() {
        if (!isMultiChoice) {
            imgChoice.setImageResource(isChoice ? R.drawable.ic_radio_button_checked_40dp
                    : R.drawable.ic_radio_button_unchecked_40dp);
        } else {
            imgChoice.setImageResource(isChoice ? R.drawable.ic_check_box_40dp
                    : R.drawable.ic_check_box_outline_blank_40dp);
        }
        cardOption.setCardBackgroundColor(itemView.getResources()
                .getColor(isChoice ? R.color.md_red_100 : R.color.md_blue_100));
    }

    @Override
    public void onClick(View v) {
        if (txtOptionTitle.getLineCount() == 1) {
            onOptionChoice();
        } else {
            itemListener.onOptionExpand(option.getCode());
        }
    }

    @OnLongClick(R.id.cardOption)
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
            txtOptionTitle.setMaxLines(20);
        } else {
            txtOptionTitle.setMaxLines(1);
        }
    }
}
