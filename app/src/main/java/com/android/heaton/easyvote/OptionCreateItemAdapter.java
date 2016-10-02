package com.android.heaton.easyvote;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;


/**
 * Created by heaton on 2016/9/2.
 */

public class OptionCreateItemAdapter extends RecyclerView.Adapter<VHCreateOptionItem> {
    private List<Option> optionList;
    public static int VIEW_TYPE_NORMAL_OPTION = 1;
    public static int VIEW_TYPE_ADD_OPTION = 2;
    private Context context;

    public OptionCreateItemAdapter(Context context, List<Option> datas) {
        optionList = datas;
        this.context = context;
    }

    @Override
    public VHCreateOptionItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (VIEW_TYPE_NORMAL_OPTION == viewType) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_create_vote_option, parent, false);
        } else if (VIEW_TYPE_ADD_OPTION == viewType) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_create_vote_option_add, parent, false);
        }
        VHCreateOptionItem vh = new VHCreateOptionItem(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(VHCreateOptionItem holder, int position) {
        if (position == optionList.size()) {
            //older.setLayout(optionList.get(position));
        } else {
            holder.setLayout(optionList.get(position));
        }
    }


    @Override
    public int getItemCount() {
        return optionList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == optionList.size() ? VIEW_TYPE_ADD_OPTION : VIEW_TYPE_NORMAL_OPTION;
    }

}
