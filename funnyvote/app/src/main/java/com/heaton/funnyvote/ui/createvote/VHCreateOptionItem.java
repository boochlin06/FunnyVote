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
import com.heaton.funnyvote.eventbus.EventBusManager;

import org.greenrobot.eventbus.EventBus;

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
    int position = 0;
    private optionEditTextListener optionEditTextListener;
    private Option option;

    public VHCreateOptionItem(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setLayout(int viewType, int position, Option option) {
        this.position = position;
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
                optionEditTextListener = new optionEditTextListener();
            }
            edtOptionTitle.addTextChangedListener(optionEditTextListener);
        }
    }

    @OnClick(R.id.relAdd)
    public void addNewOption() {
        EventBus.getDefault().post(new EventBusManager.OptionControlEvent(0
                , null, EventBusManager.OptionControlEvent.OPTION_ADD, null));
    }

    @OnClick(R.id.imgDeleteOption)
    public void removeOption() {
        EventBus.getDefault().post(new EventBusManager
                .OptionControlEvent(option.getId(), null
                , EventBusManager.OptionControlEvent.OPTION_REMOVE, option.getCode()));
    }

    private final class optionEditTextListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EventBus.getDefault().post(new EventBusManager
                    .OptionControlEvent(option.getId(), s.toString()
                    , EventBusManager.OptionControlEvent.OPTION_INPUT_TEXT, option.getCode()));

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
