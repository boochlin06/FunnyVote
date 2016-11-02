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
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabOptionFragment extends Fragment {
    private RecyclerView ryOptions;
    private View rootView;
    private OptionCreateItemAdapter optionAdapter;
    private List<Option> optionList;

    public CreateVoteTabOptionFragment(List<Option> optionList) {
        this.optionList = optionList;
    }
    public CreateVoteTabOptionFragment(){
        optionList = new ArrayList<>();
        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setId(i);
            option.setCount(0);
            optionList.add(option);
        }
    }

    public static CreateVoteTabOptionFragment newTabFragment(List<Option> optionList) {
        return new CreateVoteTabOptionFragment(optionList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_vote_tab_options, container, false);
        ryOptions = (RecyclerView) rootView.findViewById(R.id.ryOptions);
        initOptionsRecyclerView();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initOptionsRecyclerView();
    }

    private void initOptionsRecyclerView() {
        optionAdapter = new OptionCreateItemAdapter(optionList);
        ryOptions.setAdapter(optionAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void notifyOptionChange() {
        optionAdapter.notifyDataSetChanged();
    }
}
