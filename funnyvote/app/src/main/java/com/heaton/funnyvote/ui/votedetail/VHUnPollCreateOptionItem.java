package com.heaton.funnyvote.ui.votedetail;

import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.databinding.CardViewItemUnpollCreateNewOptionBinding;

/**
 * Created by heaton on 2016/9/2.
 */

public class VHUnPollCreateOptionItem extends RecyclerView.ViewHolder {

    private final CardViewItemUnpollCreateNewOptionBinding binding;
    private Option option;
    private optionEditTextListener optionEditTextListener;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public VHUnPollCreateOptionItem(View itemView, VoteDetailContentActivity.OptionItemListener itemListener) {
        this(CardViewItemUnpollCreateNewOptionBinding.bind(itemView), itemListener);
    }

    public VHUnPollCreateOptionItem(CardViewItemUnpollCreateNewOptionBinding binding, VoteDetailContentActivity.OptionItemListener itemListener) {
        super(binding.getRoot());
        this.binding = binding;
        this.itemListener = itemListener;

        binding.relAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewOption();
            }
        });

        binding.imgDeleteOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeOption();
            }
        });

        binding.imgNewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionAddNewCheck();
            }
        });
    }

    public void setLayout(Option option) {
        this.option = option;
        binding.txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        if (getItemViewType() == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_ADD_NEW) {
            binding.relNormal.setVisibility(View.INVISIBLE);
            binding.relAdd.setVisibility(View.VISIBLE);
            binding.imgDeleteOption.setVisibility(View.GONE);
            binding.imgNewOption.setVisibility(View.GONE);
            binding.edtOptionTitle.setVisibility(View.GONE);
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener);
        } else if (getItemViewType() == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
            binding.relNormal.setVisibility(View.VISIBLE);
            binding.relAdd.setVisibility(View.INVISIBLE);
            binding.imgNewOption.setVisibility(View.VISIBLE);
            binding.imgDeleteOption.setVisibility(View.VISIBLE);
            binding.edtOptionTitle.setVisibility(View.VISIBLE);
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener);
            binding.edtOptionTitle.setText(option.getTitle());
            if (optionEditTextListener == null) {
                optionEditTextListener = new optionEditTextListener(itemListener);
            }
            binding.edtOptionTitle.addTextChangedListener(optionEditTextListener);
        }
    }

    public void addNewOption() {
        itemListener.onOptionAddNew();
    }

    public void removeOption() {
        itemListener.onOptionRemove(option.getId());
    }

    public void onOptionAddNewCheck() {
        itemListener.onOptionAddNewCheck(binding.edtOptionTitle.getText().toString());
    }

    private final class optionEditTextListener implements TextWatcher {
        VoteDetailContentActivity.OptionItemListener itemListener;
        public optionEditTextListener(VoteDetailContentActivity.OptionItemListener itemListener) {
            this.itemListener = itemListener;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            itemListener.onOptionTextChange(option.getId(),s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
