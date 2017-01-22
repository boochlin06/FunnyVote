package com.android.heaton.funnyvote.ui.search;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.ui.createvote.CreateVoteActivity;

import java.util.List;


/**
 * Created by heaton on 2017/1/22.
 */

public class SearchItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_VOTE = 41;
    public static final int ITEM_TYPE_RELOAD = 42;
    public static final int ITEM_TYPE_NO_VOTE = 43;

    public static final String TAG_NO_VOTE_CREATE_NEW = "CREATE_NEW";
    public static final String TAG_NO_VOTE_REFRESH = "REFRESH";
    public static final String TAG_NO_VOTE_NOPE = "CREATE_NOPE";


    private OnReloadClickListener mOnReloadClickListener;
    private boolean showReload;
    private String tagNoVote = TAG_NO_VOTE_NOPE;

    private List<VoteData> voteList;
    private Context context;
    private long maxCount;

    public SearchItemAdapter(Context context, List<VoteData> datas) {
        this.context = context;
        this.voteList = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_VOTE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_search, parent, false);
            return new VHSearchItem(v);
        } else if (viewType == ITEM_TYPE_RELOAD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_reload, parent, false);
            return new ReloadViewHolder(v);
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

    public void setOnReloadClickListener(OnReloadClickListener listener) {
        mOnReloadClickListener = listener;
    }

    private View.OnClickListener mReloadItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.reload_rotate);
            view.findViewById(R.id.img_load_more).startAnimation(animation);
            if (mOnReloadClickListener != null) {
                mOnReloadClickListener.onReloadClicked();
            }
        }
    };
    private View.OnClickListener noVoteItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (tagNoVote.equals(TAG_NO_VOTE_CREATE_NEW)) {
                context.startActivity(new Intent(context, CreateVoteActivity.class));
            } else if (tagNoVote.equals(TAG_NO_VOTE_REFRESH)) {
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.reload_rotate);
                view.findViewById(R.id.imgRefrshVote).startAnimation(animation);
                if (mOnReloadClickListener != null) {
                    mOnReloadClickListener.onReloadClicked();
                }
            } else if (tagNoVote.equals(TAG_NO_VOTE_NOPE)) {

            }
        }
    };

    private class ReloadViewHolder extends RecyclerView.ViewHolder {
        public ImageView reloadImage;

        public ReloadViewHolder(View itemView) {
            super(itemView);
            reloadImage = (ImageView) itemView.findViewById(R.id.img_load_more);
            itemView.setOnClickListener(mReloadItemClickListener);
        }
    }

    public void setNoVoteTag(String tag) {
        this.tagNoVote = tag;
    }

    private class VHNoVote extends RecyclerView.ViewHolder {

        public ImageView imgAddVote;
        public ImageView imgRefreshVote;
        public TextView txtNoVote;

        public VHNoVote(View itemView) {
            super(itemView);
            imgAddVote = (ImageView) itemView.findViewById(R.id.imgAddVote);
            imgRefreshVote = (ImageView) itemView.findViewById(R.id.imgRefrshVote);
            txtNoVote = (TextView) itemView.findViewById(R.id.txtNoVote);
            if (tagNoVote.equals(TAG_NO_VOTE_CREATE_NEW)) {
                imgAddVote.setVisibility(View.VISIBLE);
                imgRefreshVote.setVisibility(View.GONE);
                txtNoVote.setText(R.string.Wall_item_no_vote_create_new);
            } else if (tagNoVote.equals(TAG_NO_VOTE_NOPE)) {
                imgAddVote.setVisibility(View.GONE);
                txtNoVote.setText(R.string.Wall_item_no_vote);
                imgRefreshVote.setVisibility(View.GONE);
            } else if (tagNoVote.equals(TAG_NO_VOTE_REFRESH)) {
                imgAddVote.setVisibility(View.GONE);
                imgRefreshVote.setVisibility(View.VISIBLE);
                txtNoVote.setText(R.string.Wall_item_no_vote_search);
            }
            itemView.setOnClickListener(noVoteItemClickListener);
        }
    }

    public interface OnReloadClickListener {
        void onReloadClicked();
    }

}
