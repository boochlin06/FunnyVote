package com.android.heaton.funnyvote.ui.votedetail;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.data.RemoteServiceApi;
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.OptionDao;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.database.VoteDataDao;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.HidingScrollListener;
import com.android.heaton.funnyvote.ui.ShareDialogActivity;
import com.android.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

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

    private static final int TITLE_EXTEND_MAX_LINE = 3;
    private static final String TAG = "VoteDetailContentActivity";
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
    private Menu menu;
    private SearchView searchView;
    private OptionItemAdapter optionItemAdapter;
    private VoteData data;
    private Activity context;
    private List<Option> optionList;
    private int optionType = OptionItemAdapter.OPTION_UNPOLL;
    private boolean isMultiChoice = false;
    private boolean isUserPreResult = false;
    // all new option id is negative auto increment.
    private long newOptionIdAuto = -1;
    private int sortType = 0;
    private User user;

    private UserManager userManager;
    private UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            VoteDetailContentActivity.this.user = user;
            new RemoteServiceApi().getVote(data.getVoteCode(), user);
        }

        @Override
        public void onFailure() {
            new LoadVoteDataFromDBTask(data.getVoteCode()).execute();
        }
    };

    public static void sendShareIntent(Context context, VoteData data) {
        Intent shareDialog = new Intent(context, ShareDialogActivity.class);
        shareDialog.putExtra(ShareDialogActivity.EXTRA_TITLE, data.getTitle());
        shareDialog.putExtra(ShareDialogActivity.EXTRA_IMG_URL, data.getVoteImage());
        shareDialog.putExtra(ShareDialogActivity.EXTRA_VOTE_URL, data.getVoteLink());
        shareDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(shareDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_detail);
        ButterKnife.bind(this);
        data = new VoteData();
        optionList = new ArrayList<>();

        context = this;
        mainToolbar.setTitle(getString(R.string.vote_detail_title));
        mainToolbar.setTitleTextColor(Color.WHITE);
        mainToolbar.setElevation(10);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String voteCode = getIntent().getExtras().getString(VHVoteWallItem.BUNDLE_KEY_VOTE_CODE);
        data.setVoteCode(voteCode);

        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getResources().getColor(R.color.md_amber_50));

        showLoadingCircle(getString(R.string.vote_detail_circle_loading));
        checkCurrentOptionType();
        setUpViews();
        setUpSubmit();
        setUpOptionAdapter(new ArrayList<Option>());
        userManager = UserManager.getInstance(getApplicationContext());
        userManager.getUser(getUserCallback);

    }

    private void setUpViews() {
        txtAuthorName.setText(data.getAuthorName());
        txtPubTime.setText(Util.getDate(data.getStartTime(), "dd/MM hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "dd/MM hh:mm"));
        txtTitle.setText(data.getTitle());
        txtTitle.setMaxLines(TITLE_EXTEND_MAX_LINE);

        if (data.getAuthorIcon() == null || data.getAuthorIcon().isEmpty()) {
            if (data.getAuthorName() != null && !data.getAuthorName().isEmpty()) {
                // TODO: MAYBE TRY PALETTE
                TextDrawable drawable = TextDrawable.builder().beginConfig()
                        .width((int) getResources().getDimension(R.dimen.image_author_size))
                        .height((int) getResources().getDimension(R.dimen.image_author_size)).endConfig()
                        .buildRound(data.getAuthorName().substring(0, 1), R.color.primary_light);
                imgAuthorIcon.setImageDrawable(drawable);
            } else {
                imgAuthorIcon.setImageResource(R.drawable.ic_person_black_24dp);
            }
        } else {
            Glide.with(this)
                    .load(data.getAuthorIcon())
                    .override((int) (Util.convertDpToPixel(getResources().getDimension(R.dimen.image_author_size), this))
                            , (int) (Util.convertDpToPixel(getResources().getDimension(R.dimen.image_author_size), this)))
                    .fitCenter()
                    .crossFade()
                    .into(imgAuthorIcon);
        }

        txtBarPollCount.setText(String.format(this
                .getString(R.string.Wall_item_bar_vote_count), data.getPollCount()));

        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);
        Glide.with(this)
                .load(data.getVoteImage())
                .override((int) (Util.convertDpToPixel(320, this)), (int) (Util.convertDpToPixel(150, this)))
                .fitCenter()
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
            }
        }
    }

    private void setUpOptionAdapter(List<Option> optionList) {
        optionItemAdapter = new OptionItemAdapter(optionType, optionList, data);
        ryOptionArea.setAdapter(optionItemAdapter);
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
        if (txtTitle.getMaxLines() == TITLE_EXTEND_MAX_LINE) {
            txtTitle.setMaxLines(30);
            imgTitleExtend.setImageResource(R.drawable.ic_expand_less_24dp);
        } else if (txtTitle.getMaxLines() == 30) {
            txtTitle.setMaxLines(TITLE_EXTEND_MAX_LINE);
            imgTitleExtend.setImageResource(R.drawable.ic_expand_more_24dp);
        }
    }

    @OnClick(R.id.relBarFavorite)
    public void onBarFavoiteClick() {
        data.setIsFavorite(!data.getIsFavorite());
        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);
        if (data.getIsFavorite()) {
            Toast.makeText(this, R.string.vote_detail_toast_add_favorite, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.vote_detail_toast_remove_favorite, Toast.LENGTH_SHORT).show();
        }
        DataLoader.getInstance(this).getVoteDataDao().insertOrReplace(data);
        EventBus.getDefault().post(new EventBusController
                .VoteDataControlEvent(data, EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_FOR_FAVORITE));
    }

    @OnClick(R.id.relBarShare)
    public void onBarShareClick() {
        sendShareIntent(this, data);
    }

    @OnClick({R.id.fabOptionSort, R.id.fabTop, R.id.fabPreResult})
    public void onFabClick(FloatingActionButton button) {
        int id = button.getId();
        if (id == R.id.fabTop) {
            ryOptionArea.smoothScrollToPosition(0);
            appBarMain.setExpanded(true, true);
        } else if (id == R.id.fabPreResult) {
            isUserPreResult = !isUserPreResult;
            if (isUserPreResult) {
                showResultOption();
            } else {
                showUnpollOption();
            }
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
        builder.setPositiveButton(getString(R.string.vote_detail_dialog_sort_select)
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sortOptions();
                    }
                });
        builder.setTitle(getString(R.string.vote_detail_dialog_sort_option));
        builder.show();
    }

    private void showPasswordDialog() {
        data.password = "test";
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(getApplicationContext().getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(getApplicationContext().getString(R.string.vote_detail_dialog_password_title));
        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final EditText password = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.edtEnterPassword);
                Button ok = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (password.getText().toString().equals(data.password)) {
                            dialog.dismiss();
                            new UpdateVoteDataToServerTask().execute();
                        } else {
                            Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.edittext_shake);
                            password.startAnimation(shake);
                            Toast.makeText(getApplicationContext(), getString(R.string.vote_detail_dialog_password_toast_retry)
                                    , Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        dialog.show();
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
            if (optionItemAdapter.getChoiceList().size() < data.getMinOption()) {
                Toast.makeText(this, String.format(getString(R.string.vote_detail_toast_option_at_least_min)
                        , data.getMinOption()), Toast.LENGTH_LONG).show();
            } else {
                boolean isFailureOption = false;
                for (int i = 0; i < optionList.size(); i++) {
                    String title = optionList.get(i).getTitle();
                    if (title == null || title.isEmpty()) {
                        isFailureOption = true;
                    }
                }
                if (isFailureOption) {
                    Toast.makeText(this, getString(R.string.vote_detail_toast_fill_all_new_option)
                            , Toast.LENGTH_LONG).show();
                } else {
                    if (data.getIsNeedPassword()) {
                        showPasswordDialog();
                    } else {
                        new UpdateVoteDataToServerTask().execute();
                    }
                }
            }
            return true;
        } else if (id == R.id.menu_info) {
            showVoteInfoDialog();
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
            super.onBackPressed();
        }
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
        time.setText(Util.getDate(data.getStartTime(), "dd/MM hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "dd/MM hh:mm"));
        security.setText(data.getSecurity());
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
    public void onOptionChoice(EventBusController.OptionChoiceEvent event) {
        long id = event.Id;
        if (event.message.equals(EventBusController.OptionChoiceEvent.OPTION_CHOICED)) {
            Log.d("test", "onOptionChoice message:" + event.message + " id:" + id);

            if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
                return;
            }
            if (!isMultiChoice) {
                optionItemAdapter.getChoiceList().clear();
                optionItemAdapter.getChoiceList().add(id);
                optionItemAdapter.notifyDataSetChanged();
            } else {
                if (optionItemAdapter.getChoiceList().contains(id)) {
                    optionItemAdapter.getChoiceList().remove(optionItemAdapter.getChoiceList()
                            .indexOf(id));
                    optionItemAdapter.notifyDataSetChanged();
                } else {
                    if (optionItemAdapter.getChoiceList().size() < data.getMaxOption()) {
                        optionItemAdapter.getChoiceList().add(id);
                        optionItemAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this
                                , String.format(this.getString(R.string.vote_detail_toast_option_over_max)
                                        , data.getMaxOption()), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } else if (event.message.equals(EventBusController.OptionChoiceEvent.OPTION_EXPAND)) {
            if (optionItemAdapter.getExpandOptionlist().contains(id)) {
                optionItemAdapter.getExpandOptionlist().remove(optionItemAdapter.getExpandOptionlist()
                        .indexOf(id));
            } else {
                optionItemAdapter.getExpandOptionlist().add(id);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionControl(EventBusController.OptionControlEvent event) {
        long id = event.Id;
        if (event.message.equals(EventBusController.OptionControlEvent.OPTION_ADD)) {
            Option option = new Option();
            option.setCount(0);
            option.setId(newOptionIdAuto--);
            optionList.add(option);
            optionItemAdapter.notifyDataSetChanged();
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_REMOVE)) {
            int removePosition = -1;
            for (int i = 0; i < optionList.size(); i++) {
                if (optionList.get(i).getId() == id) {
                    removePosition = i;
                    break;
                }
            }
            if (removePosition >= 0) {
                optionList.remove(removePosition);
                optionItemAdapter.getChoiceList().remove(optionItemAdapter.getChoiceList().indexOf(id));
                optionItemAdapter.notifyDataSetChanged();
            }
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_INPUT_TEXT)) {
            //appBarMain.setExpanded(false);
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
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteService(EventBusController.RemoteServiceEvent event) {
        if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE)) {
            if (event.success) {
                VoteData data = event.response.body();
                List<Option> optionList = data.getNetOptions();
                Log.d(TAG, "GET vote success:" + data.getVoteCode() + " image:" + data.getVoteImage());
                new SaveVoteDataToDBTask(data, optionList).execute();
            } else {
                Toast.makeText(this, R.string.create_vote_toast_create_fail, Toast.LENGTH_LONG).show();
                Log.d(TAG, "GET vote false:");
                hideLoadingCircle();
            }
        }
    }

    private class LoadVoteDataFromDBTask extends AsyncTask<Void, Void, Void> {
        private String voteCode;

        public LoadVoteDataFromDBTask(String voteCode) {
            this.voteCode = voteCode;
        }

        @Override
        protected void onPreExecute() {
            //showLoadingCircle(getString(R.string.vote_detail_circle_loading));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideLoadingCircle();
            checkCurrentOptionType();
            setUpViews();
            setUpOptionAdapter(optionList);
            setUpSubmit();
            optionItemAdapter.notifyDataSetChanged();
            if (isMultiChoice) {
                Toast.makeText(context, String.format(getString(R.string.vote_detail_dialog_multi_option)
                        , data.getMinOption(), data.getMaxOption()), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            data = DataLoader.getInstance(context).queryVoteDataById(this.voteCode);
            optionList = data.getOptions();
            return null;
        }
    }

    private class SaveVoteDataToDBTask extends AsyncTask<Void, Void, Void> {

        private VoteData voteSetting;
        private List<Option> optionList;

        public SaveVoteDataToDBTask(VoteData voteSetting, List<Option> optionList) {
            this.voteSetting = voteSetting;
            this.optionList = optionList;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideLoadingCircle();
            checkCurrentOptionType();
            setUpViews();
            setUpOptionAdapter(optionList);
            setUpSubmit();
            optionItemAdapter.notifyDataSetChanged();
            if (isMultiChoice) {
                Toast.makeText(context, String.format(getString(R.string.vote_detail_dialog_multi_option)
                        , data.getMinOption(), data.getMaxOption()), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            voteSetting.setOptionCount(optionList.size());
            for (int i = 0; i < optionList.size(); i++) {
                Option option = optionList.get(i);
                option.setVoteCode(voteSetting.getVoteCode());
                option.setId(null);
                if (i == 0) {
                    voteSetting.setOption1Title(option.getTitle());
                    voteSetting.setOption1Code(option.getCode());
                    voteSetting.setOption1Count(option.getCount());
                } else if (i == 1) {
                    voteSetting.setOption2Title(option.getTitle());
                    voteSetting.setOption2Code(option.getCode());
                    voteSetting.setOption2Count(option.getCount());
                }
                option.dumpDetail();
            }
            DataLoader.getInstance(getApplicationContext()).deleteVoteDataAndOption(voteSetting.getVoteCode());
            DataLoader.getInstance(getApplicationContext()).getVoteDataDao().insertOrReplace(voteSetting);
            DataLoader.getInstance(getApplicationContext()).getOptionDao().insertOrReplaceInTx(optionList);

            data = DataLoader.getInstance(context).queryVoteDataById(voteSetting.getVoteCode());
            optionList = data.getOptions();
            return null;
        }
    }

    private class UpdateVoteDataToServerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            circleLoad.setText(getString(R.string.vote_detail_circle_updating));
            circleLoad.setVisibility(View.VISIBLE);
            circleLoad.spin();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            circleLoad.stopSpinning();
            circleLoad.setVisibility(View.GONE);
            fabPreResult.setVisibility(View.GONE);

            checkCurrentOptionType();
            setUpViews();
            showResultOption();
            setUpSubmit();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // todo:Update to db.
            data.setIsPolled(true);
            for (int i = 0; i < optionList.size(); i++) {
                Option option = optionList.get(i);
                for (int j = 0; j < optionItemAdapter.getChoiceList().size(); j++) {
                    Log.d("test", "choice option:id:" + optionItemAdapter.getChoiceList().get(j)
                            + " nor id:" + option.getId());
                    if (optionItemAdapter.getChoiceList().get(j).longValue() == option.getId().longValue()) {
                        Log.d("test", "1 choice option:id:" + optionItemAdapter.getChoiceList().get(j));
                        option.setCount(option.getCount() + 1);
                        option.setIsUserChoiced(true);
                        data.setOptionUserChoiceTitle(option.getTitle());
                        data.setOptionUserChoiceCount(option.getCount());
                        data.setOptionUserChoiceCode(option.getId() > 0 ? option.getCode() :
                                data.getVoteCode() + "_" + (optionList.size() + option.getId()));
                        option.dumpDetail();
                        break;
                    }
                }
                if (option.getId() < 0) {
                    // add new option api to sync db
                    Log.d("test", "new option:id:" + option.getId());
                    option.setCode(data.getVoteCode() + "_" + (optionList.size() + option.getId()));
                    option.setVoteCode(data.getVoteCode());
                    option.setId(null);
                }
                if (option.getCount() > data.getOptionTopCount()) {
                    data.setOptionTopCode(option.getCode());
                    data.setOptionTopCount(option.getCount());
                    data.setOptionTopTitle(option.getTitle());
                }
                if (i == 0) {
                    data.setOption1Title(option.getTitle());
                    data.setOption1Code(option.getCode());
                    data.setOption1Count(option.getCount());
                } else if (i == 1) {
                    data.setOption2Code(option.getCode());
                    data.setOption2Count(option.getCount());
                    data.setOption2Title(option.getTitle());
                }
                option.dumpDetail();
            }
            data.setPollCount(data.getPollCount() + optionItemAdapter.getChoiceList().size());
            optionItemAdapter.setVoteData(data);

            VoteDataDao voteDataDao = DataLoader.getInstance(context).getVoteDataDao();
            voteDataDao.insertOrReplace(data);
            OptionDao optionDao = DataLoader.getInstance(context).getOptionDao();
            optionDao.insertOrReplaceInTx(optionList);
            optionList = data.getOptions();
            EventBus.getDefault().post(new EventBusController
                    .VoteDataControlEvent(data, EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_AND_CONTENT));

            try {
                Thread.currentThread().sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
