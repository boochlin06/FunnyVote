package com.heaton.funnyvote.ui.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.VoteData;

import java.util.List;


/**
 * Created by heaton on 2017/1/22.
 */

public class SearchItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_VOTE = 41;
    public static final int ITEM_TYPE_RELOAD = 42;
    public static final int ITEM_TYPE_NO_VOTE = 43;

    private boolean showReload;

    private List<VoteData> voteList;
    private Context context;
    private long maxCount;
    private SearchFragment.VoteSearchItemListener itemListener;

    public SearchItemAdapter(Context context
            , List<VoteData> datas, SearchFragment.VoteSearchItemListener itemListener) {
        this.context = context;
        this.itemListener = itemListener;
        this.voteList = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_VOTE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_search, parent, false);
            return new VHSearchItem(v, itemListener);
        } else if (viewType == ITEM_TYPE_RELOAD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_reload, parent, false);
            return new ReloadViewHolder(v, itemListener);
        } else if (viewType == ITEM_TYPE_NO_VOTE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_no_vote, parent, false);
            return new VHNoVote(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHSearchItem) {
            ((VHSearchItem) holder).setLayout(voteList.get(position));
        }
    }

    public void setVoteList(List<VoteData> voteList) {
        this.voteList = voteList;
    }

    @Override
    public int getItemCount() {
        if (voteList.size() > 0 && voteList.size() < maxCount) {
            showReload = true;
        } else {
            showReload = false;
        }
        if (showReload) {
            return voteList.size() + 1;
        } else {
            if (voteList.size() == 0) {
                return 1;
            } else {
                return voteList.size();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showReload && position == getItemCount() - 1) {
            return ITEM_TYPE_RELOAD;
        } else {
            if (voteList.size() == 0) {
                return ITEM_TYPE_NO_VOTE;
            } else {
                return ITEM_TYPE_VOTE;
            }
        }
    }

    public void setMaxCount(long count) {
        maxCount = count;
    }


    private class ReloadViewHolder extends RecyclerView.ViewHolder {
        public ImageView reloadImage;


        public ReloadViewHolder(View itemView, final SearchFragment.VoteSearchItemListener itemListener) {
            super(itemView);
            reloadImage = (ImageView) itemView.findViewById(R.id.img_load_more);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.reload_rotate);
                    v.findViewById(R.id.img_load_more).startAnimation(animation);
                    itemListener.onReloadVote();
                }
            });
        }
    }


    private class VHNoVote extends RecyclerView.ViewHolder {

        public ImageView imgAddVote;
        public ImageView imgRefreshVote;
        public ImageView imgLogo;
        public TextView txtNoVote;

        public VHNoVote(View itemView) {
            super(itemView);
            imgAddVote = (ImageView) itemView.findViewById(R.id.imgAddVote);
            imgRefreshVote = (ImageView) itemView.findViewById(R.id.imgRefreshVote);
            imgLogo = (ImageView) itemView.findViewById(R.id.imgLogo);
            txtNoVote = (TextView) itemView.findViewById(R.id.txtNoVote);
            imgAddVote.setVisibility(View.GONE);
            imgRefreshVote.setVisibility(View.GONE);
            imgLogo.setVisibility(View.VISIBLE);
            txtNoVote.setText(R.string.wall_item_no_vote_search);
        }
    }
}
