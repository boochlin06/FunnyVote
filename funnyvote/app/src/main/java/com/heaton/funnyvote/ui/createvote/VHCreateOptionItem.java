package com.heaton.funnyvote.ui.createvote;

import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.databinding.CardViewCreateVoteOptionBinding;

/**
 * Created by heaton on 2016/9/2.
 */

public class VHCreateOptionItem extends RecyclerView.ViewHolder {

    private final CardViewCreateVoteOptionBinding binding;
    private optionEditTextListener optionEditTextListener;
    private Option option;
    private CreateVoteTabOptionFragment.OptionItemListener itemListener;

    public VHCreateOptionItem(View itemView, CreateVoteTabOptionFragment.OptionItemListener itemListener) {
        this(CardViewCreateVoteOptionBinding.bind(itemView), itemListener);
    }

    public VHCreateOptionItem(CardViewCreateVoteOptionBinding binding, CreateVoteTabOptionFragment.OptionItemListener itemListener) {
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
    }

    public void setLayout(int viewType, Option option) {
        this.option = option;
        if (viewType == OptionCreateItemAdapter.VIEW_TYPE_ADD_OPTION) {
            binding.relNormal.setVisibility(View.INVISIBLE);
            binding.relAdd.setVisibility(View.VISIBLE);
            binding.imgDeleteOption.setVisibility(View.GONE);
            binding.edtOptionTitle.setVisibility(View.GONE);
            binding.edtOptionTitle.removeTextChangedListener(optionEditTextListener);
        } else if (viewType == OptionCreateItemAdapter.VIEW_TYPE_NORMAL_OPTION) {
            binding.relNormal.setVisibility(View.VISIBLE);
            binding.relAdd.setVisibility(View.INVISIBLE);
            binding.imgDeleteOption.setVisibility(View.VISIBLE);
            binding.txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
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

    private final class optionEditTextListener implements TextWatcher {

        CreateVoteTabOptionFragment.OptionItemListener itemListener;
        public optionEditTextListener(CreateVoteTabOptionFragment.OptionItemListener itemListener) {
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
