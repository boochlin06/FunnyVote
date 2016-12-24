package com.android.heaton.funnyvote.ui.votedetail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.android.heaton.funnyvote.eventbus.EventBusController.OptionChoiceEvent.OPTION_CHOICED;

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
    private Option option;
    private boolean isChoice = false;
    private boolean isMultiChoice = false;
    private boolean isExpand = false;

    public VHUnPollOptionItem(View itemView, boolean isMultiChoice) {
        super(itemView);
        this.isMultiChoice = isMultiChoice;
        ButterKnife.bind(this, itemView);
    }

    public void setLayout(boolean isChoice, boolean isExpand, Option option) {
        this.option = option;
        this.isChoice = isChoice;
        this.isExpand = isExpand;
        txtOptionTitle.setText(option.getTitle());
        txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        setUpOptionExpandLayout();
        setUpImgChoiceLaout();
        this.itemView.setOnClickListener(this);
    }

    @OnClick(R.id.imgChoice)
    public void onOptionChoice() {
        setUpImgChoiceLaout();
        EventBus.getDefault().post(new EventBusController.OptionChoiceEvent(option.getId(), OPTION_CHOICED, option.getCode()));
    }

    private void setUpImgChoiceLaout() {
        if (!isMultiChoice) {
            imgChoice.setImageResource(isChoice ? R.drawable.ic_radio_button_checked_40dp
                    : R.drawable.ic_radio_button_unchecked_40dp);
        } else {
            imgChoice.setImageResource(isChoice ? R.drawable.ic_check_box_40dp
                    : R.drawable.ic_check_box_outline_blank_40dp);
        }
    }

    @Override
    public void onClick(View v) {
        if (txtOptionTitle.getLineCount() > 1) {
            EventBus.getDefault().post(new EventBusController
                    .OptionChoiceEvent(option.getId(), EventBusController.OptionChoiceEvent.OPTION_EXPAND, option.getCode()));
            isExpand = !isExpand;
            setUpOptionExpandLayout();
        } else {
            onOptionChoice();
        }
    }

    private void setUpOptionExpandLayout() {
        if (isExpand) {
            txtOptionTitle.setMaxLines(20);
        } else {
            txtOptionTitle.setMaxLines(1);
        }
    }
}
