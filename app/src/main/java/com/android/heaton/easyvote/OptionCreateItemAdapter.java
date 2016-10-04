package com.android.heaton.easyvote;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


/**
 * Created by heaton on 2016/9/2.
 */

public class OptionCreateItemAdapter extends RecyclerView.Adapter<VHCreateOptionItem> {

    private List<Option> optionList;
    public static int VIEW_TYPE_NORMAL_OPTION = 1;
    public static int VIEW_TYPE_ADD_OPTION = 2;
    private Context context;

    public OptionCreateItemAdapter(Context context) {
        optionList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Option initialOption = new Option();
            initialOption.setContent("Please input vote option content.");
            optionList.add(new Option());
        }
        Log.d("test", "new constructor");
        this.context = context;
    }

    public List<Option> getOptionList() {
        return optionList;
    }
    @Override
    public VHCreateOptionItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_create_vote_option, parent, false);
        VHCreateOptionItem vh = new VHCreateOptionItem(v, this);
        return vh;
    }

    @Override
    public void onBindViewHolder(VHCreateOptionItem holder, int position) {
        if (position == optionList.size()) {
            holder.setLayout(VIEW_TYPE_ADD_OPTION, 0, null);
        } else {
            holder.setLayout(VIEW_TYPE_NORMAL_OPTION, position, optionList.get(position));
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

    public void addNewOption() {
        optionList.add(new Option());
        Log.d("test", "add new ");
        this.notifyDataSetChanged();
    }

    public void removeOption(int position) {
        optionList.remove(position);
        Log.d("test", "remove position: " + position);
        this.notifyDataSetChanged();
        //this.notifyItemRemoved(position);
    }

}
