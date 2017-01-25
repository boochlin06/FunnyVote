package com.android.heaton.funnyvote.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.votedetail.VoteDetailContentActivity;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class VHVoteWallItem extends RecyclerView.ViewHolder {

    public static final int OPTION_TYPE_UNPOLL_2 = 0;
    public static final int OPTION_TYPE_UNPOLL_2_MORE = 1;
    public static final int OPTION_TYPE_POLLED_TOP_2 = 2;
    public static final int OPTION_TYPE_POLLED_TOP_1 = 3;
    public static String BUNDLE_KEY_VOTE_CODE = "VOTE_ID";
    public VoteData data;
    public View.OnClickListener MoveToVoteDetailOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityToVoteDetail(v.getContext(), data.getVoteCode());
        }
    };
    @BindView(R.id.imgAuthorIcon)
    CircleImageView imgAuthorIcon;
    @BindView(R.id.txtAuthorName)
    TextView txtAuthorName;
    @BindView(R.id.txtPubTime)
    TextView txtPubTime;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.imgBarPollCount)
    ImageView imgBarPollCount;
    @BindView(R.id.txtBarPollCount)
    TextView txtBarPollCount;
    @BindView(R.id.relBarPollCount)
    RelativeLayout relBarPollCount;
    @BindView(R.id.imgBarShare)
    ImageView imgBarShare;
    @BindView(R.id.txtBarShare)
    TextView txtBarShare;
    @BindView(R.id.relBarShare)
    RelativeLayout relBarShare;
    @BindView(R.id.imgBarFavorite)
    ImageView imgBarFavorite;
    @BindView(R.id.txtBarFavorite)
    TextView txtBarFavorite;
    @BindView(R.id.relBarFavorite)
    RelativeLayout relBarFavorite;
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
    @BindView(R.id.txtOptionNumber3)
    TextView txtOptionNumber3;
    @BindView(R.id.imgThirdOption)
    ImageView imgThirdOption;
    @BindView(R.id.txtThirdOption)
    TextView txtThirdOption;
    @BindView(R.id.btnThirdOption)
    LinearLayout btnThirdOption;
    private int optionType;


    public VHVoteWallItem(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }

    public void setLayout(VoteData data) {
        this.data = data;
        txtTitle.setText(data.getTitle());
        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp
                : R.drawable.ic_star_border_24dp);

        if (data.getAuthorIcon() == null || data.getAuthorIcon().isEmpty()) {
            if (data.getAuthorName() != null && !data.getAuthorName().isEmpty()) {
                TextDrawable drawable = TextDrawable.builder().beginConfig().width(36).height(36).endConfig()
                        .buildRound(data.getAuthorName().substring(0, 1), R.color.primary_light);
                imgAuthorIcon.setImageDrawable(drawable);
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp);
            }
        } else {
            Glide.with(itemView.getContext())
                    .load(data.getAuthorIcon())
                    .override((int) (Util.convertDpToPixel(itemView.getContext().getResources()
                                    .getDimension(R.dimen.image_author_size), itemView.getContext()))
                            , (int) (Util.convertDpToPixel(itemView.getContext().getResources()
                                    .getDimension(R.dimen.image_author_size), itemView.getContext())))
                    .fitCenter()
                    .crossFade()
                    .into(imgAuthorIcon);
        }

        txtAuthorName.setText(data.getAuthorName());

        if (data.getVoteImage() == null || data.getVoteImage().isEmpty()) {
            imgMain.setImageResource(data.getLocalImage());
        } else {
            Glide.with(itemView.getContext())
                    .load(data.getVoteImage())
                    .override((int) (Util.convertDpToPixel(320, itemView.getContext()))
                            , (int) (Util.convertDpToPixel(itemView.getContext().getResources()
                                    .getDimension(R.dimen.image_main_height), itemView.getContext())))
                    .fitCenter()
                    .crossFade()
                    .into(imgMain);
        }

        // Check vote is end.
        if (data.getEndTime() < System.currentTimeMillis()) {
            txtPubTime.setText(itemView.getContext().getString(R.string.Wall_item_vote_end));
        } else {
            txtPubTime.setText(Util.getDate(data.getStartTime(), "dd/MM hh:mm")
                    + " ~ " + Util.getDate(data.getEndTime(), "dd/MM hh:mm"));
        }

        txtBarPollCount.setText(String.format(itemView.getContext()
                .getString(R.string.Wall_item_bar_vote_count), data.getPollCount()));
        // Check option type.
        if (data.getIsPolled() && data.getOptionUserChoiceCode() != null) {
            if (data.getOptionUserChoiceCode().equals(data.getOptionTopCode())) {
                optionType = OPTION_TYPE_POLLED_TOP_1;
            } else {
                optionType = OPTION_TYPE_POLLED_TOP_2;
            }
        } else {
            if (data.getOptionCount() > 2) {
                optionType = OPTION_TYPE_UNPOLL_2_MORE;
            } else {
                optionType = OPTION_TYPE_UNPOLL_2;
            }
            // If vote is end , as Polled and not the same.
            if (data.getEndTime() < System.currentTimeMillis()) {
                optionType = OPTION_TYPE_POLLED_TOP_1;
            }
        }

        if (OPTION_TYPE_UNPOLL_2 == optionType) {

            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.GONE);
            txtFirstOption.setText(data.getOption1Title());
            txtFirstOption.setMaxLines(1);

            txtOptionNumber1.setVisibility(View.VISIBLE);
            txtOptionNumber2.setVisibility(View.VISIBLE);
            txtOptionNumber3.setVisibility(View.GONE);

            btnSecondOption.setVisibility(View.VISIBLE);
            txtSecondOption.setText(data.getOption2Title());
            imgSecondOption.setVisibility(View.GONE);

            btnThirdOption.setVisibility(View.GONE);
        } else if (OPTION_TYPE_UNPOLL_2_MORE == optionType) {

            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.GONE);
            txtFirstOption.setText(data.getOption1Title());
            txtFirstOption.setMaxLines(1);

            txtOptionNumber1.setVisibility(View.VISIBLE);
            txtOptionNumber2.setVisibility(View.VISIBLE);
            txtOptionNumber3.setVisibility(View.VISIBLE);

            btnSecondOption.setVisibility(View.VISIBLE);
            txtSecondOption.setText(data.getOption2Title());
            imgSecondOption.setVisibility(View.GONE);

            btnThirdOption.setVisibility(View.VISIBLE);
            txtThirdOption.setText(itemView.getContext().getString(R.string.Wall_item_more));
            imgThirdOption.setVisibility(View.GONE);
            btnThirdOption.setOnClickListener(MoveToVoteDetailOnClickListener);
        } else if (OPTION_TYPE_POLLED_TOP_1 == optionType) {
            // When user polled option and top option is the same.
            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.VISIBLE);
            // Check no one poll case.
            if (data.getPollCount() > 0) {
                imgFirstOption.setImageResource(R.drawable.ic_cup);
                txtFirstOption.setText(data.getOptionTopTitle());
            } else {
                imgFirstOption.setImageResource(R.drawable.ic_radio_button_checked_white_24dp);
                txtFirstOption.setText(itemView.getContext().getString(R.string.Wall_item_no_one_poll));
            }
            txtFirstOption.setMaxLines(3);

            txtOptionNumber1.setVisibility(View.GONE);
            txtOptionNumber2.setVisibility(View.GONE);
            txtOptionNumber3.setVisibility(View.GONE);

            btnSecondOption.setVisibility(View.GONE);
            txtSecondOption.setText(data.getOptionUserChoiceTitle());
            imgSecondOption.setVisibility(View.GONE);

            btnThirdOption.setVisibility(View.GONE);
        } else if (OPTION_TYPE_POLLED_TOP_2 == optionType) {
            // When user polled option and top option is not the same.
            btnFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setVisibility(View.VISIBLE);
            imgFirstOption.setImageResource(R.drawable.ic_cup);
            txtFirstOption.setText(data.getOptionTopTitle());
            txtFirstOption.setMaxLines(2);

            txtOptionNumber1.setVisibility(View.GONE);
            txtOptionNumber2.setVisibility(View.GONE);
            txtOptionNumber3.setVisibility(View.GONE);

            btnSecondOption.setVisibility(View.VISIBLE);
            txtSecondOption.setText(data.getOptionUserChoiceTitle());
            imgSecondOption.setVisibility(View.VISIBLE);
            imgSecondOption.setImageResource(R.drawable.ic_radio_button_checked_white_24dp);

            btnThirdOption.setVisibility(View.GONE);
        }
        itemView.setOnClickListener(MoveToVoteDetailOnClickListener);
    }

    private void setLayout() {
        setLayout(this.data);
    }

    public static void startActivityToVoteDetail(Context context, String voteCode) {
        Intent intent = new Intent(context, VoteDetailContentActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_VOTE_CODE, voteCode);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @OnClick(R.id.relBarFavorite)
    public void onBarFavoriteClick() {
        if (!Util.isNetworkConnected(itemView.getContext())) {
            Toast.makeText(itemView.getContext(), R.string.toast_network_connect_error_favorite, Toast.LENGTH_SHORT).show();
            return;
        }
        data.setIsFavorite(!data.getIsFavorite());
        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);
        if (data.getIsFavorite()) {
            Toast.makeText(itemView.getContext()
                    , R.string.vote_detail_toast_add_favorite, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(itemView.getContext()
                    , R.string.vote_detail_toast_remove_favorite, Toast.LENGTH_SHORT).show();
        }
        DataLoader.getInstance(itemView.getContext()).getVoteDataDao().insertOrReplace(data);
        EventBus.getDefault().post(new EventBusController
                .VoteDataControlEvent(data, EventBusController.VoteDataControlEvent.VOTE_FAVORITE));
    }

    @OnClick(R.id.relBarShare)
    public void onBarShareClick() {
        VoteDetailContentActivity.sendShareIntent(itemView.getContext(), data);
    }

    @OnClick({R.id.imgAuthorIcon, R.id.txtAuthorName})
    public void onAuthorClick() {
        VoteDetailContentActivity.sendPersonalDetailIntent(itemView.getContext(), data);
    }

    @OnLongClick({R.id.btnFirstOption, R.id.btnSecondOption, R.id.btnThirdOption})
    public boolean onOptionLongClick(LinearLayout optionButton) {
        if (!(data.getMinOption() == 1 && data.getMaxOption() == 1)) {
            startActivityToVoteDetail(optionButton.getContext(), data.getVoteCode());
            return true;
        }
        updateUI(optionButton);
        return true;
    }

    private void updateUI(LinearLayout optionButton) {
        int optionId = optionButton.getId();
        if (OPTION_TYPE_UNPOLL_2 == optionType || OPTION_TYPE_UNPOLL_2_MORE == optionType) {
            if (optionId == R.id.btnFirstOption) {
                if (!Util.isNetworkConnected(itemView.getContext())) {
                    Toast.makeText(itemView.getContext(), R.string.toast_network_connect_error_quick_poll, Toast.LENGTH_SHORT).show();
                    return;
                } else if (data.getIsNeedPassword()) {
                    EventBus.getDefault().post(new EventBusController
                            .VoteDataControlEvent(data, data.getOption1Code(), EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
                } else {
                    EventBus.getDefault().post(new EventBusController
                            .VoteDataControlEvent(data, data.getOption1Code(), EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
                    data.setIsPolled(true);
                    data.setOptionUserChoiceTitle(data.getOption1Title());
                    data.setOptionUserChoiceCount(data.getOption1Count() + 1);
                    data.setOptionUserChoiceCode(data.getOption1Code());
                    data.setPollCount(data.getPollCount() + 1);
                    if (data.getOptionUserChoiceCount() > data.getOptionTopCount()) {
                        data.setOptionTopCode(data.getOptionUserChoiceCode());
                        data.setOptionTopTitle(data.getOptionUserChoiceTitle());
                        data.setOptionTopCount(data.getOptionUserChoiceCount());
                    }
                    itemView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setLayout();
                        }
                    }, 500);
                }
            } else if (optionId == R.id.btnSecondOption) {
                if (!Util.isNetworkConnected(itemView.getContext())) {
                    Toast.makeText(itemView.getContext(), R.string.toast_network_connect_error_quick_poll, Toast.LENGTH_SHORT).show();
                    return;
                } else if (data.getIsNeedPassword()) {
                    EventBus.getDefault().post(new EventBusController
                            .VoteDataControlEvent(data, data.getOption2Code(), EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
                } else {
                    EventBus.getDefault().post(new EventBusController
                            .VoteDataControlEvent(data, data.getOption2Code(), EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
                    data.setIsPolled(true);
                    data.setOptionUserChoiceTitle(data.getOption2Title());
                    data.setOptionUserChoiceCount(data.getOption2Count() + 1);
                    data.setOptionUserChoiceCode(data.getOption2Code());
                    data.setPollCount(data.getPollCount() + 1);
                    if (data.getOptionUserChoiceCount() > data.getOptionTopCount()) {
                        data.setOptionTopCode(data.getOptionUserChoiceCode());
                        data.setOptionTopTitle(data.getOptionUserChoiceTitle());
                        data.setOptionTopCount(data.getOptionUserChoiceCount());
                    }
                    itemView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setLayout();
                        }
                    }, 500);
                }
            } else if (optionId == R.id.btnThirdOption) {
                startActivityToVoteDetail(itemView.getContext(), data.getVoteCode());
            }
        } else if (OPTION_TYPE_POLLED_TOP_1 == optionType) {
            startActivityToVoteDetail(itemView.getContext(), data.getVoteCode());
        } else if (OPTION_TYPE_POLLED_TOP_2 == optionType) {
            startActivityToVoteDetail(itemView.getContext(), data.getVoteCode());
        }
    }

}