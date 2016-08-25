package com.android.heaton.easyvote;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by heaton on 2016/8/22.
 */

public class OptionItemAdapter extends Adapter<VHOptionItem> {

    private Context context;
    private List<Option> optionList;
    public OptionItemAdapter(Context context, List<Option> datas) {
        this.context = context;
        optionList = datas;
    }
    @Override
    public VHOptionItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_options, parent , false);
        VHOptionItem vh = new VHOptionItem(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(VHOptionItem holder, int position) {
        holder.setLayout(optionList.get(position));
    }


    @Override
    public int getItemCount() {
        return optionList.size();
    }
}
