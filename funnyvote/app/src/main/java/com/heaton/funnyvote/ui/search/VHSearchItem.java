package com.heaton.funnyvote.ui.search;

import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.databinding.ItemListSearchBinding;

/**
 * Created by heaton on 2017/1/22.
 */
public class VHSearchItem extends RecyclerView.ViewHolder implements View.OnClickListener {
    final ItemListSearchBinding binding;
    private VoteData data;
    private SearchFragment.VoteSearchItemListener itemListener;

    public VHSearchItem(View itemView, SearchFragment.VoteSearchItemListener itemListener) {
        super(itemView);
        this.binding = ItemListSearchBinding.bind(itemView);
        this.itemListener = itemListener;
        itemView.setOnClickListener(this);
    }

    public VHSearchItem(ItemListSearchBinding binding, SearchFragment.VoteSearchItemListener itemListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.itemListener = itemListener;
        binding.getRoot().setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (itemListener != null && data != null) {
            itemListener.onVoteItemClick(data);
        }
    }

    public void setLayout(VoteData data) {
        this.data = data;
        binding.txtTitle.setText(data.getTitle());
        binding.txtAuthorName.setText(data.getAuthorName());
        binding.txtBarPollCount.setText(String.format(itemView.getContext()
                .getString(R.string.wall_item_bar_vote_count), data.getPollCount()));
        if (data.getVoteImage() == null || data.getVoteImage().isEmpty()) {
            binding.imgMain.setImageResource(data.getLocalImage());
        } else {
            Glide.with(itemView.getContext())
                    .load(data.getVoteImage())
                    .override((int) itemView.getResources().getDimension(R.dimen.search_image_width)
                            , (int) itemView.getResources().getDimension(R.dimen.search_image_high))
                    .centerCrop()
                    .into(binding.imgMain);
        }
        if (data.getEndTime() < System.currentTimeMillis()) {
            binding.txtHint.setText(R.string.search_item_time_end);
            binding.txtHint.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.md_red_500));
        } else {
            binding.txtHint.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.md_blue_500));
            binding.txtHint.setText(R.string.search_item_time_voting);
        }
    }
}
