package com.android.heaton.easyvote;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2016/9/2.
 */

public class VHCreateOptionItem extends RecyclerView.ViewHolder {

    @BindView(R.id.txtOptionNumber)
    TextView txtOptionNumber;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    @BindView(R.id.txtOptionContent)
    TextView txtOptionContent;
    @BindView(R.id.imgAdd)
    ImageView imgAdd;
    public VHCreateOptionItem(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
    public void setLayout(Option data){

    }
}
