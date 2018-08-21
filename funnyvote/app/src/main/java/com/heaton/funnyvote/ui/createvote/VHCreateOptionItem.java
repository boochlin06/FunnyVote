package com.heaton.funnyvote.ui.createvote;

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

public class VHCreateOptionItem extends RecyclerView.ViewHolder {

    @BindView(R.id.imgAdd)
    ImageView imgAdd;
    @BindView(R.id.relAdd)
    RelativeLayout relAdd;
    @BindView(R.id.txtOptionNumber)
    TextView txtOptionNumber;
    @BindView(R.id.imgDeleteOption)
    ImageView imgDelete;
    @BindView(R.id.edtOptionTitle)
    EditText edtOptionTitle;
    @BindView(R.id.relNormal)
    RelativeLayout relNormal;
    private optionEditTextListener optionEditTextListener;
    private Option option;
    private CreateVoteTabOptionFragment.OptionItemListener itemListener;

    public VHCreateOptionItem(View itemView, CreateVoteTabOptionFragment.OptionItemListener itemListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.itemListener = itemListener;
    }

    public void setLayout(int viewType, Option option) {
        this.option = option;
        if (viewType == OptionCreateItemAdapter.VIEW_TYPE_ADD_OPTION) {
            relNormal.setVisibility(View.INVISIBLE);
            relAdd.setVisibility(View.VISIBLE);
            imgDelete.setVisibility(View.GONE);
            edtOptionTitle.setVisibility(View.GONE);
            edtOptionTitle.removeTextChangedListener(optionEditTextListener);
        } else if (viewType == OptionCreateItemAdapter.VIEW_TYPE_NORMAL_OPTION) {
            relNormal.setVisibility(View.VISIBLE);
            relAdd.setVisibility(View.INVISIBLE);
            imgDelete.setVisibility(View.VISIBLE);
            txtOptionNumber.setText(Integer.toString(getAdapterPosition() + 1));
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
            itemListener.onOptionTextChange(option.getId(), s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
