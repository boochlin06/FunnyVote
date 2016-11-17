package com.android.heaton.funnyvote.ui.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.VoteData;

import java.util.List;

/**
 * Created by heaton on 16/4/1.
 */
public class VoteWallItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_VOTE = 41;
    public static final int ITEM_TYPE_RELOAD = 42;

    private Context context;
    private List<VoteData> voteList;
    private long mMaxCount;
    private boolean showReload;

    private View.OnClickListener mReloadItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Animation animation= AnimationUtils.loadAnimation(context, R.anim.reload_rotate);
            view.findViewById(R.id.img_load_more).startAnimation(animation);
            if (mOnReloadClickListener != null) {
                mOnReloadClickListener.onReloadClicked();
            }
        }
    };

    private OnReloadClickListener mOnReloadClickListener;

    public VoteWallItemAdapter(Context context, List<VoteData> datas) {
        this.context = context;
        voteList = datas;
    }

    public void setOnReloadClickListener(OnReloadClickListener listener) {
        mOnReloadClickListener = listener;
    }

    public void setVoteList(List<VoteData> voteList) {
        this.voteList = voteList;
    }

    public void setMaxCount(long count) {
        mMaxCount = count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_VOTE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_wall_item, parent, false);
            return new VHVoteWallItem(v);
        } else if (viewType == ITEM_TYPE_RELOAD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_reload, parent, false);
            return new ReloadViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof  VHVoteWallItem) {
            ((VHVoteWallItem)holder).setLayout(voteList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        if (voteList.size() > 0 && voteList.size() < mMaxCount) {
            showReload = true;
        } else {
            showReload = false;
        }
        if (showReload) {
            return voteList.size() + 1;
        } else {
            return voteList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (showReload && position == getItemCount() - 1) {
            return ITEM_TYPE_RELOAD;
        } else {
            return ITEM_TYPE_VOTE;
        }
    }

    private class ReloadViewHolder extends RecyclerView.ViewHolder {
        public ImageView reloadImage;

        public ReloadViewHolder(View itemView) {
            super(itemView);
            reloadImage = (ImageView)itemView.findViewById(R.id.img_load_more);
            itemView.setOnClickListener(mReloadItemClickListener);
        }
    }

    public interface OnReloadClickListener {
        void onReloadClicked();
    }
}
