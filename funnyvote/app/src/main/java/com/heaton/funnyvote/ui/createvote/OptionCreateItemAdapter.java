package com.heaton.funnyvote.ui.createvote;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;

import java.util.List;


/**
 * Created by heaton on 2016/9/2.
 */

public class OptionCreateItemAdapter extends RecyclerView.Adapter<VHCreateOptionItem> {

    public static int VIEW_TYPE_NORMAL_OPTION = 1;
    public static int VIEW_TYPE_ADD_OPTION = 2;
    private List<Option> optionList;
    private CreateVoteTabOptionFragment.OptionItemListener itemListener;

    public OptionCreateItemAdapter(List<Option> optionList
            , CreateVoteTabOptionFragment.OptionItemListener itemListener) {
        this.optionList = optionList;
        this.itemListener = itemListener;
    }

    @Override
    public VHCreateOptionItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_create_vote_option
                , parent, false);
        VHCreateOptionItem vh = new VHCreateOptionItem(v, itemListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(VHCreateOptionItem holder, int position) {
        if (position == optionList.size()) {
            holder.setLayout(VIEW_TYPE_ADD_OPTION, null);
        } else {
            holder.setLayout(VIEW_TYPE_NORMAL_OPTION, optionList.get(position));
        }
    }


    @Override
    public int getItemCount() {
        return optionList == null ? 0 : optionList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == optionList.size() ? VIEW_TYPE_ADD_OPTION : VIEW_TYPE_NORMAL_OPTION;
    }
}
