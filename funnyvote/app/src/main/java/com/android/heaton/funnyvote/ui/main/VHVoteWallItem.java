package com.android.heaton.funnyvote.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.amulyakhare.textdrawable.TextDrawable;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
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
    @BindView(R.id.imgBarFavorite)
    ImageView imgBarFavorite;
    @BindView(R.id.txtBarFavorite)
    TextView txtBarFavorite;
    @BindView(R.id.relBarFavorite)
    RelativeLayout relBarFavorite;
    @BindView(R.id.imgBarShare)
    ImageView imgBarShare;
    @BindView(R.id.txtBarShare)
    TextView txtBarShare;
    @BindView(R.id.relBarShare)
    RelativeLayout relBarShare;
    @BindView(R.id.txtOptionNumber1)
    TextView txtOptionNumber1;
    @BindView(R.id.txtFirstOptionTitle)
    TextView txtFirstOptionTitle;
    @BindView(R.id.imgChampion1)
    ImageView imgChampion1;
    @BindView(R.id.progressFirstOption)
    RoundCornerProgressBar progressFirstOption;
    @BindView(R.id.txtFirstPollCountPercent)
    TextView txtFirstPollCountPercent;
    @BindView(R.id.btnFirstOption)
    CardView btnFirstOption;
    @BindView(R.id.txtOptionNumber2)
    TextView txtOptionNumber2;
    @BindView(R.id.txtSecondOptionTitle)
    TextView txtSecondOptionTitle;
    @BindView(R.id.imgChampion2)
    ImageView imgChampion2;
    @BindView(R.id.progressSecondOption)
    RoundCornerProgressBar progressSecondOption;
    @BindView(R.id.txtSecondPollCountPercent)
    TextView txtSecondPollCountPercent;
    @BindView(R.id.btnSecondOption)
    CardView btnSecondOption;
    @BindView(R.id.txtOptionNumber3)
    TextView txtOptionNumber3;
    @BindView(R.id.imgThirdOption)
    ImageView imgThirdOption;
    @BindView(R.id.txtThirdOption)
    TextView txtThirdOption;
    @BindView(R.id.btnThirdOption)
    CardView btnThirdOption;

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
                    .override((int) itemView.getContext().getResources()
                                    .getDimension(R.dimen.vote_image_author_size)
                            , (int) itemView.getContext().getResources()
                                    .getDimension(R.dimen.vote_image_author_size))
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
                    .override((int) itemView.getContext().getResources()
                                    .getDimension(R.dimen.vote_image_main_width)
                            , (int) (itemView.getContext().getResources()
                                    .getDimension(R.dimen.vote_image_main_height)))
                    .crossFade()
                    .into(imgMain);
        }

        // Check vote is end.
        if (data.getEndTime() < System.currentTimeMillis()) {
            txtPubTime.setText(itemView.getContext().getString(R.string.wall_item_vote_end));
        } else {
            txtPubTime.setText(Util.getDate(data.getStartTime(), "yyyy/MM/dd hh:mm")
                    + " ~ " + Util.getDate(data.getEndTime(), "yyyy/MM/dd hh:mm"));
        }

        txtBarPollCount.setText(String.format(itemView.getContext()
                .getString(R.string.wall_item_bar_vote_count), data.getPollCount()));

        setUpOptionArea();

        itemView.setOnClickListener(MoveToVoteDetailOnClickListener);
    }

    private void setUpOptionArea() {
        setUpOptionArea(false);
    }

    private void setUpOptionArea(boolean isQuickPoll) {
        // More than 3 options.
        if (data.getOptionCount() > 2) {
            if (data.getIsPolled() || data.getEndTime() < System.currentTimeMillis()) {
                boolean isShowTopOption = false;

                progressFirstOption.setVisibility(View.VISIBLE);
                progressFirstOption.setMax(data.getPollCount());

                progressSecondOption.setVisibility(View.VISIBLE);
                progressSecondOption.setMax(data.getPollCount());

                txtFirstPollCountPercent.setVisibility(View.VISIBLE);
                txtSecondPollCountPercent.setVisibility(View.VISIBLE);

                if (isQuickPoll) {
                    // show option 1 at first button , and option 2 at second button.
                    progressFirstOption.setVisibility(View.VISIBLE);
                    progressFirstOption.setProgress(data.getOption1Count());

                    progressSecondOption.setVisibility(View.VISIBLE);
                    progressSecondOption.setProgress(data.getOption2Count());

                    txtFirstPollCountPercent.setVisibility(View.VISIBLE);
                    txtSecondPollCountPercent.setVisibility(View.VISIBLE);

                    double percent1 = data.getPollCount() == 0 ? 0
                            : (double) data.getOption1Count() / data.getPollCount() * 100;
                    double percent2 = data.getPollCount() == 0 ? 0
                            : (double) data.getOption2Count() / data.getPollCount() * 100;
                    txtFirstPollCountPercent.setText(String.format("%3.1f%%", percent1));
                    txtSecondPollCountPercent.setText(String.format("%3.1f%%", percent2));
                    setUpFirstButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                            && data.getOptionTopCount() == data.getOption1Count()
                            && data.getOption1Count() != 0);
                    setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                            && data.getOptionTopCount() == data.getOption2Count()
                            && data.getOption2Count() != 0);
                    setUpFirstButtonProgressLayout(data.getOption1Polled());
                    setUpSecondButtonProgressLayout(data.getOption2Polled());
                    return;
                }

                // If not quick poll First button is top option , if no one poll , show first option.
                if (!TextUtils.isEmpty(data.getOptionTopCode())
                        && data.getOptionTopCount() != 0) {
                    // show top top option at second button.
                    isShowTopOption = true;
                    txtFirstOptionTitle.setText(data.getOptionTopTitle());
                    progressFirstOption.setProgress(data.getOptionTopCount());
                    double percentTop = data.getPollCount() == 0 ? 0
                            : (double) data.getOptionTopCount() / data.getPollCount() * 100;
                    txtFirstPollCountPercent.setText(String.format("%3.1f%%", percentTop));

                    setUpFirstButtonTopLayout(data.getOptionTopCount() != 0);
                    setUpFirstButtonProgressLayout(data.getOptionTopPolled());
                } else {
                    // show option 1 at second button.
                    isShowTopOption = false;
                    txtFirstOptionTitle.setText(data.getOption1Title());
                    progressFirstOption.setProgress(data.getOption1Count());
                    ;
                    double percent1 = data.getPollCount() == 0 ? 0
                            : (double) data.getOption1Count() / data.getPollCount() * 100;
                    txtFirstPollCountPercent.setText(String.format("%3.1f%%", percent1));

                    setUpFirstButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                            && data.getOptionTopCount() == data.getOption1Count()
                            && data.getOption1Count() != 0);
                    setUpFirstButtonProgressLayout(data.getOption1Polled());
                }

                // Second button is user choice if user no choice show one or second option.
                // should be different with first button.
                // if first button show first option , so no one poll ,user choice and top is 0 vote.
                // set button 2 layout.
                if (isShowTopOption) {
                    if (!data.getOptionTopCode().equals(data.getOptionUserChoiceCode())
                            && !TextUtils.isEmpty(data.getOptionUserChoiceCode())) {
                        // show user choice option at second button.
                        txtSecondOptionTitle.setText(data.getOptionUserChoiceTitle());
                        progressSecondOption.setProgress(data.getOptionUserChoiceCount());
                        double percentUserChoice = data.getPollCount() == 0 ? 0
                                : (double) data.getOptionUserChoiceCount() / data.getPollCount() * 100;
                        txtSecondPollCountPercent.setText(String.format("%3.1f%%", percentUserChoice));

                        setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                                && data.getOptionTopCount() == data.getOptionUserChoiceCount()
                                && data.getOptionUserChoiceCount() != 0);
                        setUpSecondButtonProgressLayout(true);
                    } else if (!data.getOptionTopCode().equals(data.getOption1Code())) {
                        // show option 1 at second button.
                        txtSecondOptionTitle.setText(data.getOption1Title());
                        progressSecondOption.setProgress(data.getOption1Count());
                        double percent1 = data.getPollCount() == 0 ? 0
                                : (double) data.getOption1Count() / data.getPollCount() * 100;
                        txtSecondPollCountPercent.setText(String.format("%3.1f%%", percent1));

                        setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                                && data.getOptionTopCount() == data.getOption1Count()
                                && data.getOption1Count() != 0);
                        setUpSecondButtonProgressLayout(data.getOption1Polled());
                    } else if (!data.getOptionTopCode().equals(data.getOption2Code())) {
                        // show option 2 at second button.
                        txtSecondOptionTitle.setText(data.getOption2Title());
                        progressSecondOption.setProgress(data.getOption2Count());
                        double percent2 = data.getPollCount() == 0 ? 0
                                : (double) data.getOption2Count() / data.getPollCount() * 100;
                        txtSecondPollCountPercent.setText(String.format("%3.1f%%", percent2));

                        setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                                && data.getOptionTopCount() == data.getOption2Count()
                                && data.getOption2Count() != 0);
                        setUpSecondButtonProgressLayout(data.getOption2Polled());
                    }

                } else {
                    // show option 2 at second button.
                    txtSecondOptionTitle.setText(data.getOption2Title());
                    progressSecondOption.setProgress(data.getOption2Count());
                    double percent2 = data.getPollCount() == 0 ? 0
                            : (double) data.getOption2Count() / data.getPollCount() * 100;
                    txtSecondPollCountPercent.setText(String.format("%3.1f%%", percent2));

                    setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                            && data.getOptionTopCount() == data.getOption2Count()
                            && data.getOption2Count() != 0);
                    setUpSecondButtonProgressLayout(data.getOption2Polled());
                }
            } else {
                // vote is not end or not poll.
                txtFirstOptionTitle.setText(data.getOption1Title());

                txtSecondOptionTitle.setText(data.getOption2Title());

                btnFirstOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_blue_100));
                btnSecondOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_blue_100));

                txtFirstPollCountPercent.setVisibility(View.GONE);
                txtSecondPollCountPercent.setVisibility(View.GONE);

                progressFirstOption.setVisibility(View.GONE);
                progressSecondOption.setVisibility(View.GONE);

                imgChampion1.setVisibility(View.GONE);
                imgChampion2.setVisibility(View.GONE);
            }
            txtThirdOption.setText(String.format(itemView.getContext().getString(R.string.wall_item_other_option)
                    , (data.getOptionCount() - 2)));
            btnThirdOption.setVisibility(View.VISIBLE);
            btnThirdOption.setOnClickListener(MoveToVoteDetailOnClickListener);
        } else {
            // 2 option type.
            txtFirstOptionTitle.setText(data.getOption1Title());
            txtSecondOptionTitle.setText(data.getOption2Title());
            progressFirstOption.setMax(data.getPollCount());
            progressSecondOption.setMax(data.getPollCount());

            if (data.getIsPolled() || data.getEndTime() < System.currentTimeMillis()) {
                // show option 1 at first button , and option 2 at second button.
                progressFirstOption.setVisibility(View.VISIBLE);
                progressFirstOption.setProgress(data.getOption1Count());

                progressSecondOption.setVisibility(View.VISIBLE);
                progressSecondOption.setProgress(data.getOption2Count());

                txtFirstPollCountPercent.setVisibility(View.VISIBLE);
                txtSecondPollCountPercent.setVisibility(View.VISIBLE);

                double percent1 = data.getPollCount() == 0 ? 0
                        : (double) data.getOption1Count() / data.getPollCount() * 100;
                double percent2 = data.getPollCount() == 0 ? 0
                        : (double) data.getOption2Count() / data.getPollCount() * 100;
                txtFirstPollCountPercent.setText(String.format("%3.1f%%", percent1));
                txtSecondPollCountPercent.setText(String.format("%3.1f%%", percent2));
                setUpFirstButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                        && data.getOptionTopCount() == data.getOption1Count()
                        && data.getOption1Count() != 0);
                setUpSecondButtonTopLayout(!TextUtils.isEmpty(data.getOptionTopCode())
                        && data.getOptionTopCount() == data.getOption2Count()
                        && data.getOption2Count() != 0);
                setUpFirstButtonProgressLayout(data.getOption1Polled());
                setUpSecondButtonProgressLayout(data.getOption2Polled());
            } else {
                btnFirstOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_blue_100));
                btnSecondOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_blue_100));

                txtFirstPollCountPercent.setVisibility(View.GONE);
                txtSecondPollCountPercent.setVisibility(View.GONE);

                progressFirstOption.setVisibility(View.GONE);
                progressSecondOption.setVisibility(View.GONE);

                imgChampion1.setVisibility(View.GONE);
                imgChampion2.setVisibility(View.GONE);
            }
            btnThirdOption.setVisibility(View.GONE);
        }
    }

    private void setUpFirstButtonTopLayout(boolean isTop) {
        if (isTop) {
            imgChampion1.setVisibility(View.VISIBLE);
        } else {
            imgChampion1.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpSecondButtonTopLayout(boolean isTop) {
        if (isTop) {
            imgChampion2.setVisibility(View.VISIBLE);
        } else {
            imgChampion2.setVisibility(View.INVISIBLE);
        }
    }

    private void setUpFirstButtonProgressLayout(boolean isPolled) {
        if (isPolled) {
            progressFirstOption.setProgressColor(itemView.getContext()
                    .getResources().getColor(R.color.md_red_600));
            progressFirstOption.setProgressBackgroundColor(itemView.getContext()
                    .getResources().getColor(R.color.md_red_200));
            btnFirstOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_red_100));
        } else {
            progressFirstOption.setProgressColor(itemView.getContext()
                    .getResources().getColor(R.color.md_blue_600));
            progressFirstOption.setProgressBackgroundColor(itemView.getContext()
                    .getResources().getColor(R.color.md_blue_200));
            btnFirstOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_blue_100));
        }
    }

    private void setUpSecondButtonProgressLayout(boolean isPolled) {
        if (isPolled) {
            progressSecondOption.setProgressColor(itemView.getContext()
                    .getResources().getColor(R.color.md_red_600));
            progressSecondOption.setProgressBackgroundColor(itemView
                    .getResources().getColor(R.color.md_red_200));
            btnSecondOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_red_100));
        } else {
            progressSecondOption.setProgressColor(itemView.getContext()
                    .getResources().getColor(R.color.md_blue_600));
            progressSecondOption.setProgressBackgroundColor(itemView.getContext()
                    .getResources().getColor(R.color.md_blue_200));
            btnSecondOption.setCardBackgroundColor(itemView.getResources().getColor(R.color.md_blue_100));
        }
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
            Toast.makeText(itemView.getContext(), R.string.toast_network_connect_error_favorite
                    , Toast.LENGTH_SHORT).show();
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
    public boolean onOptionLongClick(CardView optionButton) {
        if (!(data.getMinOption() == 1 && data.getMaxOption() == 1)
                || data.getIsPolled() || data.getEndTime() < System.currentTimeMillis()
                || optionButton.getId() == R.id.btnThirdOption) {
            startActivityToVoteDetail(optionButton.getContext(), data.getVoteCode());
            return true;
        }
        if (!data.getIsPolled()) {
            if (!Util.isNetworkConnected(itemView.getContext())) {
                Toast.makeText(itemView.getContext(), R.string.toast_network_connect_error_quick_poll, Toast.LENGTH_SHORT).show();
                return true;
            } else if (data.getIsNeedPassword()) {
                EventBus.getDefault().post(new EventBusController
                        .VoteDataControlEvent(data, btnFirstOption.getId() == R.id.btnFirstOption ?
                        data.getOption1Code() : data.getOption2Code()
                        , EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
            } else {
                if (optionButton.getId() == R.id.btnFirstOption) {
                    EventBus.getDefault().post(new EventBusController
                            .VoteDataControlEvent(data, data.getOption1Code()
                            , EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
                    data.setOption1Polled(true);
                    data.setOption1Count(data.getOption1Count() + 1);
                    data.setOptionUserChoiceTitle(data.getOption1Title());
                    data.setOptionUserChoiceCount(data.getOption1Count());
                    data.setOptionUserChoiceCode(data.getOption1Code());
                } else {
                    EventBus.getDefault().post(new EventBusController
                            .VoteDataControlEvent(data, data.getOption2Code()
                            , EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL));
                    data.setOption2Polled(true);
                    data.setOption2Count(data.getOption2Count() + 1);
                    data.setOptionUserChoiceTitle(data.getOption2Title());
                    data.setOptionUserChoiceCount(data.getOption2Count());
                    data.setOptionUserChoiceCode(data.getOption2Code());
                }

                if (data.getOptionUserChoiceCount() > data.getOptionTopCount()) {
                    data.setOptionTopCode(data.getOptionUserChoiceCode());
                    data.setOptionTopTitle(data.getOptionUserChoiceTitle());
                    data.setOptionTopCount(data.getOptionUserChoiceCount());
                    data.setOptionTopPolled(true);
                }
                data.setPollCount(data.getPollCount() + 1);
                data.setIsPolled(true);
                itemView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                }, 500);
            }
        }
        return true;
    }

    private void updateUI() {
        txtBarPollCount.setText(String.format(itemView.getContext()
                .getString(R.string.wall_item_bar_vote_count), data.getPollCount()));
        setUpOptionArea(true);
    }

}