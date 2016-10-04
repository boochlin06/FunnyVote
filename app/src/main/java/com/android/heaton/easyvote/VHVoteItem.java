package com.android.heaton.easyvote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class VHVoteItem extends RecyclerView.ViewHolder {

    /*
    ENDLESS/END mean vote is end or not end.
    UNPOLL/POLLED mean user is polled or not poll.
    NUMBER 1/2/3 ... mean option numbers
     */
    public static final int OPTION_TYPE_UNPOLL_2 = 0;
    public static final int OPTION_TYPE_UNPOLL_2_MORE = 1;
    public static final int OPTION_TYPE_POLLED_TOP_2 = 2;
    public static final int OPTION_TYPE_POLLED_TOP_1 = 3;

    public VoteData data;
    private int optionType;
    public static String BUNDLE_KEY_VOTE_ID = "VOTE_ID";


    @BindView(R.id.imgAuthorIcon)
    ImageView imgAuthorIcon;
    @BindView(R.id.txtAuthorName)
    TextView txtAuthorName;
    @BindView(R.id.txtPubTime)
    TextView txtPubTime;
    @BindView(R.id.imgFavorite)
    ImageView imgFavorite;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.txtOptionNumber1)
    TextView txtOptionNumber1;
    @BindView(R.id.imgFirstOption)
    ImageView imgFirstOption;
    @BindView(R.id.txtFirstOption)
    TextView txtFirstOption;
    @BindView(R.id.btnFirstOption)
    LinearLayout btnFirstOption;
    @BindView(R.id.txtOptionNumber2)
    TextView txtOptionNumber2;
    @BindView(R.id.imgSecondOption)
    ImageView imgSecondOption;
    @BindView(R.id.txtSecondOption)
    TextView txtSecondOption;
    @BindView(R.id.btnSecondOption)
    LinearLayout btnSecondOption;
    @BindView(R.id.imgThirdOption)
    ImageView imgThirdOption;
    @BindView(R.id.txtThirdOption)
    TextView txtThirdOption;
    @BindView(R.id.btnThirdOption)
    LinearLayout btnThirdOption;
    @BindView(R.id.txtOptionNumber3)
    TextView txtOptionNumber3;
    @BindView(R.id.imgPollCount)
    ImageView imgPollCount;
    @BindView(R.id.txtPollCount)
    TextView txtCount;
    @BindView(R.id.imgShare)
    ImageView imgShare;

    public VHVoteItem(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }


    public void setLayout(VoteData data) {
        this.data = data;
        txtTitle.setText(data.title);
        imgFavorite.setImageResource(data.isFavorite ? R.drawable.ic_star_24dp
                : R.drawable.ic_star_border_24dp);

        if (data.authorIcon.isEmpty()) {
            imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            // use GLIDE to load bitmap.
        }

        txtAuthorName.setText(data.authorName);

        if (data.voteImage.isEmpty()) {
            imgMain.setImageResource(data.localImage);
        } else {
            // use GLIDE to load bitmap.
        }

        // Check vote is end.
        if (data.endTime < System.currentTimeMillis()) {
            txtPubTime.setText("VOTE END");
        } else {
            txtPubTime.setText("2016/12/23");
        }

        txtCount.setText(Integer.toString(data.pollCount));
        // Check option type.
        //int optionType = 0;//getAdapterPosition() % 4;
        if (data.isPolled) {
            if (data.optionUserChoiceCode.equals(data.optionTopCode)) {
                optionType = OPTION_TYPE_POLLED_TOP_1;
            } else {
                optionType = OPTION_TYPE_POLLED_TOP_2;
            }
        } else {
            if (data.optionCount > 2) {
                optionType = OPTION_TYPE_UNPOLL_2_MORE;
            } else {
                optionType = OPTION_TYPE_UNPOLL_2;
            }
            // If vote is end , as Polled and not the same.
            if (data.endTime < System.currentTimeMillis()) {
                // Check no one polled or not.
                if (data.optionCount > 0) {
                    optionType = OPTION_TYPE_POLLED_TOP_1;
                } else {
                    // TODO : Add no one poll case.
                    // Check is 2 more or not
                    if (data.optionCount > 2) {
                        optionType = OPTION_TYPE_UNPOLL_2_MORE;
                    } else {
                        optionType = OPTION_TYPE_UNPOLL_2;
                    }
                }
            }
        }

        if (OPTION_TYPE_UNPOLL_2 == optionType) {

            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.GONE);
            txtFirstOption.setText(data.option1title);
            txtFirstOption.setMaxLines(1);

            txtOptionNumber1.setVisibility(View.VISIBLE);
            txtOptionNumber2.setVisibility(View.VISIBLE);
            txtOptionNumber3.setVisibility(View.GONE);

            btnSecondOption.setVisibility(View.VISIBLE);
            txtSecondOption.setText(data.option2title);
            imgSecondOption.setVisibility(View.GONE);

            btnThirdOption.setVisibility(View.GONE);
        } else if (OPTION_TYPE_UNPOLL_2_MORE == optionType) {

            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.GONE);
            txtFirstOption.setText(data.option1title);
            txtFirstOption.setMaxLines(1);

            txtOptionNumber1.setVisibility(View.VISIBLE);
            txtOptionNumber2.setVisibility(View.VISIBLE);
            txtOptionNumber3.setVisibility(View.VISIBLE);

            btnSecondOption.setVisibility(View.VISIBLE);
            txtSecondOption.setText(data.option2title);
            imgSecondOption.setVisibility(View.GONE);

            btnThirdOption.setVisibility(View.VISIBLE);
            txtThirdOption.setText("More...");
            imgThirdOption.setVisibility(View.GONE);
            btnThirdOption.setOnClickListener(MoveToVoteDetailOnClickListener);
        } else if (OPTION_TYPE_POLLED_TOP_1 == optionType) {
            // When user polled option and top option is the same.
            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setImageResource(R.drawable.ic_cup);
            txtFirstOption.setText(data.optionTopTitle);
            txtFirstOption.setMaxLines(3);

            txtOptionNumber1.setVisibility(View.GONE);
            txtOptionNumber2.setVisibility(View.GONE);
            txtOptionNumber3.setVisibility(View.GONE);

            btnSecondOption.setVisibility(View.GONE);
            txtSecondOption.setText(data.optionUserChoiceTitle);
            imgSecondOption.setVisibility(View.GONE);

            btnThirdOption.setVisibility(View.GONE);
        } else if (OPTION_TYPE_POLLED_TOP_2 == optionType) {
            // When user polled option and top option is not the same.
            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setImageResource(R.drawable.ic_cup);
            txtFirstOption.setText(data.optionTopTitle);
            txtFirstOption.setMaxLines(2);

            txtOptionNumber1.setVisibility(View.GONE);
            txtOptionNumber2.setVisibility(View.GONE);
            txtOptionNumber3.setVisibility(View.GONE);

            btnSecondOption.setVisibility(View.VISIBLE);
            txtSecondOption.setText(data.optionUserChoiceTitle);
            imgSecondOption.setVisibility(View.VISIBLE);
            imgSecondOption.setImageResource(R.drawable.ic_radio_button_checked_white_24dp);

            btnThirdOption.setVisibility(View.GONE);
        }
        // Check multi/single choices.
