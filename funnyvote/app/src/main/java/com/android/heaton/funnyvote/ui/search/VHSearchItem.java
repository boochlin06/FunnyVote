package com.android.heaton.funnyvote.ui.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.database.VoteData;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2017/1/22.
 */

public class VHSearchItem extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgPollCount)
    ImageView imgPollCount;
    @BindView(R.id.txtBarPollCount)
    TextView txtBarPollCount;
    @BindView(R.id.relBarPollCount)
    RelativeLayout relBarPollCount;
    @BindView(R.id.txtHint)
    TextView txtHint;
    private VoteData data;
    public VHSearchItem(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onClick(View v) {

    }

    public void setLayout(VoteData data) {
        this.data = data;
        txtTitle.setText(data.getTitle());
        txtBarPollCount.setText(String.format(itemView.getContext()
                .getString(R.string.Wall_item_bar_vote_count), data.getPollCount()));
        if (data.getVoteImage() == null || data.getVoteImage().isEmpty()) {
            imgMain.setImageResource(data.getLocalImage());
        } else {
            Glide.with(itemView.getContext())
                    .load(data.getVoteImage())
                    .override((int) (Util.convertDpToPixel(120, itemView.getContext()))
                            , (int) (Util.convertDpToPixel(80, itemView.getContext())))
                    .centerCrop()
                    .crossFade()
                    .into(imgMain);
        }
        if (data.getEndTime() < System.currentTimeMillis()) {
            txtHint.setText(R.string.search_item_time_end);
            txtHint.setTextColor(itemView.getContext().getColor(R.color.md_red_500));
        } else {
            txtHint.setTextColor(itemView.getContext().getColor(R.color.md_blue_500));
            txtHint.setText(R.string.search_item_time_voting);
        }
    }

}
