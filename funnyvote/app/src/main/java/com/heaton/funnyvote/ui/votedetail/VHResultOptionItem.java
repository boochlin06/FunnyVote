package com.heaton.funnyvote.ui.votedetail;

import android.animation.ObjectAnimator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;

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
    @BindView(R.id.txtPollCountPercent)
    TextView txtPollCountPercent;
    @BindView(R.id.progressPollCount)
    RoundCornerProgressBar progressPollCount;
    @BindView(R.id.cardOption)
    CardView cardOption;
    private boolean isChoice = false;
    private boolean isExpand = false;
    private int totalPollCount;
    private Option option;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public VHResultOptionItem(View itemView, int totalPollCount
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        super(itemView);
        this.totalPollCount = totalPollCount;
        this.itemListener = itemListener;
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
        double percent = totalPollCount == 0 ? 0 : (double) option.getCount() / totalPollCount * 100;
        txtPollCountPercent.setText(String.format("%3.1f%%", percent));
        setUpImgChampion(isTop);
        setUpOptionExpandLayout();
        setUpOptionChoiceLayout();
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

    private void setUpOptionChoiceLayout() {
        if (option.getIsUserChoiced() || isChoice) {
            cardOption.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.md_red_100));
            progressPollCount.setProgressColor(itemView.getContext()
                    .getResources().getColor(R.color.md_red_600));
            progressPollCount.setProgressBackgroundColor(itemView.getContext()
                    .getResources().getColor(R.color.md_red_200));
        } else {
            cardOption.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.md_blue_100));
            progressPollCount.setProgressColor(itemView.getContext()
                    .getResources().getColor(R.color.md_blue_600));
            progressPollCount.setProgressBackgroundColor(itemView.getContext()
                    .getResources().getColor(R.color.md_blue_200));

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
        itemListener.onOptionExpand(option.getCode());
//        EventBus.getDefault().post(new EventBusManager
//                .OptionChoiceEvent(option.getId(), EventBusManager.OptionChoiceEvent.OPTION_EXPAND, option.getCode()));
        //isExpand = !isExpand;
        //setUpOptionExpandLayout();
    }
}
