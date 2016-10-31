package com.android.heaton.funnyvote.ui.votedetail;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2016/10/20.
 */

public class VHResultOptionItem extends RecyclerView.ViewHolder implements View.OnClickListener {

    @BindView(R.id.txtOptionNumber)
    TextView txtOptionNumber;
    @BindView(R.id.imgPollChoice)
    ImageView imgPollChoice;
    @BindView(R.id.imgChampion)
    ImageView imgChampion;
    @BindView(R.id.txtOptionTitle)
    TextView txtOptionTitle;
    @BindView(R.id.txtPollCount)
    TextView txtPollCount;
    @BindView(R.id.progressPollCount)
    RoundCornerProgressBar progressPollCount;
    private boolean isChoice = false;
    private boolean isMultiChoice = false;
    private boolean isExpand = false;
    private Option option;

    public VHResultOptionItem(View itemView, boolean isMultiChoice, int totalPollCount) {
        super(itemView);
        this.isMultiChoice = isMultiChoice;
        ButterKnife.bind(this, itemView);
        progressPollCount.setMax(totalPollCount);
    }

    public void setLayout(boolean isChoice, boolean isExpand, boolean isTop, Option option) {
        this.isChoice = isChoice;
        this.isExpand = isExpand;
        this.option = option;
        txtOptionTitle.setText(option.getTitle());
        txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        txtPollCount.setText(Integer.toString(option.getCount()));
        setUpImgChampion(isTop);
        setUpOptionExpandLayout();
        ObjectAnimator animator = ObjectAnimator.ofFloat(progressPollCount, "progress", 0, option.getCount());
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1000);
        animator.start();
        itemView.setOnClickListener(this);
    }

    private void setUpImgChampion(boolean isChampion) {
        if (isChampion) {
            imgChampion.setVisibility(View.VISIBLE);
        } else {
            imgChampion.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpImgChoiceLaout() {
        if (!isMultiChoice) {
            imgPollChoice.setImageResource(isChoice ? R.drawable.ic_radio_button_checked_40dp
                    : R.drawable.ic_radio_button_unchecked_40dp);
        } else {
            // multi choice.
            imgPollChoice.setImageResource(isChoice ? R.drawable.ic_check_box_40dp
                    : R.drawable.ic_check_box_outline_blank_40dp);
        }
    }

    private void setUpOptionExpandLayout() {
        if (isExpand) {
            txtOptionTitle.setMaxLines(20);
        } else {
            txtOptionTitle.setMaxLines(1);
        }
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new EventBusController
                .OptionChoiceEvent(option.getId(), EventBusController.OptionChoiceEvent.OPTION_EXPAND));
        isExpand = !isExpand;
        setUpOptionExpandLayout();
    }
}
