package com.android.heaton.easyvote;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by heaton on 16/4/1.
 */
public class VoteItemAdapter extends RecyclerView.Adapter<VHVoteItem>  {

    private Context context;
    private List<VoteData> voteList;
    public VoteItemAdapter(Context context, List<VoteData> datas) {
        this.context = context;
        voteList = datas;
    }
    @Override
    public VHVoteItem onCreateViewHolder(final ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_wall_item, parent , false);
        VHVoteItem vh = new VHVoteItem(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.getContext().startActivity(new Intent(parent.getContext(),VoteDetailContent.class));
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(VHVoteItem holder, int position) {
        holder.setLayout(voteList.get(position));
    }

    @Override
    public int getItemCount() {
        return voteList.size();
    }

}
