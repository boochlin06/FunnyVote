package com.android.heaton.easyvote;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.onClick;

/**
 * Created by heaton on 2016/9/2.
 */

public class VHCreateOptionItem extends RecyclerView.ViewHolder {

    @BindView(R.id.imgAdd)
    ImageView imgAdd;
    @BindView(R.id.relAdd)
    RelativeLayout relAdd;
    @BindView(R.id.txtOptionNumber)
    TextView txtOptionNumber;
    @BindView(R.id.imgDelete)
    ImageView imgDelete;
    @BindView(R.id.txtOptionContent)
    EditText txtOptionContent;
    @BindView(R.id.relNormal)
    RelativeLayout relNormal;
    OptionCreateItemAdapter adapter;
    int position = 0;
    public VHCreateOptionItem(View itemView,OptionCreateItemAdapter adapter) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.adapter = adapter;
    }
    public void setLayout(int viewType ,int position,Option data){
        this.position = position;
        if(viewType ==OptionCreateItemAdapter.VIEW_TYPE_ADD_OPTION) {
            relNormal.setVisibility(View.INVISIBLE);
            relAdd.setVisibility(View.VISIBLE);
        } else if (viewType == OptionCreateItemAdapter.VIEW_TYPE_NORMAL_OPTION) {
            relNormal.setVisibility(View.VISIBLE);
            relAdd.setVisibility(View.INVISIBLE);
            txtOptionNumber.setText(String.valueOf(position+1));
        }
    }
    @OnClick(R.id.relAdd)
    public void AddNewOption(){
        adapter.addNewOption();
    }

    @OnClick(R.id.imgDelete)
    public void removeOption(){
        adapter.removeOption(position);
    }
}
