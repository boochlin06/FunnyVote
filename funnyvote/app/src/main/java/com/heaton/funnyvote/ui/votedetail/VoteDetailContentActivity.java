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
import com.heaton.funnyvote.Util;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.VoteData.VoteDataManager;
import com.heaton.funnyvote.data.user.UserManager;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.eventbus.EventBusManager;
import com.heaton.funnyvote.retrofit.Server;
import com.heaton.funnyvote.ui.HidingScrollListener;
import com.heaton.funnyvote.ui.ShareDialogActivity;
import com.heaton.funnyvote.ui.about.AboutFragment;
import com.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.heaton.funnyvote.ui.personal.PersonalActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by heaton on 2016/8/21.
 */

public class VoteDetailContentActivity extends AppCompatActivity {

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
    private List<Option> optionList;
    private int optionType = OptionItemAdapter.OPTION_UNPOLL;
    private boolean isMultiChoice = false;
    private boolean isUserPreResult = false;
    private boolean isUserOnAddNewOption = false;
    // all new option id is negative auto increment.
    private long newOptionIdAuto = -1;
    private int sortType = 0;
    private User user;
    private Tracker tracker;
    private ShowcaseView showcaseView;

    private VoteDataManager voteDataManager;
    private UserManager userManager;
    private UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            VoteDetailContentActivity.this.user = user;
            setUpAdmob();
            voteDataManager.getVote(data.getVoteCode(), user);
        }

        @Override
        public void onFailure() {
            voteDataManager.getVote(data.getVoteCode(), user);
        }
    };

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
        optionList = new ArrayList<>();

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

        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getResources().getColor(R.color.md_amber_50));

        showLoadingCircle(getString(R.string.vote_detail_circle_loading));

        ENABLE_ADMOB = getResources().getBoolean(R.bool.enable_detail_admob);

        checkCurrentOptionType();
        setUpViews();
        setUpSubmit();
        setUpOptionAdapter(new ArrayList<Option>());
        voteDataManager = VoteDataManager.getInstance(getApplicationContext());
        userManager = UserManager.getInstance(getApplicationContext());
        userManager.getUser(getUserCallback, false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker.setScreenName(AnalyzticsTag.SCREEN_VOTE_DETAIL);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setUpViews() {
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

    private void setUpSubmit() {
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

    private void setUpOptionAdapter(List<Option> optionList) {
        optionItemAdapter = new OptionItemAdapter(optionType, optionList, data);
        ryOptionArea.setAdapter(optionItemAdapter);
    }

    private void setUpAdmob() {
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

    private void checkCurrentOptionType() {
        if (data.getEndTime() < System.currentTimeMillis() || data.getIsPolled() || isUserPreResult) {
            optionType = OptionItemAdapter.OPTION_SHOW_RESULT;
        } else {
            optionType = OptionItemAdapter.OPTION_UNPOLL;
        }
        this.isMultiChoice = data.isMultiChoice();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.imgTitleExtend)
    public void onTitleExtendClick() {
        showTitleDetailDialog();
    }

    private void showTitleDetailDialog() {
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


    @OnClick(R.id.relBarFavorite)
    public void onBarFavoriteClick() {
        data.setIsFavorite(!data.getIsFavorite());
        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);
        voteDataManager.favoriteVote(data.getVoteCode(), data.getIsFavorite(), user);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                .setAction(data.getIsFavorite() ? AnalyzticsTag.ACTION_ADD_FAVORITE
                        : AnalyzticsTag.ACTION_REMOVE_FAVORITE)
                .setLabel(data.getVoteCode())
                .build());
    }

    @OnClick(R.id.relBarShare)
    public void onBarShareClick() {
        sendShareIntent(this, data);
    }

    @OnClick({R.id.imgAuthorIcon, R.id.txtAuthorName})
    public void onAuthorClick() {
        sendPersonalDetailIntent(this, data);
    }

    @OnClick({R.id.fabOptionSort, R.id.fabTop, R.id.fabPreResult})
    public void onFabClick(FloatingActionButton button) {
        int id = button.getId();
        if (id == R.id.fabTop) {
            ryOptionArea.smoothScrollToPosition(0);
            appBarMain.setExpanded(true, true);
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_MOVE_TOP)
                    .setLabel(data.getVoteCode()).build());
        } else if (id == R.id.fabPreResult) {
            isUserPreResult = !isUserPreResult;
            if (isUserPreResult) {
                showResultOption();
            } else {
                showUnpollOption();
            }
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                    .setAction(AnalyzticsTag.ACTION_CHANGE_MODE)
                    .setLabel(data.getVoteCode()).build());
            setUpSubmit();
        } else if (id == R.id.fabOptionSort) {
            showSortOptionDialog();
        }
        famOther.collapse();
    }

    private void showResultOption() {
        int currentFirstVisibleItem = ((LinearLayoutManager) ryOptionArea.getLayoutManager())
                .findFirstVisibleItemPosition();
        fabPreResult.setTitle(getString(R.string.vote_detail_fab_return_poll));
        optionType = OptionItemAdapter.OPTION_SHOW_RESULT;
        optionItemAdapter.setOptionType(optionType);
        ryOptionArea.setAdapter(this.optionItemAdapter);
        ryOptionArea.scrollToPosition(currentFirstVisibleItem);
        // Todo: set animation to make transfer funny and smooth.
    }

    private void showUnpollOption() {
        int currentFirstVisibleItem = ((LinearLayoutManager) ryOptionArea.getLayoutManager())
                .findFirstVisibleItemPosition();
        fabPreResult.setTitle(getString(R.string.vote_detail_fab_pre_result));
        optionType = OptionItemAdapter.OPTION_UNPOLL;
        optionItemAdapter.setOptionType(optionType);
        ryOptionArea.setAdapter(this.optionItemAdapter);
        ryOptionArea.scrollToPosition(currentFirstVisibleItem);
        // Todo: set animation to make transfer funny and smooth.
    }

    private void showLoadingCircle(String content) {
        circleLoad.setVisibility(View.VISIBLE);
        circleLoad.setText(content);
        circleLoad.spin();
    }

    private void hideLoadingCircle() {
        circleLoad.stopSpinning();
        circleLoad.setVisibility(View.GONE);
    }

    private void showSortOptionDialog() {
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
                        sortOptions();
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                                .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                                .setLabel(finalAllType[sortType]).build());
                    }
                });
        builder.setTitle(getString(R.string.vote_detail_dialog_sort_option));
        builder.show();
    }

    private void showPollPasswordDialog() {
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
                        voteDataManager.pollVote(data.getVoteCode(), password.getText().toString()
                                , optionItemAdapter.getChoiceCodeList(), user);
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

    private void showAddNewOptionPasswordDialog(final String newOptionText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(getApplicationContext().getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(getApplicationContext().getString(R.string.vote_detail_dialog_password_title));
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
                        List<String> newOptions = new ArrayList<>();
                        newOptions.add(newOptionText);
                        voteDataManager.addNewOption(data.getVoteCode(), password.getText().toString(), newOptions, user);
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

    private void sortOptions() {
        Comparator<Option> comparator = null;
        switch (sortType) {
            case 0:
                comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option option1, Option option2) {
                        // TODO:Add user add new option case id compare.
                        if (option1.getId() < 0 || option2.getId() < 0) {
                            return ((Long) (Math.abs(option1.getId()) + 100000))
                                    .compareTo(Math.abs(option2.getId()) + 100000);
                        } else {
                            return option1.getId().compareTo(option2.getId());
                        }
                    }
                };
                break;
            case 1:
                comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option option1, Option option2) {
                        return option1.getTitle().compareTo(option2.getTitle());
                    }
                };
                break;
            case 2:
                comparator = new Comparator<Option>() {
                    @Override
                    public int compare(Option option1, Option option2) {
                        return option2.getCount().compareTo(option1.getCount());
                    }
                };
                break;
        }
        Collections.sort(optionItemAdapter.getCurrentList(), comparator);
        optionItemAdapter.notifyDataSetChanged();
        if (!optionItemAdapter.isSearchMode()) {
            Collections.sort(optionList, comparator);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_content_detail, menu);
        this.menu = menu;
        setUpSubmit();
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

                    if (newText.length() > 0) {
                        List<Option> searchList = new ArrayList<>();
                        for (int i = 0; i < optionList.size(); i++) {
                            if (optionList.get(i).getTitle().contains(newText)) {
                                searchList.add(optionList.get(i));
                            }
                        }
                        tracker.send(new HitBuilders.EventBuilder()
                                .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                                .setAction(AnalyzticsTag.ACTION_SEARCH_OPTION)
                                .setLabel(newText).build());
                        optionItemAdapter.setSearchList(searchList);
                        appBarMain.setExpanded(false);
                        return false;
                    } else {
                        optionItemAdapter.setSearchMode(false);
                        optionItemAdapter.notifyDataSetChanged();
                        appBarMain.setExpanded(true);
                    }

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
            if (showcaseView != null && showcaseView.isShowing()) {
                showcaseView.hide();
            } else {
                submitPoll();
            }
            return true;
        } else if (id == R.id.menu_info) {
            showVoteInfoDialog();
        } else if (id == android.R.id.home) {
            finish();
        }
        return true;
    }

    private void submitPoll() {
        if (optionItemAdapter.getChoiceList().size() < data.getMinOption()) {
            Toast.makeText(this, String.format(getString(R.string.vote_detail_toast_option_at_least_min)
                    , data.getMinOption()), Toast.LENGTH_LONG).show();
        } else if (optionItemAdapter.getChoiceCodeList().size() > data.getMaxOption()) {
            Toast.makeText(this, String.format(getString(R.string.vote_detail_toast_option_over_max)
                    , data.getMaxOption()), Toast.LENGTH_LONG).show();
        } else if (isUserOnAddNewOption) {
            Toast.makeText(this, R.string.vote_detail_toast_fill_new_option, Toast.LENGTH_LONG).show();
        } else {
            if (data.getIsNeedPassword()) {
                showPollPasswordDialog();
            } else {
                showLoadingCircle(getString(R.string.vote_detail_circle_updating));
                Log.d(TAG, "choice:" + optionItemAdapter.getChoiceCodeList().size()
                        + " vc:" + data.getVoteCode() + " user:" + user.getUserCode() + "  type:" + user.getType());
                voteDataManager.pollVote(data.getVoteCode(), null, optionItemAdapter.getChoiceCodeList(), user);
                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                        .setAction(AnalyzticsTag.ACTION_POLL_VOTE)
                        .setLabel(data.getVoteCode())
                        .build());
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (famOther.isExpanded()) {
            famOther.collapse();
        } else {
            if (optionItemAdapter.getChoiceList().size() > 0) {
                showExitCheckDialog();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void showExitCheckDialog() {
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

    private void showVoteInfoDialog() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_vote_detail_info, null);
        TextView option = ButterKnife.findById(content, R.id.txtOptionInfo);
        TextView time = ButterKnife.findById(content, R.id.txtTime);
        TextView security = ButterKnife.findById(content, R.id.txtSecurity);
        if (!isMultiChoice) {
            option.setText(getString(R.string.vote_detail_dialog_single_option));
        } else {
            String multi = String.format(getString(R.string.vote_detail_dialog_multi_option)
                    , data.getMinOption(), data.getMaxOption());
            option.setText(multi);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionChoice(EventBusManager.OptionChoiceEvent event) {
        long id = event.Id;
        if (event.message.equals(EventBusManager.OptionChoiceEvent.OPTION_CHOICED)) {
            Log.d(TAG, "onOptionChoice message:" + event.message + " id:" + id + " code:" + event.code);

            if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
                return;
            }
            if (!isMultiChoice) {
                optionItemAdapter.getChoiceList().clear();
                optionItemAdapter.getChoiceList().add(id);
                optionItemAdapter.getChoiceCodeList().clear();
                optionItemAdapter.getChoiceCodeList().add(event.code);
                optionItemAdapter.notifyDataSetChanged();
            } else {
                if (optionItemAdapter.getChoiceList().contains(id)) {
                    optionItemAdapter.getChoiceList().remove(optionItemAdapter.getChoiceList()
                            .indexOf(id));
                    optionItemAdapter.getChoiceCodeList().remove(event.code);
                    optionItemAdapter.notifyDataSetChanged();
                } else {
                    if (optionItemAdapter.getChoiceList().size() < data.getMaxOption()) {
                        optionItemAdapter.getChoiceList().add(id);
                        optionItemAdapter.getChoiceCodeList().add(event.code);
                        optionItemAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this
                                , String.format(this.getString(R.string.vote_detail_toast_option_over_max)
                                        , data.getMaxOption()), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (event.message.equals(EventBusManager.OptionChoiceEvent.OPTION_EXPAND)) {
            if (optionItemAdapter.getExpandOptionlist().contains(event.code)) {
                optionItemAdapter.getExpandOptionlist().remove(optionItemAdapter.getExpandOptionlist()
                        .indexOf(event.code));
            } else {
                optionItemAdapter.getExpandOptionlist().add(event.code);
            }
        } else if (event.message.equals(EventBusManager.OptionChoiceEvent.OPTION_QUICK_POLL)) {
            if (!isMultiChoice) {
                optionItemAdapter.getChoiceList().clear();
                optionItemAdapter.getChoiceList().add(id);
                optionItemAdapter.getChoiceCodeList().clear();
                optionItemAdapter.getChoiceCodeList().add(event.code);
                optionItemAdapter.notifyDataSetChanged();
                submitPoll();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionControl(EventBusManager.OptionControlEvent event) {
        long id = event.Id;
        if (event.message.equals(EventBusManager.OptionControlEvent.OPTION_ADD)) {
            if (isUserOnAddNewOption) {
                Toast.makeText(getApplicationContext(), R.string.vote_detail_toast_confirm_new_option, Toast.LENGTH_SHORT).show();
            } else {
                isUserOnAddNewOption = true;
                Option option = new Option();
                option.setCount(0);
                option.setId(newOptionIdAuto--);
                option.setCode("add" + newOptionIdAuto);
                optionList.add(option);
                optionItemAdapter.notifyDataSetChanged();
            }
        } else if (event.message.equals(EventBusManager.OptionControlEvent.OPTION_REMOVE)) {
            isUserOnAddNewOption = false;
            int removePosition = -1;
            for (int i = 0; i < optionList.size(); i++) {
                if (optionList.get(i).getId() == id) {
                    removePosition = i;
                    break;
                }
            }
            if (removePosition >= 0) {
                optionList.remove(removePosition);
                optionItemAdapter.notifyDataSetChanged();
            }
        } else if (event.message.equals(EventBusManager.OptionControlEvent.OPTION_INPUT_TEXT)) {
            int targetPosition = -1;
            for (int i = 0; i < optionList.size(); i++) {
                if (optionList.get(i).getId() == id) {
                    targetPosition = i;
                    break;
                }
            }
            if (targetPosition >= 0) {
                optionList.get(targetPosition).setTitle(event.inputText);
            }
        } else if (event.message.equals(EventBusManager.OptionControlEvent.OPTION_ADD_CHECK)) {
            if (event.inputText != null && !TextUtils.isEmpty(event.inputText)) {
                if (data.getIsNeedPassword()) {
                    showAddNewOptionPasswordDialog(event.inputText);
                } else {
                    showLoadingCircle(getString(R.string.vote_detail_circle_updating));
                    List<String> newOptions = new ArrayList<>();
                    newOptions.add(event.inputText);
                    voteDataManager.addNewOption(data.getVoteCode(), null, newOptions, user);
                    tracker.send(new HitBuilders.EventBuilder()
                            .setCategory(AnalyzticsTag.CATEGORY_VOTE_DETAIL)
                            .setAction(AnalyzticsTag.ACTION_ADD_NEW_OPTION)
                            .setLabel(data.getVoteCode()).build());
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.vote_detail_toast_fill_new_option
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteService(EventBusManager.RemoteServiceEvent event) {
        if (event.message.equals(EventBusManager.RemoteServiceEvent.GET_VOTE)) {
            this.optionList = event.optionList;
            this.data = event.voteData;
            hideLoadingCircle();
            checkCurrentOptionType();
            setUpViews();
            setUpOptionAdapter(optionList);
            setUpSubmit();
            optionItemAdapter.notifyDataSetChanged();
            if (event.success) {
                if (data.getEndTime() > System.currentTimeMillis() && !data.getIsPolled() && isMultiChoice) {
                    Toast.makeText(context, String.format(getString(R.string.vote_detail_dialog_multi_option)
                            , data.getMinOption(), data.getMaxOption()), Toast.LENGTH_SHORT).show();
                } else if (data.getEndTime() < System.currentTimeMillis()) {
                    if (data.getIsPolled()) {
                        Toast.makeText(context, getString(R.string.vote_detail_toast_vote_end_polled)
                                , Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, getString(R.string.vote_detail_toast_vote_end_not_poll)
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, R.string.create_vote_toast_create_fail, Toast.LENGTH_LONG).show();
            }
        } else if (event.message.equals(EventBusManager.RemoteServiceEvent.POLL_VOTE)) {
            hideLoadingCircle();
            if (event.success && this.data.getVoteCode().equals(event.voteData.getVoteCode())) {
                this.data = event.voteData;
                this.optionList = event.optionList;
                Log.d(TAG, "Poll vote success:" + data.getVoteCode()
                        + " image:" + data.getVoteImage() + " pw:" + data.getIsNeedPassword()
                        + " option size:" + optionList.size());
                if (pollPasswordDialog != null && pollPasswordDialog.isShowing()) {
                    pollPasswordDialog.dismiss();
                }
                checkCurrentOptionType();
                setUpViews();
                setUpOptionAdapter(optionList);
                setUpSubmit();
                optionItemAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new EventBusManager
                        .VoteDataControlEvent(data, EventBusManager.VoteDataControlEvent.VOTE_SYNC_WALL_AND_CONTENT));
            } else {
                if (!event.success && event.errorResponseMessage.equals("error_invalid_password")) {
                    if (pollPasswordDialog != null && pollPasswordDialog.isShowing()) {
                        final EditText password = (EditText) pollPasswordDialog.findViewById(R.id.edtEnterPassword);
                        password.selectAll();
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.edittext_shake);
                        password.startAnimation(shake);
                        Toast.makeText(getApplicationContext(), getString(R.string.vote_detail_dialog_password_toast_retry)
                                , Toast.LENGTH_LONG).show();
                    }
                } else {
                    pollPasswordDialog.dismiss();
                    Toast.makeText(this, R.string.toast_network_connect_error_quick_poll, Toast.LENGTH_LONG).show();
                }
            }
        } else if (event.message.equals(EventBusManager.RemoteServiceEvent.FAVORITE_VOTE)) {
            if (event.voteData.getVoteCode().equals(data.getVoteCode())) {
                if (event.success) {
                    imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                            R.drawable.ic_star_border_24dp);
                    if (data.getIsFavorite()) {
                        Toast.makeText(this, R.string.vote_detail_toast_add_favorite, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.vote_detail_toast_remove_favorite, Toast.LENGTH_SHORT).show();
                    }
                    EventBus.getDefault().post(new EventBusManager
                            .VoteDataControlEvent(data, EventBusManager.VoteDataControlEvent.VOTE_SYNC_WALL_FOR_FAVORITE));
                } else {
                    // fail, reverse to request status
                    data.setIsFavorite(!event.voteData.getIsFavorite());
                    imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                            R.drawable.ic_star_border_24dp);
                    Toast.makeText(this, R.string.toast_network_connect_error, Toast.LENGTH_SHORT).show();
                }
            }
        } else if (event.message.equals(EventBusManager.RemoteServiceEvent.ADD_NEW_OPTION)) {
            hideLoadingCircle();
            if (event.success && this.data.getVoteCode().equals(event.voteData.getVoteCode())) {
                this.data = event.voteData;
                this.optionList = event.optionList;
                isUserOnAddNewOption = false;
                Log.d(TAG, "Add new Option vote success:" + data.getVoteCode()
                        + " image:" + data.getVoteImage() + " pw:" + data.getIsNeedPassword()
                        + " option size:" + optionList.size());
                hideLoadingCircle();
                checkCurrentOptionType();
                setUpOptionAdapter(optionList);
                if (newOptionPasswordDialog != null && newOptionPasswordDialog.isShowing()) {
                    newOptionPasswordDialog.dismiss();
                }
                optionItemAdapter.notifyDataSetChanged();
                EventBus.getDefault().post(new EventBusManager
                        .VoteDataControlEvent(data, EventBusManager.VoteDataControlEvent.VOTE_SYNC_WALL_AND_CONTENT));
            } else {
                if (!event.success && event.errorResponseMessage.equals("error_invalid_password")) {
                    if (newOptionPasswordDialog != null && newOptionPasswordDialog.isShowing()) {
                        final EditText password = (EditText) newOptionPasswordDialog.findViewById(R.id.edtEnterPassword);
                        password.selectAll();
                        Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.edittext_shake);
                        password.startAnimation(shake);
                        Toast.makeText(getApplicationContext(), getString(R.string.vote_detail_dialog_password_toast_retry)
                                , Toast.LENGTH_LONG).show();
                    }
                } else {
                    newOptionPasswordDialog.dismiss();
                    Toast.makeText(this, R.string.create_vote_toast_create_fail, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
