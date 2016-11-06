package com.android.heaton.funnyvote.ui.createvote;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabSettingFragment extends Fragment {

    private static final long DEFAULT_END_TIME_INTERNAL = 7 * 86400 * 1000;

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

    private VoteData voteSettings;

    public CreateVoteTabSettingFragment() {
    }

    public static CreateVoteTabSettingFragment newTabFragment() {
        return new CreateVoteTabSettingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_vote_tab_settings, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initVoteSettings();
    }

    private void initVoteSettings() {
        voteSettings = new VoteData();
        voteSettings.setMaxOption(1);
        edtMaxOption.setText(Integer.toString(voteSettings.getMaxOption()));
        voteSettings.setMinOption(1);
        edtMinOption.setText(Integer.toString(voteSettings.getMinOption()));
        voteSettings.setIsUserCanAddOption(false);
        swtUserAdd.setChecked(voteSettings.getIsUserCanAddOption());
        voteSettings.setIsCanPreviewResult(false);
        swtPreResult.setChecked(voteSettings.getIsUserCanAddOption());
        voteSettings.setIsNeedPassword(false);
        swtNeedPwd.setChecked(voteSettings.getIsNeedPassword());
        voteSettings.setSecurity(VoteData.SECURITY_PUBLIC);
        txtSecurityDetail.setText(getString(R.string.create_vote_tab_settings_public));
        voteSettings.setEndTime(System.currentTimeMillis() + DEFAULT_END_TIME_INTERNAL);
        txtEndTimeDetail.setText(Util.getDate(voteSettings.getEndTime(), "yyyy/MM/dd"));

        swtAnonymous.setChecked(false);
    }

    public VoteData getVoteSettings() {
        voteSettings.setMaxOption(edtMaxOption.getText().length() == 0 ? 0 :
                Integer.parseInt(edtMaxOption.getText().toString()));
        voteSettings.setMinOption(edtMinOption.getText().length() == 0 ? 0 :
                Integer.parseInt(edtMinOption.getText().toString()));
        voteSettings.setIsUserCanAddOption(swtUserAdd.isChecked());
        if (swtAnonymous.isChecked()) {
            if (edtAuthorName.getText().length() == 0) {
                voteSettings.setAuthorName(getString(R.string.create_vote_tab_settings_anonymous));
            } else {
                voteSettings.setAuthorName(edtAuthorName.getText().toString());
            }
        } else {
            List<User> users = DataLoader.getInstance(getContext()).getUserDao().loadAll();
            if (users.size() > 0) {
                User user = users.get(0);
                voteSettings.setAuthorIcon(user.getUserIcon());
                voteSettings.setAuthorCode(user.getEmail());
                voteSettings.setAuthorName(user.getUserName());
            }
        }
        voteSettings.setIsCanPreviewResult(swtPreResult.isChecked());
        voteSettings.setStartTime(System.currentTimeMillis());

        voteSettings.setIsNeedPassword(swtNeedPwd.isChecked());
        if (swtNeedPwd.isChecked()) {
            voteSettings.password = edtPwd.getText().toString();
        }
        return voteSettings;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.txtEndTimeDetail, R.id.txtEndTime})
    public void onTimeDetailClick() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, 7);
        DatePickerDialog timeSetting = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view
                            , int year, int monthOfYear, int dayOfMonth) {
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(year, monthOfYear, dayOfMonth);
                        if (endTime.getTimeInMillis() < System.currentTimeMillis()) {
                            Toast.makeText(getContext(), getString(R.string.create_vote_toast_endtime_more_than_current)
                                    , Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            voteSettings.setEndTime(endTime.getTimeInMillis());
                            txtEndTimeDetail.setText(Util.getDate(endTime.getTimeInMillis(), "yyyy/MM/dd"));
                        }
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        timeSetting.show(getActivity().getFragmentManager(), "End time");
    }

    int SecurityType = 0;

    @OnClick({R.id.txtSecurityDetail, R.id.txtSecurity})
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
                            voteSettings.setSecurity(VoteData.SECURITY_PUBLIC);
                            txtSecurityDetail.setText(R.string.create_vote_tab_settings_public);
                        } else {
                            voteSettings.setSecurity(VoteData.SECURITY_PRIVATE);
                            txtSecurityDetail.setText(R.string.create_vote_tab_settings_private);
                        }
                        dialog.dismiss();
                    }
                });
        builder.setTitle(getString(R.string.vote_detail_dialog_security));
        builder.show();
    }
}
