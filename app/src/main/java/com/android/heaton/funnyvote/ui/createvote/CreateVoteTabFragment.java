package com.android.heaton.funnyvote.ui.createvote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabFragment extends Fragment {
    public static final String TAB_OPTIONS = "tab_options";
    public static final String TAB_SETTINGS = "tab_settings";
    RecyclerView ryOptions;
    private View rootView;
    private String tab = TAB_OPTIONS;
    private OptionCreateItemAdapter optionAdapter;

    public CreateVoteTabFragment() {
        super();
    }

    public CreateVoteTabFragment(String tab) {
        this.tab = tab;
    }

    public static CreateVoteTabFragment newTabFragment(String tab) {
        return new CreateVoteTabFragment(tab);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (tab.equals(TAB_OPTIONS)) {
            rootView = inflater.inflate(R.layout.fragment_create_vote_tab_options, container, false);
            ryOptions = (RecyclerView) rootView.findViewById(R.id.ryOptions);
            initOptionsRecyclerView();
        } else {
            rootView = inflater.inflate(R.layout.fragment_create_vote_tab_settings, container, false);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (tab.equals(TAB_OPTIONS)) {
            initOptionsRecyclerView();
        } else {

        }
    }

    private void initOptionsRecyclerView() {
        optionAdapter = new OptionCreateItemAdapter(getActivity());
        ryOptions.setAdapter(optionAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEventSubmit(EventBusController.SubmitMessageEvent event) {
        if (TAB_OPTIONS.equals(tab)) {
            // Save options to db.
            optionAdapter.getOptionList();
            Log.d("test", "save options");
        } else if (TAB_SETTINGS.equals(tab)) {
            // Save vote settings
            Log.d("test", "save options settings");
        }
    }
}
