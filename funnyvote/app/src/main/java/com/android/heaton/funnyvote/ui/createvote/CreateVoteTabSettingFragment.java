package com.android.heaton.funnyvote.ui.createvote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import java.util.Calendar;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.database.VoteData;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabSettingFragment extends Fragment {

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
    @BindView(R.id.txtMemberOnly)
    TextView txtMemberOnly;
    @BindView(R.id.swtMemberOnly)
    Switch swtMemberOnly;
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

        swtAnonymous.setChecked(false);
        swtMemberOnly.setChecked(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public VoteData getVoteSettings() {
        return voteSettings;
    }

    @OnClick(R.id.txtEndTimeDetail)
    public void onTimeDetailClick() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH,7);
        DatePickerDialog timeSetting = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view
                            , int year, int monthOfYear, int dayOfMonth) {
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(year,monthOfYear,dayOfMonth);
                        voteSettings.setEndTime(endTime.getTimeInMillis());
                        txtEndTimeDetail.setText(Util.getDate(endTime.getTimeInMillis(),"yyyy/MM/dd"));
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        timeSetting.show(getActivity().getFragmentManager(),"End time");
    }
}
