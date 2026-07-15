package com.heaton.funnyvote.ui.createvote;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;
import com.bumptech.glide.Glide;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabOptionFragment extends Fragment {
    private RecyclerView ryOptions;
    private View rootView;
    private OptionCreateItemAdapter optionItemAdapter;
    private List<Option> optionList;
    ImageView imgMain;
    ImageView imgPick;

    public CreateVoteTabOptionFragment(){
    }

    public static CreateVoteTabOptionFragment newTabFragment() {
        return new CreateVoteTabOptionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_create_vote_tab_options, container, false);
        ryOptions = (RecyclerView) rootView.findViewById(R.id.ryOptions);
        imgPick = (ImageView) rootView.findViewById(R.id.imgPick);
        imgMain = (ImageView) rootView.findViewById(R.id.imgMain);

        View.OnClickListener pickImageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.startPickImageActivity(getActivity());
            }
        };
        imgMain.setOnClickListener(pickImageListener);
        imgPick.setOnClickListener(pickImageListener);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        optionList = new ArrayList<>();
        for (long i = 0; i < 2; i++) {
            Option option = new Option();
            option.setId(i);
            option.setCount(0);
            optionList.add(option);
        }
        initOptionsRecyclerView();
    }

    private void initOptionsRecyclerView() {
        optionItemAdapter = new OptionCreateItemAdapter(optionList);
        ryOptions.setAdapter(optionItemAdapter);
    }
    public List<Option> getOptionList() {
        return optionList;
    }

    public void setVoteImage(Uri imageUri) {
        imgMain.setVisibility(View.VISIBLE);
        imgPick.setVisibility(View.GONE);
        Glide.with(this)
                .load(imageUri)
                .into(imgMain);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void notifyOptionChange() {
        optionItemAdapter.notifyDataSetChanged();
    }
}
