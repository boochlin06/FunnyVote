package com.android.heaton.easyvote;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabFragment extends Fragment {
    RecyclerView ryOptions;
    private View rootView;
    private String tab = TAB_OPTIONS;
    public static final String TAB_OPTIONS = "tab_options";
    public static final String TAB_SETTINGS = "tab_settings";

    public CreateVoteTabFragment() {
        super();
    }

    public CreateVoteTabFragment(String tab) {
        this.tab = tab;
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
        List<Option> optionList = new ArrayList<>();
        for (int i = 0 ; i < 2 ; i++) {
            Option initialOption = new Option();
            initialOption.setContent("Please input vote option content.");
            optionList.add(new Option());
        }

        ryOptions.setAdapter(new OptionCreateItemAdapter(getActivity(), optionList));
    }

    public static CreateVoteTabFragment newTabFragment(String tab) {
        return new CreateVoteTabFragment(tab);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
