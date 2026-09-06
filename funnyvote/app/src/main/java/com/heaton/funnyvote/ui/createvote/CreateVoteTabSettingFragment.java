package com.heaton.funnyvote.ui.createvote;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.databinding.FragmentCreateVoteTabSettingsBinding;
import com.heaton.funnyvote.utils.Util;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.Calendar;

/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabSettingFragment extends Fragment implements CreateVoteContract.SettingFragmentView {

    private static final long DEFAULT_END_TIME = 30;

    private FragmentCreateVoteTabSettingsBinding binding;
    private CreateVoteContract.Presenter presenter;
    private int SecurityType = 0;

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
        binding = FragmentCreateVoteTabSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setSettingFragmentView(this);

        View.OnClickListener timeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeDetailClick();
            }
        };
        binding.txtEndTimeDetail.setOnClickListener(timeClickListener);
        binding.txtEndTime.setOnClickListener(timeClickListener);
        binding.imgEndTime.setOnClickListener(timeClickListener);

        View.OnClickListener securityClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSecurityDetailClick();
            }
        };
        binding.txtSecurityDetail.setOnClickListener(securityClickListener);
        binding.txtSecurity.setOnClickListener(securityClickListener);
        binding.imgSecurity.setOnClickListener(securityClickListener);
    }

    @Override
    public void setUpVoteSettings(VoteData voteSettings) {
        if (binding == null) return;
        binding.edtMaxOption.setText(Integer.toString(voteSettings.getMaxOption()));
        binding.edtMinOption.setText(Integer.toString(voteSettings.getMinOption()));
        binding.swtUserAdd.setChecked(voteSettings.getIsUserCanAddOption());
        binding.swtPreResult.setChecked(voteSettings.getIsUserCanAddOption());
        binding.swtNeedPwd.setChecked(voteSettings.getIsNeedPassword());
        binding.swtNeedPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSwtNeedPwd(isChecked);
            }
        });
        updateSwtNeedPwd(voteSettings.getIsNeedPassword());
        binding.txtSecurityDetail.setText(getString(R.string.create_vote_tab_settings_public));
        voteSettings.setEndTime(System.currentTimeMillis() + DEFAULT_END_TIME * 86400 * 1000);
        binding.txtEndTimeDetail.setText(Util.getDate(voteSettings.getEndTime(), "yyyy/MM/dd"));

        binding.swtAnonymous.setChecked(false);
    }

    @Override
    public void updateSwtNeedPwd(boolean isChecked) {
        if (binding == null) return;
        if (isChecked) {
            binding.edtPwd.setVisibility(View.VISIBLE);
            binding.txtPwd.setVisibility(View.VISIBLE);
        } else {
            binding.edtPwd.setVisibility(View.INVISIBLE);
            binding.txtPwd.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void updateUserSetting(User user) {
        if (binding == null) return;
        binding.edtAuthorName.setText(user.getUserName());
    }

    @Override
    public VoteData getFinalVoteSettings(VoteData oldVoteData) {
        if (binding == null) return oldVoteData;
        VoteData finalVoteSettings = oldVoteData;
        finalVoteSettings.setMaxOption(binding.edtMaxOption.getText().length() == 0 ? 0 :
                Integer.parseInt(binding.edtMaxOption.getText().toString()));
        finalVoteSettings.setMinOption(binding.edtMinOption.getText().length() == 0 ? 0 :
                Integer.parseInt(binding.edtMinOption.getText().toString()));
        finalVoteSettings.setIsUserCanAddOption(binding.swtUserAdd.isChecked());
        finalVoteSettings.setIsCanPreviewResult(binding.swtPreResult.isChecked());
        finalVoteSettings.setIsNeedPassword(binding.swtNeedPwd.isChecked());
        if (binding.swtNeedPwd.isChecked()) {
            finalVoteSettings.password = binding.edtPwd.getText().toString();
        }
        return finalVoteSettings;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

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
        timeSetting.show(requireActivity().getSupportFragmentManager(), "End time");
    }

    public void onSecurityDetailClick() {
        if (getContext() == null || binding == null) return;
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
                        if (binding == null) return;
                        if (SecurityType == 0) {
                            presenter.updateVoteSecurity(VoteData.SECURITY_PUBLIC);
                            binding.txtSecurityDetail.setText(R.string.create_vote_tab_settings_public);
                        } else {
                            presenter.updateVoteSecurity(VoteData.SECURITY_PRIVATE);
                            binding.txtSecurityDetail.setText(R.string.create_vote_tab_settings_private);
                        }
                        dialog.dismiss();
                    }
                });
        builder.setTitle(getString(R.string.vote_detail_dialog_security));
        builder.show();
    }

    @Override
    public void setPresenter(CreateVoteContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
