package com.heaton.funnyvote.ui.createvote;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.database.Option;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.List;

/**
 * Created by heaton on 2016/9/1.
 */

public class CreateVoteTabOptionFragment extends Fragment implements CreateVoteContract.OptionFragmentView {
    private RecyclerView ryOptions;
    private View rootView;
    private OptionCreateItemAdapter optionItemAdapter;
    private OptionItemListener itemListener;
    private CreateVoteContract.Presenter presenter;
    ImageView imgMain;
    ImageView imgPick;

    public CreateVoteTabOptionFragment() {
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

        itemListener = new OptionItemListener() {
            @Override
            public void onOptionTextChange(long optionId, String newOptionText) {
                presenter.reviseOption(optionId, newOptionText);
            }

            @Override
            public void onOptionAddNew() {
                presenter.addNewOption();
            }

            @Override
            public void onOptionRemove(long optionId) {
                presenter.removeOption(optionId);
            }
        };

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.setOptionFragmentView(this);
    }

    @Override
    public void setUpOptionAdapter(List<Option> optionList) {
        optionItemAdapter = new OptionCreateItemAdapter(optionList, itemListener);
        ryOptions.setAdapter(optionItemAdapter);
    }

    @Override
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

    @Override
    public void setPresenter(CreateVoteContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void refreshOptions() {
        optionItemAdapter.notifyDataSetChanged();
    }

    public interface OptionItemListener {
        void onOptionTextChange(long optionId, String newOptionText);

        void onOptionAddNew();

        void onOptionRemove(long optionId);
    }
}
