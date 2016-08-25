package com.android.heaton.easyvote;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by heaton on 2016/8/22.
 */

public class VHOptionItem extends RecyclerView.ViewHolder {
    private TextView txtContent;
    private TextView txtNumber;
    public VHOptionItem(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.txtOptionContent);
        txtNumber = (TextView) itemView.findViewById(R.id.txtOptionNumber);
    }
    public void setLayout(Option data) {
        txtContent.setText(data.getContent());
        txtNumber.setText("1");
    }
}
