package com.android.heaton.funnyvote.ui.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.ui.createvote.CreateVoteActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 16/4/1.
 */
public class VoteWallItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int ITEM_TYPE_VOTE = 41;
    public static final int ITEM_TYPE_RELOAD = 42;
    public static final int ITEM_TYPE_NO_VOTE = 43;
    public static final int ITEM_TYPE_ADMOB = 44;

    public static final String TAG_NO_VOTE_CREATE_NEW = "CREATE_NEW";
    public static final String TAG_NO_VOTE_REFRESH = "REFRESH";
    public static final String TAG_NO_VOTE_NOPE = "CREATE_NOPE";

    public static int ADMOB_FREQUENCE = 10;
    public static boolean ENABLE_ADMOB = false;

    private Context context;
    private List<VoteData> voteList;
    private List<ListTypeItem> itemTypeList;
    private long maxCount;
    private boolean showReload;
    private String tagNoVote = TAG_NO_VOTE_NOPE;
    private View bannerAdmob;

    private class ListTypeItem {
        private int viewType;
        private VoteData voteData;

        public ListTypeItem(int viewType, VoteData voteData) {
            this.viewType = viewType;
            this.voteData = voteData;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public void setVoteData(VoteData voteData) {
            this.voteData = voteData;
        }

        public VoteData getVoteData() {
            return this.voteData;
        }

        public int getViewType() {
            return this.viewType;
        }
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
                view.findViewById(R.id.imgRefreshVote).startAnimation(animation);
                if (mOnReloadClickListener != null) {
                    mOnReloadClickListener.onReloadClicked();
                }
            } else if (tagNoVote.equals(TAG_NO_VOTE_NOPE)) {

            }
        }
    };
    private OnReloadClickListener mOnReloadClickListener;

    public VoteWallItemAdapter(Context context, List<VoteData> datas) {
        this.context = context;
        this.voteList = datas;
        ENABLE_ADMOB = context.getResources().getBoolean(R.bool.enable_list_admob);
        ADMOB_FREQUENCE = context.getResources().getInteger(R.integer.list_admob_frequence);
        itemTypeList = new ArrayList<>();
    }

    public void setOnReloadClickListener(OnReloadClickListener listener) {
        mOnReloadClickListener = listener;
    }

    public void setVoteList(List<VoteData> voteList) {
        this.voteList = voteList;
    }

    public void setMaxCount(long count) {
        maxCount = count;
    }

    public void resetItemTypeList() {
        itemTypeList.clear();
        for (int i = 0; i < voteList.size(); i++) {
            if (i % ADMOB_FREQUENCE == ADMOB_FREQUENCE - 1 && ENABLE_ADMOB) {
                itemTypeList.add(new ListTypeItem(ITEM_TYPE_ADMOB, null));
            }
            itemTypeList.add(new ListTypeItem(ITEM_TYPE_VOTE, this.voteList.get(i)));
        }
        if (this.voteList.size() > 0 && this.voteList.size() < maxCount) {
            showReload = true;
        } else {
            showReload = false;
        }
        if (showReload) {
            itemTypeList.add(new ListTypeItem(ITEM_TYPE_RELOAD, null));
        } else if (!showReload && voteList.size() == 0) {
            itemTypeList.add(new ListTypeItem(ITEM_TYPE_NO_VOTE, null));
        }
    }

    public void setNoVoteTag(String tag) {
        this.tagNoVote = tag;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        if (viewType == ITEM_TYPE_VOTE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_wall_item, parent, false);
            return new VHVoteWallItem(v);
        } else if (viewType == ITEM_TYPE_RELOAD) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_reload, parent, false);
            return new ReloadViewHolder(v);
        } else if (viewType == ITEM_TYPE_NO_VOTE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_item_no_vote, parent, false);
            return new VHNoVote(v);
        } else if (viewType == ITEM_TYPE_ADMOB) {
            if (bannerAdmob == null) {
                bannerAdmob = LayoutInflater.from(context).inflate(R.layout.item_list_admob, parent, false);
                AdView adView = (AdView) bannerAdmob.findViewById(R.id.adView);
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            }
            return new VHAdMob(bannerAdmob);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHVoteWallItem) {
            ((VHVoteWallItem) holder).setLayout(itemTypeList.get(position).getVoteData());
        }
    }

    @Override
    public int getItemCount() {
        return itemTypeList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return itemTypeList.get(position).getViewType();
    }

    private class ReloadViewHolder extends RecyclerView.ViewHolder {
        public ImageView reloadImage;

        public ReloadViewHolder(View itemView) {
            super(itemView);
            reloadImage = (ImageView) itemView.findViewById(R.id.img_load_more);
            itemView.setOnClickListener(mReloadItemClickListener);
        }
    }

    private class VHNoVote extends RecyclerView.ViewHolder {

        public ImageView imgAddVote;
        public ImageView imgRefreshVote;
        public TextView txtNoVote;

        public VHNoVote(View itemView) {
            super(itemView);
            imgAddVote = (ImageView) itemView.findViewById(R.id.imgAddVote);
            imgRefreshVote = (ImageView) itemView.findViewById(R.id.imgRefreshVote);
            txtNoVote = (TextView) itemView.findViewById(R.id.txtNoVote);
            if (tagNoVote.equals(TAG_NO_VOTE_CREATE_NEW)) {
                imgAddVote.setVisibility(View.VISIBLE);
                imgRefreshVote.setVisibility(View.GONE);
                txtNoVote.setText(R.string.wall_item_no_vote_create_new);
            } else if (tagNoVote.equals(TAG_NO_VOTE_NOPE)) {
                imgAddVote.setVisibility(View.GONE);
                txtNoVote.setText(R.string.wall_item_no_vote);
                imgRefreshVote.setVisibility(View.GONE);
            } else if (tagNoVote.equals(TAG_NO_VOTE_REFRESH)) {
                imgAddVote.setVisibility(View.GONE);
                imgRefreshVote.setVisibility(View.VISIBLE);
                txtNoVote.setText(R.string.wall_item_no_vote_refresh);
            }
            itemView.setOnClickListener(noVoteItemClickListener);
        }
    }

    private class VHAdMob extends RecyclerView.ViewHolder {

        public VHAdMob(View view) {
            super(view);
        }
    }

    public interface OnReloadClickListener {
        void onReloadClicked();
    }

}