//        if (!(data.minOption == 1 && data.maxOption == 1)) {
//            setAllOptionToDetailClickListener();
//        }
        // Check end or not.
//        if (System.currentTimeMillis() > data.endTime) {
//            setAllOptionToDetailClickListener();
//        }
        itemView.setOnClickListener(MoveToVoteDetailOnClickListener);
    }

    private void setLayout() {
        setLayout(this.data);
    }

    private void setAllOptionToDetailClickListener() {
        btnFirstOption.setOnClickListener(MoveToVoteDetailOnClickListener);
        btnSecondOption.setOnClickListener(MoveToVoteDetailOnClickListener);
        btnThirdOption.setOnClickListener(MoveToVoteDetailOnClickListener);
    }

    public View.OnClickListener MoveToVoteDetailOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityToVoteDetail(v.getContext());
        }
    };

    private void startActivityToVoteDetail(Context context) {
        Intent intent = new Intent(context, VoteDetailContent.class);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_VOTE_ID, data.voteCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @OnClick(R.id.imgFavorite)
    public void onFavoiteClick(ImageView image) {
        data.isFavorite = !data.isFavorite;
        image.setImageResource(data.isFavorite ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);
        // TODO: Update to db.
    }

    @OnClick(R.id.imgShare)
    public void onShareClick() {
        // TODO:TURN TO share page.
    }

    @OnLongClick({R.id.btnFirstOption, R.id.btnSecondOption, R.id.btnThirdOption})
    public boolean onOptionClick(LinearLayout optionButton) {
        int optionId = optionButton.getId();
        Log.d("test", "Type:" + optionType + " Button id:" + optionId);
        if (!(data.minOption == 1 && data.maxOption == 1)) {
            startActivityToVoteDetail(optionButton.getContext());
            return true;
        }
        if (OPTION_TYPE_UNPOLL_2 == optionType || OPTION_TYPE_UNPOLL_2_MORE == optionType) {
            if (optionId == R.id.btnFirstOption) {
                data.isPolled = true;
                data.optionUserChoiceTitle = data.option1title;
                data.optionUserChoiceCount = data.option1count + 1;
                data.optionUserChoiceCode = data.option1code;
                data.pollCount = data.optionUserChoiceCount;
                if (data.optionUserChoiceCount > data.optionTopCount) {
                    data.optionTopCode = data.optionUserChoiceCode;
                    data.optionTopTitle = data.optionUserChoiceTitle;
                }
                itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setLayout();
                    }
                }, 500);
                setLayout(this.data);
            } else if (optionId == R.id.btnSecondOption) {
                data.isPolled = true;
                data.optionUserChoiceTitle = data.option2title;
                data.optionUserChoiceCount = data.option2Count + 1;
                data.optionUserChoiceCode = data.option2code;
                data.pollCount = data.optionUserChoiceCount;
                if (data.optionUserChoiceCount > data.optionTopCount) {
                    data.optionTopCode = data.optionUserChoiceCode;
                    data.optionTopTitle = data.optionUserChoiceTitle;
                }
                itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setLayout();
                    }
                }, 500);
            } else if (optionId == R.id.btnThirdOption) {
                startActivityToVoteDetail(optionButton.getContext());
            }

            // TODO: update to db.
        } else if (OPTION_TYPE_POLLED_TOP_1 == optionType) {
            startActivityToVoteDetail(optionButton.getContext());
        } else if (OPTION_TYPE_POLLED_TOP_2 == optionType) {
            startActivityToVoteDetail(optionButton.getContext());
        }
        return true;
    }
}