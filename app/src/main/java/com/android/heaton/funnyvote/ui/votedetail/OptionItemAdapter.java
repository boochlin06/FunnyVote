package com.android.heaton.funnyvote.ui.votedetail;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by heaton on 2016/8/22.
 */

public class OptionItemAdapter extends Adapter<RecyclerView.ViewHolder> {


    public static final int OPTION_UNPOLL_VIEW_TYPE_ADD_NEW = 0;
    public static final int OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT = 1;
    public static final int OPTION_UNPOLL = 2;
    public static final int OPTION_SHOW_RESULT = 3;
    private List<Option> optionList;
    private int optionChoiceType = OPTION_UNPOLL;
    private List<Integer> choiceList;
    private List<Option> newOptionList;
    private List<Integer> expandOptionlist;
    private int pollCount = 0;
    private VoteData data;


    public OptionItemAdapter(int optionType, List<Option> optionList, VoteData data) {
        this.optionList = optionList;
        this.optionChoiceType = optionType;
        this.pollCount = data.getPollCount();
        this.choiceList = new ArrayList<>();
        this.newOptionList = new ArrayList<>();
        this.expandOptionlist = new ArrayList<>();
        this.data = data;
    }

    public void setOptionType(int optionType) {
        this.optionChoiceType = optionType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (optionChoiceType == OPTION_UNPOLL) {
            if (viewType == OPTION_UNPOLL_VIEW_TYPE_ADD_NEW || viewType == OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
                return new VHUnpollCreateOptionItem(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_unpoll_create_new_option, parent, false), data.isMultiChoice());
            } else {
                return new VHUnPollOptionItem(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_item_unpoll_options, parent, false), data.isMultiChoice());
            }
        } else if (optionChoiceType == OPTION_SHOW_RESULT) {
            return new VHResultOptionItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_item_result_option, parent, false), data.isMultiChoice(), pollCount);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VHUnPollOptionItem) {
            ((VHUnPollOptionItem) holder).setLayout(choiceList.contains(position)
                    , expandOptionlist.contains(position), optionList.get(position));
        } else if (holder instanceof VHResultOptionItem) {
            ((VHResultOptionItem) holder).setLayout(choiceList.contains(position)
                    , expandOptionlist.contains(position)
                    , (optionList.get(position).getCount() == data.getOptionTopCount()
                            && data.getPollCount() != 0)
                    , optionList.get(position));
        } else if (holder instanceof VHUnpollCreateOptionItem) {
            if (holder.getItemViewType() == OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
                ((VHUnpollCreateOptionItem) holder).setLayout(choiceList.contains(position)
                        , newOptionList.get(position - optionList.size()));
            } else {
                ((VHUnpollCreateOptionItem) holder).setLayout(choiceList.contains(position)
                        , null);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (OPTION_UNPOLL == optionChoiceType) {
            if (data.getIsUserCanAddOption()) {
                return optionList.size() + newOptionList.size() + 1;
            } else {
                return optionList.size();
            }
        } else {
            return optionList.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = -1;
        if (optionChoiceType == OPTION_UNPOLL) {
            if (position == optionList.size() + newOptionList.size()) {
                return OPTION_UNPOLL_VIEW_TYPE_ADD_NEW;
            } else if (position >= optionList.size() && position < optionList.size() + newOptionList.size()) {
                return OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT;
            }
        }
        return type;
    }

    public List<Integer> getChoiceList() {
        return choiceList;
    }

    public List<Option> getNewOptionList() {
        return newOptionList;
    }

    public List<Integer> getExpandOptionlist() {
        return expandOptionlist;
    }
}
