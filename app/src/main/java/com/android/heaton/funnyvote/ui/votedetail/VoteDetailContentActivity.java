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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.Util;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.database.VoteDataLoader;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.ui.main.VHVoteWallItem;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
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

    private static final int FAB_THRESHOD = 100;
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
    @BindView(R.id.fabOptionSearch)
    FloatingActionButton fabOptionSearch;
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
    Menu menu;
    private OptionItemAdapter optionItemAdapter;
    private VoteData data;
    private Activity context;
    private List<Option> optionList;
    private int optionType = OptionItemAdapter.OPTION_UNPOLL;
    private boolean isMultiChoice = false;
    private boolean isUserPreResult = false;

    public static void sendShareIntent(Context context, VoteData data) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format(
                context.getString(R.string.vote_share_to_other_app_default_message), data.getTitle()));
        context.startActivity(Intent.createChooser(sendIntent
                , context.getResources().getText(R.string.vote_detail_menu_share_to)));
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

        String voteId = getIntent().getExtras().getString(VHVoteWallItem.BUNDLE_KEY_VOTE_ID);
        data.setVoteCode(voteId);

        circleLoad.setText(getString(R.string.vote_detail_circle_loading));
        circleLoad.setTextMode(TextMode.TEXT);
        circleLoad.setShowTextWhileSpinning(true);
        circleLoad.setFillCircleColor(getColor(R.color.md_amber_50));

        checkCurrentOptionType();
        setUpViews();
        setUpSubmit();
        setUpOptionAdapter(new ArrayList<Option>());

        ryOptionArea.addOnScrollListener(new HidingScrollListener(FAB_THRESHOD) {
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
        new LoadVoteDataTask(voteId).execute();
    }

    private void setUpViews() {
        txtAuthorName.setText(data.getAuthorName());
        txtPubTime.setText(Util.getDate(data.getStartTime(), "dd/MM hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "dd/MM hh:mm"));
        txtTitle.setText(data.getTitle());

        imgBarFavorite.setImageResource(data.getIsFavorite() ? R.drawable.ic_star_24dp :
                R.drawable.ic_star_border_24dp);

        txtBarPollCount.setText(String.format(this
                .getString(R.string.Wall_item_bar_vote_count), data.getPollCount()));

        imgMain.setImageResource(data.getLocalImage());

        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT || !data.getIsCanPreviewResult()) {
            fabPreResult.setVisibility(View.GONE);
        } else {
            fabPreResult.setVisibility(View.VISIBLE);
        }
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

    private void setUpOptionAdapter(List<Option> optionData) {
        optionItemAdapter = new OptionItemAdapter(optionType, optionData, data);
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
        // TODO: Update to db.
    }

    @OnClick(R.id.relBarShare)
    public void onBarShareClick() {
        sendShareIntent(this, data);
    }

    @OnClick({R.id.fabOptionSearch, R.id.fabTop, R.id.fabPreResult})
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
        } else if (id == R.id.fabOptionSearch) {

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
        optionItemAdapter.notifyDataSetChanged();
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
        optionItemAdapter.notifyDataSetChanged();
        // Todo: set animation to make transfer funny and smooth.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_content_detail, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_submit) {
            // Save to db and updating to db.
            if (optionItemAdapter.getChoiceList().size() < data.getMinOption()) {
                Toast.makeText(this, String.format(getString(R.string.vote_detail_toast_option_at_least_min)
                        , data.getMinOption()), Toast.LENGTH_LONG).show();
            } else {
                new UpdateVoteDataTask().execute();
            }
            return true;
        } else if (id == R.id.menu_info) {
            showVoteInfo();
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

    private void showVoteInfo() {
        View content = LayoutInflater.from(this).inflate(R.layout.dialog_vote_detail_info, null);
        TextView option = ButterKnife.findById(content, R.id.txtOptionInfo);
        TextView time = ButterKnife.findById(content, R.id.txtTime);
        if (!isMultiChoice) {
            option.setText(getString(R.string.vote_detail_dialog_single_option));
        } else {
            String multi = String.format(getString(R.string.vote_detail_dialog_multi_option)
                    , data.getMinOption(), data.getMaxOption());
            option.setText(multi);
        }
        time.setText(Util.getDate(data.getStartTime(), "dd/MM hh:mm")
                + " ~ " + Util.getDate(data.getEndTime(), "dd/MM hh:mm"));
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
        int position = event.optionPosition;
        if (optionType == OptionItemAdapter.OPTION_SHOW_RESULT) {
            return;
        }
        if (!isMultiChoice) {
            optionItemAdapter.getChoiceList().clear();
            optionItemAdapter.getChoiceList().add(position);
            optionItemAdapter.notifyDataSetChanged();
        } else {
            if (optionItemAdapter.getChoiceList().contains(position)) {
                optionItemAdapter.getChoiceList().remove(optionItemAdapter.getChoiceList()
                        .indexOf(position));
                optionItemAdapter.notifyDataSetChanged();
            } else {
                if (optionItemAdapter.getChoiceList().size() < data.getMaxOption()) {
                    optionItemAdapter.getChoiceList().add(position);
                    optionItemAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this
                            , String.format(this.getString(R.string.vote_detail_toast_option_over_max)
                                    , data.getMaxOption()), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOptionControl(EventBusController.OptionControlEvent event) {
        int position = event.position;
        if (event.message.equals(EventBusController.OptionControlEvent.OPTION_ADD)) {
            optionItemAdapter.getNewOptionList().add(new Option());
            optionItemAdapter.notifyDataSetChanged();
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_REMOVE)) {
            optionItemAdapter.getNewOptionList().remove(position - optionList.size());
            optionItemAdapter.notifyDataSetChanged();
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_INPUT_TEXT)) {
            int target = position - optionList.size();
            optionItemAdapter.getNewOptionList().get(target).setTitle(event.inputText);
        } else if (event.message.equals(EventBusController.OptionControlEvent.OPTION_EXPAND)) {

            if (optionItemAdapter.getExpandOptionlist().contains(event.position)) {
                optionItemAdapter.getExpandOptionlist().remove(optionItemAdapter.getExpandOptionlist()
                        .indexOf(position));
            } else {
                optionItemAdapter.getExpandOptionlist().add(event.position);
            }
        }
    }

    private class LoadVoteDataTask extends AsyncTask<Void, Void, Void> {
        private String voteCode;

        public LoadVoteDataTask(String voteCode) {
            this.voteCode = voteCode;
        }

        ;

        @Override
        protected void onPreExecute() {
            circleLoad.setText(getString(R.string.vote_detail_circle_loading));
            circleLoad.setVisibility(View.VISIBLE);
            circleLoad.spin();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            circleLoad.stopSpinning();
            circleLoad.setVisibility(View.GONE);
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
            data = VoteDataLoader.getInstance(context).queryVoteDataById(this.voteCode);
            optionList = data.getOptions();
            return null;
        }
    }

    private class UpdateVoteDataTask extends AsyncTask<Void, Void, Void> {

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
            optionList.addAll(optionItemAdapter.getNewOptionList());
            optionItemAdapter.getNewOptionList().clear();
            for (int i = 0; i < optionItemAdapter.getChoiceList().size(); i++) {
                int count = optionList.get(optionItemAdapter.getChoiceList().get(i)).getCount();
                Option option = optionList.get(optionItemAdapter.getChoiceList().get(i));
                option.setCount(count + 1);
                if (option.getCount() > data.getOptionTopCount()) {
                    data.setOptionTopCode(option.getCode());
                    data.setOptionTopCount(option.getCount());
                    data.setOptionTopTitle(option.getTitle());
                }

            }
            data.setPollCount(data.getPollCount() + optionItemAdapter.getChoiceList().size());
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

        public boolean mControlsVisible = true;
        private int HIDE_THRESHOLD = 50;
        private int mScrolledDistance = 0;

        public HidingScrollListener(int threshold) {
            this.HIDE_THRESHOLD = threshold;
        }

        private HidingScrollListener() {
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
            if (firstVisibleItem == 0) {
                if (!mControlsVisible) {
                    onShow();
                    mControlsVisible = true;
                }
            } else {
                if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                    onHide();
                    mControlsVisible = false;
                    mScrolledDistance = 0;
                } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                    onShow();
                    mControlsVisible = true;
                    mScrolledDistance = 0;
                }
            }
            if ((mControlsVisible && dy > 0) || (!mControlsVisible && dy < 0)) {
                mScrolledDistance += dy;
            }
        }

        public void resetScrollDistance() {
            mControlsVisible = true;
            mScrolledDistance = 0;
        }

        public abstract void onHide();

        public abstract void onShow();
    }
}
