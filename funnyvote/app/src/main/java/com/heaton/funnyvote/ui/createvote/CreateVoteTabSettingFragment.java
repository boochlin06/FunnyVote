package com.heaton.funnyvote.ui.createvote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.di.ActivityScoped;
import com.heaton.funnyvote.utils.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerFragment;


/**
 * Created by heaton on 2016/9/1.
 */
@ActivityScoped
public class CreateVoteTabSettingFragment extends DaggerFragment implements CreateVoteContract.SettingFragmentView {

    private static final long DEFAULT_END_TIME = 30;

    @BindView(R.id.lineOption)
    View lineOption;
    @BindView(R.id.txtOption)
    TextView txtOption;
    @BindView(R.id.txtMaxOption)
    TextView txtMaxOption;
    @BindView(R.id.edtMaxOption)
    EditText edtMaxOption;
    @BindView(R.id.txtMinOption)
    TextView txtMinOption;
    @BindView(R.id.edtMinOption)
    EditText edtMinOption;
    @BindView(R.id.txtUserAdd)
    TextView txtUserAdd;
    @BindView(R.id.swtUserAdd)
    Switch swtUserAdd;
    @BindView(R.id.lineAuthor)
    View lineAuthor;
    @BindView(R.id.txtAuthor)
    TextView txtAuthor;
    @BindView(R.id.txtAnonymous)
    TextView txtAnonymous;
    @BindView(R.id.swtAnonymous)
    Switch swtAnonymous;
    @BindView(R.id.txtAuthorName)
    TextView txtAuthorName;
    @BindView(R.id.edtAuthorName)
    EditText edtAuthorName;
    @BindView(R.id.lineVote)
    View lineVote;
    @BindView(R.id.txtVote)
    TextView txtVote;
    @BindView(R.id.txtPreResult)
    TextView txtPreResult;
    @BindView(R.id.swtPreResult)
    Switch swtPreResult;
    @BindView(R.id.txtEndTime)
    TextView txtEndTime;
    @BindView(R.id.txtEndTimeDetail)
    TextView txtEndTimeDetail;
    @BindView(R.id.txtSecurity)
    TextView txtSecurity;
    @BindView(R.id.txtSecurityDetail)
    TextView txtSecurityDetail;
    @BindView(R.id.txtNeedPwd)
    TextView txtNeedPwd;
    @BindView(R.id.swtNeedPwd)
    Switch swtNeedPwd;
    @BindView(R.id.txtPwd)
    TextView txtPwd;
    @BindView(R.id.edtPwd)
    EditText edtPwd;
    @BindView(R.id.imgEndTime)
    ImageView imgEndTime;
    @BindView(R.id.imgSecurity)
    ImageView imgSecurity;

    @Inject
    CreateVoteContract.Presenter presenter;
    private int SecurityType = 0;

    @Inject
    public CreateVoteTabSettingFragment() {
    }

    public static CreateVoteTabSettingFragment newTabFragment() {
        return new CreateVoteTabSettingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_vote_tab_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setSettingFragmentView(this);
    }

    @Override
    public void setUpVoteSettings(VoteData voteSettings) {
        edtMaxOption.setText(Integer.toString(voteSettings.getMaxOption()));
        edtMinOption.setText(Integer.toString(voteSettings.getMinOption()));
        swtUserAdd.setChecked(voteSettings.getIsUserCanAddOption());
        swtPreResult.setChecked(voteSettings.getIsUserCanAddOption());
        swtNeedPwd.setChecked(voteSettings.getIsNeedPassword());
        swtNeedPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSwtNeedPwd(isChecked);
            }
        });
        updateSwtNeedPwd(voteSettings.getIsNeedPassword());
        txtSecurityDetail.setText(getString(R.string.create_vote_tab_settings_public));
        voteSettings.setEndTime(System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000);
        txtEndTimeDetail.setText(Util.getDate(voteSettings.getEndTime(), "yyyy/MM/dd"));

        swtAnonymous.setChecked(false);
    }

    @Override
    public void updateSwtNeedPwd(boolean isChecked) {
        if (isChecked) {
            edtPwd.setVisibility(View.VISIBLE);
            txtPwd.setVisibility(View.VISIBLE);
        } else {
            edtPwd.setVisibility(View.INVISIBLE);
            txtPwd.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void updateUserSetting(User user) {
        edtAuthorName.setText(user.getUserName());
    }

    @Override
    public VoteData getFinalVoteSettings(VoteData oldVoteData) {

        VoteData finalVoteSettings = oldVoteData;
        finalVoteSettings.setMaxOption(edtMaxOption.getText().length() == 0 ? 0 :
                Integer.parseInt(edtMaxOption.getText().toString()));
        finalVoteSettings.setMinOption(edtMinOption.getText().length() == 0 ? 0 :
                Integer.parseInt(edtMinOption.getText().toString()));
        finalVoteSettings.setIsUserCanAddOption(swtUserAdd.isChecked());
        finalVoteSettings.setIsCanPreviewResult(swtPreResult.isChecked());
        finalVoteSettings.setIsNeedPassword(swtNeedPwd.isChecked());
        if (swtNeedPwd.isChecked()) {
            finalVoteSettings.password = edtPwd.getText().toString();
        }
        return finalVoteSettings;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.txtEndTimeDetail, R.id.txtEndTime, R.id.imgEndTime})
    public void onTimeDetailClick() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, (int) DEFAULT_END_TIME);
        DatePickerDialog timeSetting = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view
                            , int year, int monthOfYear, int dayOfMonth) {
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(year, monthOfYear, dayOfMonth);
                        presenter.updateVoteEndTime(endTime.getTimeInMillis());
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        timeSetting.show(getActivity().getFragmentManager(), "End time");
    }


    @OnClick({R.id.txtSecurityDetail, R.id.txtSecurity, R.id.imgSecurity})
    public void onSecurityDetailClick() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String[] allType = new String[]{getString(R.string.create_vote_tab_settings_public_hint)
                , getString(R.string.create_vote_tab_settings_private_hint)};
        builder.setSingleChoiceItems(allType, SecurityType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SecurityType = which;
            }
        });
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (SecurityType == 0) {
                            presenter.updateVoteSecurity(VoteData.SECURITY_PUBLIC);
                            txtSecurityDetail.setText(R.string.create_vote_tab_settings_public);
                        } else {
                            presenter.updateVoteSecurity(VoteData.SECURITY_PRIVATE);
                            txtSecurityDetail.setText(R.string.create_vote_tab_settings_private);
                        }
                        dialog.dismiss();
                    }
                });
        builder.setTitle(getString(R.string.vote_detail_dialog_security));
        builder.show();
    }

}
