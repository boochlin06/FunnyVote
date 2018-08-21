package com.heaton.funnyvote.ui.votedetail;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 2016/9/2.
 */

public class VHUnPollCreateOptionItem extends RecyclerView.ViewHolder {

    @BindView(R.id.imgAdd)
    ImageView imgAdd;
    @BindView(R.id.relAdd)
    RelativeLayout relAdd;
    @BindView(R.id.txtOptionNumber)
    TextView txtOptionNumber;

    @BindView(R.id.imgNewOption)
    ImageView imgNewOption;
    @BindView(R.id.edtOptionTitle)
    EditText edtOptionTitle;
    @BindView(R.id.imgDeleteOption)
    ImageView imgDelete;
    @BindView(R.id.relNormal)
    RelativeLayout relNormal;
    private Option option;
    private optionEditTextListener optionEditTextListener;
    private VoteDetailContentActivity.OptionItemListener itemListener;

    public VHUnPollCreateOptionItem(View itemView, VoteDetailContentActivity.OptionItemListener itemListener) {
        super(itemView);
        this.itemListener = itemListener;
        ButterKnife.bind(this, itemView);
    }

    public void setLayout(Option option) {
        this.option = option;
        txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
        if (getItemViewType() == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_ADD_NEW) {
            relNormal.setVisibility(View.INVISIBLE);
            relAdd.setVisibility(View.VISIBLE);
            imgDelete.setVisibility(View.GONE);
            imgNewOption.setVisibility(View.GONE);
            edtOptionTitle.setVisibility(View.GONE);
            edtOptionTitle.removeTextChangedListener(optionEditTextListener);
        } else if (getItemViewType() == OptionItemAdapter.OPTION_UNPOLL_VIEW_TYPE_INPUT_CONTENT) {
            relNormal.setVisibility(View.VISIBLE);
            relAdd.setVisibility(View.INVISIBLE);
            imgNewOption.setVisibility(View.VISIBLE);
            imgDelete.setVisibility(View.VISIBLE);
            edtOptionTitle.setVisibility(View.VISIBLE);
            edtOptionTitle.removeTextChangedListener(optionEditTextListener);
            edtOptionTitle.setText(option.getTitle());
            if (optionEditTextListener == null) {
                optionEditTextListener = new optionEditTextListener(itemListener);
            }
            edtOptionTitle.addTextChangedListener(optionEditTextListener);
        }
    }


    @OnClick(R.id.relAdd)
    public void addNewOption() {
        itemListener.onOptionAddNew();
    }

    @OnClick(R.id.imgDeleteOption)
    public void removeOption() {
        itemListener.onOptionRemove(option.getId());
    }

    @OnClick(R.id.imgNewOption)
    public void onOptionAddNewCheck() {
        itemListener.onOptionAddNewCheck(edtOptionTitle.getText().toString());
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
            itemListener.onOptionTextChange(option.getId(), s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
