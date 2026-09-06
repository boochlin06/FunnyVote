package com.heaton.funnyvote.ui.introduction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlideSelectionListener;
import com.heaton.funnyvote.databinding.FragmentIntroductionBinding;

/**
 * Created by heaton on 2017/2/25.
 */
public class IntroductionFragment extends Fragment implements ISlideSelectionListener,
        ISlideBackgroundColorHolder {
    public static final String ARG_TITLE = "title";
    protected static final String ARG_DESC = "desc";
    protected static final String ARG_DRAWABLE = "drawable";
    protected static final String ARG_BG_COLOR = "bg_color";

    private FragmentIntroductionBinding binding;
    private int drawable, bgColor;
    private String title, description;

    public static IntroductionFragment newInstance(CharSequence title, CharSequence description,
                                                   @DrawableRes int imageDrawable, @ColorInt int bgColor) {
        IntroductionFragment slide = new IntroductionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title.toString());
        args.putString(ARG_DESC, description.toString());
        args.putInt(ARG_DRAWABLE, imageDrawable);
        args.putInt(ARG_BG_COLOR, bgColor);
        slide.setArguments(args);

        return slide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().size() != 0) {
            drawable = getArguments().getInt(ARG_DRAWABLE);
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESC);
            bgColor = getArguments().getInt(ARG_BG_COLOR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentIntroductionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            drawable = savedInstanceState.getInt(ARG_DRAWABLE);
            title = savedInstanceState.getString(ARG_TITLE);
            description = savedInstanceState.getString(ARG_DESC);
            bgColor = savedInstanceState.getInt(ARG_BG_COLOR);
        }
        if (binding != null) {
            binding.txtTitle.setText(title);
            binding.txtDescription.setText(description);
            binding.imgFragment.setImageResource(drawable);
            binding.main.setBackgroundColor(bgColor);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(ARG_DRAWABLE, drawable);
        outState.putString(ARG_TITLE, title);
        outState.putString(ARG_DESC, description);
        outState.putInt(ARG_BG_COLOR, bgColor);
        super.onSaveInstanceState(outState);
    }

    @Override
    public int getDefaultBackgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        if (binding != null) {
            binding.main.setBackgroundColor(backgroundColor);
        }
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {

    }
}
