package com.heaton.funnyvote.ui.votedetail;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.heaton.funnyvote.FirstTimePref;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.retrofit.Server;
import com.heaton.funnyvote.ui.HidingScrollListener;
import com.heaton.funnyvote.ui.ShareDialogActivity;
import com.heaton.funnyvote.ui.about.AboutFragment;
import com.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.heaton.funnyvote.ui.personal.PersonalActivity;
import com.heaton.funnyvote.utils.Util;

import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 2016/8/21.
 */

public class VoteDetailContentActivity extends AppCompatActivity implements VoteDetailContract.View {

    private static final int TITLE_EXTEND_MAX_LINE = 5;
    private static final String TAG = VoteDetailContentActivity.class.getSimpleName();
    public static boolean ENABLE_ADMOB = true;
    @BindView(R.id.imgAuthorIcon)
    ImageView imgAuthorIcon;
    @BindView(R.id.txtAuthorName)
    TextView txtAuthorName;
    @BindView(R.id.txtPubTime)
    TextView txtPubTime;
    @BindView(R.id.txtTitle)
    TextView txtTitle;
    @BindView(R.id.imgMain)
    ImageView imgMain;
    @BindView(R.id.ryOptionArea)
    RecyclerView ryOptionArea;
    @BindView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.circleLoad)
    CircleProgressView circleLoad;
    @BindView(R.id.fabTop)
    FloatingActionButton fabTop;
    @BindView(R.id.fabPreResult)
    FloatingActionButton fabPreResult;
    @BindView(R.id.famOther)
    FloatingActionsMenu famOther;
    @BindView(R.id.txtBarShare)
    TextView txtBarShare;
    @BindView(R.id.txtBarPollCount)
    TextView txtBarPollCount;
    @BindView(R.id.imgBarFavorite)
    ImageView imgBarFavorite;
    @BindView(R.id.fabOptionSort)
    FloatingActionButton fabOptionSort;
    @BindView(R.id.imgBarPollCount)
    ImageView imgBarPollCount;
    @BindView(R.id.relBarPollCount)
    RelativeLayout relBarPollCount;
    @BindView(R.id.imgBarShare)
    ImageView imgBarShare;
    @BindView(R.id.relBarShare)
    RelativeLayout relBarShare;
    @BindView(R.id.txtBarFavorite)
    TextView txtBarFavorite;
    @BindView(R.id.relBarFavorite)
    RelativeLayout relBarFavorite;
    @BindView(R.id.appBarMain)
    AppBarLayout appBarMain;
    @BindView(R.id.imgTitleExtend)
    ImageView imgTitleExtend;
    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.imgLock)
    ImageView imgLock;
    private Menu menu;
    private SearchView searchView;
    private AlertDialog newOptionPasswordDialog;
    private AlertDialog pollPasswordDialog;
    private OptionItemAdapter optionItemAdapter;
    private VoteData data;
    private Activity context;
    private int sortType = 0;
    private Tracker tracker;
    private ShowcaseView showcaseView;

    private VoteDetailContract.Presenter presenter;
    private OptionItemListener optionItemListener;

    public static void sendShareIntent(Context context, VoteData data) {
        Intent shareDialog = new Intent(context, ShareDialogActivity.class);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_TITLE, data.getTitle());
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IMG_URL, data.getVoteImage());
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, Server.WEB_URL + data.getVoteCode());
        shareDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(shareDialog);
    }

    public static void sendPersonalDetailIntent(Context context, VoteData data) {
        Intent personalActivity = new Intent(context, PersonalActivity.class);
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_CODE, data.getAuthorCode());
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_CODE_TYPE, data.getAuthorCodeType());
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_NAME, data.getAuthorName());
        personalActivity.putExtra(PersonalActivity.EXTRA_PERSONAL_ICON, data.getAuthorIcon());
        personalActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(personalActivity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);
        ButterKnife.bind(this);


        FunnyVoteApplication application = (FunnyVoteApplication) getApplication();
        tracker = application.getDefaultTracker();
        context = this;
        data = new VoteData();

        mainToolbar.setTitle(getString(R.string.vote_detail_title));
        mainToolbar.setTitleTextColor(Color.WHITE);
        mainToolbar.setElevation(10);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String voteCode = "";
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            voteCode = intent.getData().getLastPathSegment();
            if (TextUtils.isEmpty(voteCode)) {
                AboutFragment.sendShareAppIntent(getApplicationContext());
                finish();
            } else {
                Log.d(TAG, "Link:" + intent.getData().toString()
                        + ",vote code:" + intent.getData().getLastPathSegment());
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory("VOTE_DETAIL")
                        .setAction(AnalyzticsTag.ACTION_LINK_VOTE)
                        .setLabel(voteCode)
                        .build());
            }
        } else {
            voteCode = intent.getExtras().getString(VHVoteWallItem.BUNDLE_KEY_VOTE_CODE);
            Log.d(TAG, "Start activity vote code:" + voteCode);
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory("VOTE_DETAIL")
                    .setAction(AnalyzticsTag.ACTION_ENTER_VOTE)
                    .setLabel(voteCode)
                    .build());
        }


        data.setVoteCode(voteCode);
        optionItemListener = new OptionItemListener() {
            @Override
            public void onOptionExpand(String optionCode) {
                presenter.resetOptionExpandStatus(optionCode);
            }

            @Override
            public void onOptionQuickPoll(long optionId, String optionCode) {
                presenter.resetOptionChoiceStatus(optionId, optionCode);
                presenter.pollVote(null);
            }

            @Override
            public void onOptionChoice(long optionId, String optionCode) {
                presenter.resetOptionChoiceStatus(optionId, optionCode);
            }

            @Override
            public void onOptionTextChange(long optionId, String newOptionText) {
                presenter.addNewOptionContentRevise(optionId, newOptionText);
            }

            @Override
            public void onOptionAddNew() {
                presenter.addNewOptionStart();
            }

            @Override
            public void onOptionAddNewCheck(String newOptionText) {
                presenter.addNewOptionCompleted(null, newOptionText);
            }

            @Override
            public void onOptionRemove(long optionId) {
                presenter.removeOption(optionId);
            }
        };

        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getResources().getColor(R.color.md_amber_50));
        ENABLE_ADMOB = getResources().getBoolean(R.bool.enable_detail_admob);
        presenter = new VoteDetailPresenter(voteCode
                , Injection.provideVoteDataRepository(context)
                , Injection.provideUserRepository(context)
                , this);
        presenter.start();

    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_VOTE_DETAIL);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void setUpSubmit(int optionType) {
        if (menu != null) {
            MenuItem submit = menu.findItem(R.id.menu_submit);
            if (submit != null && optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
                submit.setVisible(false);
            } else {
                submit.setVisible(true);
                Target homeTarget = new Target() {
                    @Override
                    public Point getPoint() {
                        return new ViewTarget(mainToolbar.findViewById(R.id.menu_submit)).getPoint();
                    }
                };
                final SharedPreferences firstTimePref = FirstTimePref.getInstance(getApplicationContext())
                        .getPreferences();

                if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, true)) {
                    showcaseView = new ShowcaseView.Builder(this)
                            .setTarget(homeTarget)
                            .withHoloShowcase()
                            .setStyle(R.style.CustomShowcaseTheme)
                            .setContentTitle(getString(R.string.vote_detail_case_view_title))
                            .setContentText(getString(R.string.vote_detail_case_view_content))
                            .setShowcaseEventListener(new OnShowcaseEventListener() {
                                @Override
                                public void onShowcaseViewHide(ShowcaseView showcaseView) {
                                    firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, false).apply();
                                }

                                @Override
                                public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                                }

                                @Override
                                public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                }

                                @Override
                                public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                }
                            })
                            .build();
                    showcaseView.show();
                }
            }
        }
    }

    @Override
    public void setUpOptionAdapter(VoteData data, int optionType, List<Option> optionList) {
        optionItemAdapter = new OptionItemAdapter(optionType, optionList, data, optionItemListener);
        ryOptionArea.setAdapter(optionItemAdapter);
    }

    @Override
    public void showHintToast(int res) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMultiChoiceToast(int max, int min) {
        Toast.makeText(context, String.format(getString(R.string.vote_detail_dialog_multi_option)
                , min, max), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showMultiChoiceAtLeast(int min) {
        Toast.makeText(this, String.format(getString(R.string.vote_detail_toast_option_at_least_min)
                , min), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showMultiChoiceOverMaxToast(int max) {
        Toast.makeText(context
                , String.format(this.getString(R.string.vote_detail_toast_option_over_max)
                        , max), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshOptions() {
        optionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateChoiceOptions(List<Long> choiceList) {
        optionItemAdapter.setChoiceList(choiceList);
        optionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateExpandOptions(List<String> expandList) {
        optionItemAdapter.setExpandOptionList(expandList);
        optionItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void showShareDialog(VoteData data) {
        sendShareIntent(this, data);
    }

    @Override
    public void showAuthorDetail(VoteData data) {
        sendPersonalDetailIntent(this, data);
    }

    @Override
    public void moveToTop() {
        ryOptionArea.smoothScrollToPosition(0);
        appBarMain.setExpanded(true, true);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(AnalyzticsTag.ACTION_MOVE_TOP)
                .setLabel(data.getVoteCode()).build());
    }

    @Override
    public void updateSearchView(List<Option> searchList, boolean isSearchMode) {
        optionItemAdapter.setSearchMode(isSearchMode);
        optionItemAdapter.setSearchList(searchList);
        optionItemAdapter.notifyDataSetChanged();
        if (isSearchMode) {
            appBarMain.setExpanded(false);
        } else {
            appBarMain.setExpanded(true);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @OnClick(R.id.imgTitleExtend)
    public void onTitleExtendClick() {
        presenter.showTitleDetail();
    }

    @Override
    public void showTitleDetailDialog(VoteData data) {
        final Dialog titleDetail = new Dialog(VoteDetailContentActivity.this
                , android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        titleDetail.requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        titleDetail.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        View content = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_title_detail, null);
        TextView txtTitleDetail = (TextView) content.findViewById(R.id.txtTitleDetail);
        TextView txtAuthorName = (TextView) content.findViewById(R.id.txtAuthorName);
        TextView txtPubTime = (TextView) content.findViewById(R.id.txtPubTime);
        ImageView imgAuthorIcon = (ImageView) content.findViewById(R.id.imgAuthorIcon);
        txtAuthorName.setText(data.getAuthorName());
        txtPubTime.setText(Util.getDate(data.getStartTime(), "yyyy/MM/dd hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "yyyy/MM/dd hh:mm"));
        if (data.getAuthorIcon() == null || data.getAuthorIcon().isEmpty()) {
            if (data.getAuthorName() != null && !data.getAuthorName().isEmpty()) {
                TextDrawable drawable = TextDrawable.builder().beginConfig()
                        .width((int) getResources().getDimension(R.dimen.vote_image_author_size))
                        .height((int) getResources().getDimension(R.dimen.vote_image_author_size)).endConfig()
                        .buildRound(data.getAuthorName().substring(0, 1), R.color.primary_light);
                imgAuthorIcon.setImageDrawable(drawable);
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp);
            }
        } else {
            Glide.with(this)
                    .load(data.getAuthorIcon())
                    .override((int) getResources().getDimension(R.dimen.vote_image_author_size)
                            , (int) getResources().getDimension(R.dimen.vote_image_author_size))
                    .fitCenter()
                    .crossFade()
                    .into(imgAuthorIcon);
        }
        ImageView imgCross = (ImageView) content.findViewById(R.id.imgCross);
        imgCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                titleDetail.dismiss();
            }
        });
        txtTitleDetail.setText(data.getTitle());
        titleDetail.setContentView(content);
        titleDetail.show();
    }

    @Override
    public void showCaseView() {
        final SharedPreferences firstTimePref = FirstTimePref.getInstance(getApplicationContext())
                .getPreferences();

        if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, true)) {
            Target homeTarget = new Target() {
                @Override
                public Point getPoint() {
                    return new ViewTarget(mainToolbar.findViewById(R.id.menu_submit)).getPoint();
                }
            };
            showcaseView = new ShowcaseView.Builder(this)
                    .setTarget(homeTarget)
                    .withHoloShowcase()
                    .setStyle(R.style.CustomShowcaseTheme)
                    .setContentTitle(getString(R.string.vote_detail_case_view_title))
                    .setContentText(getString(R.string.vote_detail_case_view_content))
                    .setShowcaseEventListener(new OnShowcaseEventListener() {
                        @Override
                        public void onShowcaseViewHide(ShowcaseView showcaseView) {
                            firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_ENTER_UNPOLL_VOTE, false).apply();
                        }

                        @Override
                        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewShow(ShowcaseView showcaseView) {

                        }

                        @Override
                        public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                        }
                    })
                    .build();
            showcaseView.show();
        }
    }

    @Override
    public void updateFavoriteView(boolean isFavorite) {
        imgBarFavorite.setImageResource(isFavorite ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(data.getIsFavorite() ? AnalyzticsTag.ACTION_ADD_FAVORITE
                        : AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                .setLabel(data.getVoteCode())
                .build());
    }

    @Override
    public void setUpAdMob(User user) {
        if (ENABLE_ADMOB) {
            AdRequest adRequest = new AdRequest.Builder()
                    .setGender(user != null && User.GENDER_MALE.equals(user.getGender()) ?
                            AdRequest.GENDER_MALE : AdRequest.GENDER_FEMALE)
                    .build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUpViews(VoteData data, int optionType) {
        txtAuthorName.setText(data.getAuthorName());
        txtPubTime.setText(Util.getDate(data.getStartTime(), "yyyy/MM/dd hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "yyyy/MM/dd hh:mm")
                + (data.getEndTime() < System.currentTimeMillis() ? "  " + getString(R.string.wall_item_vote_end) : ""));
        txtTitle.setText(data.getTitle());

        if (data.getAuthorIcon() == null || data.getAuthorIcon().isEmpty()) {
            if (data.getAuthorName() != null && !data.getAuthorName().isEmpty()) {
                TextDrawable drawable = TextDrawable.builder().beginConfig()
                        .width((int) getResources().getDimension(R.dimen.vote_image_author_size))
                        .height((int) getResources().getDimension(R.dimen.vote_image_author_size)).endConfig()
                        .buildRound(data.getAuthorName().substring(0, 1), R.color.primary_light);
                imgAuthorIcon.setImageDrawable(drawable);
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp);
            }
        } else {
            Glide.with(this)
                    .load(data.getAuthorIcon())
                    .override((int) getResources().getDimension(R.dimen.vote_image_author_size)
                            , (int) getResources().getDimension(R.dimen.vote_image_author_size))
                    .fitCenter()
                    .crossFade()
                    .into(imgAuthorIcon);
        }
        if (VoteData.SECURITY_PUBLIC.equals(data.getSecurity())) {
            imgLock.setVisibility(View.INVISIBLE);
        } else {
            imgLock.setVisibility(View.VISIBLE);
        }
        txtBarPollCount.setText(String.format(this
                .getString(R.string.wall_item_bar_vote_count), data.getPollCount()));

        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);

        Glide.with(this)
                .load(data.getVoteImage())
                .override((int) getResources().getDimension(R.dimen.vote_detail_image_width)
                        , (int) getResources().getDimension(R.dimen.vote_detail_image_high))
                .dontAnimate()
                .crossFade()
                .into(imgMain);

        if (txtTitle.getLineCount() >= TITLE_EXTEND_MAX_LINE) {
            imgTitleExtend.setVisibility(View.VISIBLE);
        } else {
            imgTitleExtend.setVisibility(View.GONE);
        }

        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT || !data.getIsCanPreviewResult()) {
            fabPreResult.setVisibility(View.GONE);
        } else {
            fabPreResult.setVisibility(View.VISIBLE);
        }

        ryOptionArea.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                famOther.collapse();
                famOther.animate().translationY(
                        famOther.getHeight())
                        .setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void onShow() {
                this.resetScrollDistance();
                famOther.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }
        });
    }

    @OnClick(R.id.relBarFavorite)
    public void onBarFavoriteClick() {
        presenter.favoriteVote();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(data.getIsFavorite() ? AnalyzticsTag.ACTION_ADD_FAVORITE
                        : AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                .setLabel(data.getVoteCode())
                .build());
    }

    @OnClick(R.id.relBarShare)
    public void onBarShareClick() {
        presenter.IntentToShareDialog();
    }

    @OnClick({R.id.imgAuthorIcon, R.id.txtAuthorName})
    public void onAuthorClick() {
        presenter.IntentToAuthorDetail();
    }

    @OnClick({R.id.fabOptionSort, R.id.fabTop, R.id.fabPreResult})
    public void onFabClick(FloatingActionButton button) {
        int id = button.getId();
        if (id == R.id.fabTop) {
            moveToTop();
        } else if (id == R.id.fabPreResult) {
            presenter.changeOptionType();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_CHANGE_MODE)
                    .setLabel(data.getVoteCode()).build());
        } else if (id == R.id.fabOptionSort) {
            presenter.CheckSortOptionType();
        }
        famOther.collapse();
    }

    @Override
    public void showResultOption(int optionType) {
        int currentFirstVisibleItem = ((LinearLayoutManager) ryOptionArea.getLayoutManager())
                .findFirstVisibleItemPosition();
        fabPreResult.setTitle(getString(R.string.vote_detail_fab_return_poll));
        optionItemAdapter.setOptionType(optionType);
        ryOptionArea.setAdapter(this.optionItemAdapter);
        ryOptionArea.scrollToPosition(currentFirstVisibleItem);
        // Todo: set animation to make transfer funny and smooth.
    }

    @Override
    public void showUnPollOption(int optionType) {
        int currentFirstVisibleItem = ((LinearLayoutManager) ryOptionArea.getLayoutManager())
                .findFirstVisibleItemPosition();
        fabPreResult.setTitle(getString(R.string.vote_detail_fab_pre_result));
        optionItemAdapter.setOptionType(optionType);
        ryOptionArea.setAdapter(this.optionItemAdapter);
        ryOptionArea.scrollToPosition(currentFirstVisibleItem);
        // Todo: set animation to make transfer funny and smooth.
    }

    @Override
    public void showLoadingCircle() {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.spin();
    }

    @Override
    public void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
    }

    @Override
    public void showSortOptionDialog(VoteData data) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] allType = null;
        if (data.getIsCanPreviewResult()) {
            allType = new String[]{getString(R.string.vote_detail_dialog_sort_default)
                    , getString(R.string.vote_detail_dialog_sort_alphabet)
                    , getString(R.string.vote_detail_dialog_sort_poll)};
        } else {
            allType = new String[]{getString(R.string.vote_detail_dialog_sort_default)
                    , getString(R.string.vote_detail_dialog_sort_alphabet)};
        }
        builder.setSingleChoiceItems(allType, this.sortType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sortType = which;
            }
        });
        final String[] finalAllType = allType;
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        presenter.sortOptions(sortType);
                        //sortOptions();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                                .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                                .setLabel(finalAllType[sortType]).build());
                    }
                });
        builder.setTitle(getString(R.string.vote_detail_dialog_sort_option));
        builder.show();
    }

    @Override
    public void showPollPasswordDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(getApplicationContext().getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(getApplicationContext().getString(R.string.vote_detail_dialog_password_title));
        pollPasswordDialog = builder.create();

        pollPasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final EditText password = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.edtEnterPassword);
                Button ok = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "choice:" + optionItemAdapter.getChoiceCodeList().size() + " vc:" + data.getVoteCode()
                                + " pw input:" + password.getText().toString());
                        presenter.pollVote(password.getText().toString());
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                                .setAction(AnalyzticsTag.ACTION_POLL_VOTE)
                                .setLabel(data.getVoteCode())
                                .build());
                    }
                });
            }
        });
        pollPasswordDialog.show();
    }

    @Override
    public void hidePollPasswordDialog() {
        if (pollPasswordDialog != null && pollPasswordDialog.isShowing()) {
            pollPasswordDialog.dismiss();
        }
    }

    @Override
    public boolean isPasswordDialogShowing() {
        if (newOptionPasswordDialog != null && newOptionPasswordDialog.isShowing()) {
            return true;
        } else if (pollPasswordDialog != null && pollPasswordDialog.isShowing()) {
            return true;
        }
        return false;
    }

    @Override
    public void shakePollPasswordDialog() {
        if (pollPasswordDialog != null && pollPasswordDialog.isShowing()) {
            final EditText password = (EditText) pollPasswordDialog.findViewById(R.id.edtEnterPassword);
            password.selectAll();
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.edittext_shake);
            password.startAnimation(shake);
        }
    }

    @Override
    public void shakeAddNewOptionPasswordDialog() {
        if (newOptionPasswordDialog != null && newOptionPasswordDialog.isShowing()) {
            final EditText password = (EditText) newOptionPasswordDialog.findViewById(R.id.edtEnterPassword);
            password.selectAll();
            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.edittext_shake);
            password.startAnimation(shake);
        }
    }

    @Override
    public void showAddNewOptionPasswordDialog(final String newOptionText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(getApplicationContext().getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(getApplicationContext().getString(R.string.vote_detail_dialog_password_title) + "test");
        newOptionPasswordDialog = builder.create();

        newOptionPasswordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final EditText password = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.edtEnterPassword);
                Button ok = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "New Option Text:" + newOptionText + " vc:" + data.getVoteCode()
                                + " pw input:" + password.getText().toString());
                        presenter.addNewOptionCompleted(password.getText().toString(), newOptionText);
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                                .setAction(AnalyzticsTag.ACTION_ADD_NEW_OPTION)
                                .setLabel(data.getVoteCode()).build());
                    }
                });
            }
        });
        newOptionPasswordDialog.show();
    }

    @Override
    public void hideAddNewOptionPasswordDialog() {
        if (newOptionPasswordDialog != null && newOptionPasswordDialog.isShowing()) {
            newOptionPasswordDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_content_detail, menu);
        this.menu = menu;
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setIconifiedByDefault(true);
        searchView.setQueryHint(getString(R.string.vote_detail_menu_search_hint));
        searchView.setOnQueryTextListener(queryListener);
        return super.onCreateOptionsMenu(menu);
    }

    final private SearchView.OnQueryTextListener queryListener =
            new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextChange(String newText) {
                    presenter.searchOption(newText);
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                            .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                            .setLabel(newText).build());
                    return false;
                }

                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_submit) {
            //Log.d(TAG, "SHOWCASEVIEW:" + (showcaseView == null) + ", showing:" + showcaseView.isShowing());
            if (showcaseView != null && showcaseView.isShowing()) {
                showcaseView.hide();
            } else {
                presenter.pollVote(null);
            }
            return true;
        } else if (id == R.id.menu_info) {
            presenter.showVoteInfo();
        } else if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (famOther.isExpanded()) {
            famOther.collapse();
        } else {
            if (optionItemAdapter == null || optionItemAdapter.getChoiceList().size() > 0) {
                showExitCheckDialog();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void showExitCheckDialog() {
        final AlertDialog.Builder exitDialog = new AlertDialog.Builder(VoteDetailContentActivity.this);
        exitDialog.setTitle(R.string.vote_detail_dialog_exit_title);
        exitDialog.setMessage(R.string.vote_detail_dialog_exit_message);
        exitDialog.setNegativeButton(R.string.vote_detail_dialog_exit_button_leave
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VoteDetailContentActivity.super.onBackPressed();
                    }
                });
        exitDialog.setPositiveButton(R.string.vote_detail_dialog_exit_button_keep
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        exitDialog.show();
    }

    @Override
    public void showVoteInfoDialog(VoteData data) {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_vote_detail_info, null);
        TextView option = ButterKnife.findById(content, R.id.txtOptionInfo);
        TextView time = ButterKnife.findById(content, R.id.txtTime);
        TextView security = ButterKnife.findById(content, R.id.txtSecurity);
        if (!data.isMultiChoice()) {
            option.setText(getString(R.string.vote_detail_dialog_single_option));
        } else {
            String multi = String.format(getString(R.string.vote_detail_dialog_multi_option)
                    , data.getMinOption(), data.getMaxOption());
            option.setText(multi);
        }
        if (data.getIsUserCanAddOption()) {
            option.setText(option.getText() + "\n\n" + getString(R.string.vote_detail_dialog_can_add_option));
        } else {
            option.setText(option.getText() + "\n\n" + getString(R.string.vote_detail_dialog_can_not_add_option));
        }
        time.setText(Util.getDate(data.getStartTime(), "yyyy/MM/dd hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "yyyy/MM/dd hh:mm"));
        security.setText(VoteData.getSecurityString(getApplicationContext(), data.getSecurity()));
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.vote_detail_dialog_title_info));
        dialog.setView(content);
        dialog.setPositiveButton(getString(R.string.vote_detail_dialog_done),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    @Override
    public void setPresenter(VoteDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public interface OptionItemListener {
        void onOptionExpand(String optionCode);

        void onOptionQuickPoll(long optionId, String optionCode);

        void onOptionChoice(long optionId, String optionCode);

        void onOptionTextChange(long optionId, String newOptionText);

        void onOptionAddNew();

        void onOptionAddNewCheck(String newOptionText);

        void onOptionRemove(long optionId);
    }
}
