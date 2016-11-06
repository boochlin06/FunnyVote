package com.android.heaton.funnyvote.ui.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.VoteData;

import java.util.List;

/**
 * Created by heaton on 16/4/1.
 */
public class VoteWallItemAdapter extends RecyclerView.Adapter<VHVoteWallItem> {


    private Context context;
    private List<VoteData> voteList;

    public VoteWallItemAdapter(Context context, List<VoteData> datas) {
        this.context = context;
        voteList = datas;
    }

    @Override
    public VHVoteWallItem onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_wall_item, parent, false);
        VHVoteWallItem vh = new VHVoteWallItem(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(VHVoteWallItem holder, int position) {
        holder.setLayout(voteList.get(position));
    }

    @Override
    public int getItemCount() {
        return voteList.size();
    }


}
