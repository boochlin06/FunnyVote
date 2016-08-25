package com.android.heaton.easyvote;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class VHVoteItem extends RecyclerView.ViewHolder {
    private TextView mTxtTitle;
    private TextView mTxtDescription;
    private TextView mTxtTimeInterval;
    private TextView mTxtHumanCount;
    public VHVoteItem(View v) {
        super(v);
        mTxtTitle = (TextView) v.findViewById(R.id.txtTitle);
    }
    public void setLayout(VoteData data) {
        mTxtTitle.setText(data.title);
    }
}