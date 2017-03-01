package com.heaton.funnyvote.ui.introduction;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlideSelectionListener;
import com.heaton.funnyvote.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by heaton on 2017/2/25.
 */

public class IntroductionFragment extends Fragment implements ISlideSelectionListener,
        ISlideBackgroundColorHolder {
    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    protected static final String ARG_TITLE = "title";
    protected static final String ARG_TITLE_TYPEFACE = "title_typeface";
    protected static final String ARG_DESC = "desc";
    protected static final String ARG_DESC_TYPEFACE = "desc_typeface";
    protected static final String ARG_DRAWABLE = "drawable";
    protected static final String ARG_BG_COLOR = "bg_color";
    protected static final String ARG_TITLE_COLOR = "title_color";
    protected static final String ARG_DESC_COLOR = "desc_color";
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.txtDescription)
    TextView txtDescription;
    @BindView(R.id.imgFragment)
    ImageView imgFragment;
    @BindView(R.id.main)
    LinearLayout main;

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
        setRetainInstance(true);

        if (getArguments() != null && getArguments().size() != 0) {
            drawable = getArguments().getInt(ARG_DRAWABLE);
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESC);
            bgColor = getArguments().getInt(ARG_BG_COLOR);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            drawable = savedInstanceState.getInt(ARG_DRAWABLE);
            title = savedInstanceState.getString(ARG_TITLE);
            description = savedInstanceState.getString(ARG_DESC);
            bgColor = savedInstanceState.getInt(ARG_BG_COLOR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_introduction, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        txtTitle.setText(title);
        txtDescription.setText(description);
        imgFragment.setImageResource(drawable);
        main.setBackgroundColor(bgColor);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
        main.setBackgroundColor(backgroundColor);
    }

    @Override
    public void onSlideSelected() {

    }

    @Override
    public void onSlideDeselected() {

    }
}
