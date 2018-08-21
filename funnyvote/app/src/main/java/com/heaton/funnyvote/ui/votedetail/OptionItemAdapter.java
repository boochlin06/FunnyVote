package com.heaton.funnyvote.ui.votedetail;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/8/22.
 */

public class OptionItemAdapter extends Adapter<RecyclerView.ViewHolder> {

    public static final int OPTION_UNPOLL_VIEW_TYPE_ADD_NEW = 20;
    public static final int OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT = 21;
    public static final int OPTION_UNPOLL = 2;
    public static final int OPTION_SHOW_RESULT = 3;
    private List<Option> optionList;
    private List<Option> searchList;

    private List<Long> choiceList;
    private List<String> choiceCodeList;
    private List<String> expandOptionList;
    private int pollCount = 0;
    private int optionChoiceType = OPTION_UNPOLL;
    private VoteData data;
    private boolean isSearchMode = false;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public OptionItemAdapter(int optionType, List<Option> optionList, VoteData data
            , VoteDetailContentActivity.OptionItemListener itemListener) {
        this.optionList = optionList;
        this.optionChoiceType = optionType;
        this.pollCount = data.getPollCount();
        this.choiceList = new ArrayList<>();
        this.choiceCodeList = new ArrayList<>();
        this.expandOptionList = new ArrayList<>();
        this.searchList = new ArrayList<>();
        this.data = data;
        this.itemListener = itemListener;
    }

    public void setOptionList(List<Option> optionList) {
        this.optionList = optionList;
    }

    public void setExpandOptionList(List<String> expandOptionList) {
        this.expandOptionList = expandOptionList;
    }

    public void setSearchList(List<Option> searchList) {
        this.searchList = searchList;
    }

    public boolean isSearchMode() {
        return isSearchMode;
    }

    public void setSearchMode(boolean searchMode) {
        this.isSearchMode = searchMode;
    }

    public List<Option> getCurrentList() {
        return isSearchMode ? searchList : optionList;
    }

    public void setOptionType(int optionType) {
        this.optionChoiceType = optionType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (optionChoiceType == OPTION_UNPOLL) {
            if (viewType == OPTION_UNPOLL_VIEW_TYPE_ADD_NEW
                    || viewType == OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
                return new VHUnPollCreateOptionItem(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_item_unpoll_create_new_option, parent, false)
                        , itemListener);
            } else {
                return new VHUnPollOptionItem(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.card_view_item_unpoll_options, parent, false)
                        , data.isMultiChoice(), itemListener);
            }
        } else if (optionChoiceType == OPTION_SHOW_RESULT) {
            return new VHResultOptionItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_view_item_result_option, parent, false)
                    , pollCount, itemListener);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        boolean isChoice = false;
        boolean isExpand = false;
        if (holder instanceof VHUnPollOptionItem) {
            isChoice = choiceList.contains(getCurrentList().get(position).getId());
            isExpand = expandOptionList.contains(getCurrentList().get(position).getCode());
            ((VHUnPollOptionItem) holder).setLayout(isChoice
                    , isExpand
                    , getCurrentList().get(position));
        } else if (holder instanceof VHResultOptionItem) {
            isChoice = choiceList.contains(getCurrentList().get(position).getId());
            isExpand = expandOptionList.contains(getCurrentList().get(position).getCode());
            ((VHResultOptionItem) holder).setLayout(isChoice
                    , isExpand
                    , (getCurrentList().get(position).getCount() == data.getOptionTopCount()
                            && data.getPollCount() != 0)
                    , getCurrentList().get(position));
        } else if (holder instanceof VHUnPollCreateOptionItem) {
            if (holder.getItemViewType() == OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
                isChoice = choiceList.contains(getCurrentList().get(position).getId());
                ((VHUnPollCreateOptionItem) holder).setLayout(getCurrentList().get(position));
            } else {
                ((VHUnPollCreateOptionItem) holder).setLayout(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (OPTION_UNPOLL == optionChoiceType && data.getIsUserCanAddOption() && !isSearchMode()) {
            return getCurrentList().size() + 1;
        } else {
            return getCurrentList().size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = -1;

        if (optionChoiceType == OPTION_UNPOLL) {
            if (position == getCurrentList().size()) {
                return OPTION_UNPOLL_VIEW_TYPE_ADD_NEW;
            } else if (getCurrentList().get(position).getId() < 0) {
                return OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT;
            }
        }
        return type;
    }

    public List<Long> getChoiceList() {
        return choiceList;
    }

    public void setChoiceList(List<Long> choiceList) {
        this.choiceList = choiceList;
    }

    public List<String> getChoiceCodeList() {
        return choiceCodeList;
    }

}
