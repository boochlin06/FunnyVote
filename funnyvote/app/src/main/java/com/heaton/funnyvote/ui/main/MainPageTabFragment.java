/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.heaton.funnyvote.ui.main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.VoteData.VoteDataManager;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.eventbus.EventBusController;
import com.heaton.funnyvote.ui.HidingScrollListener;
import com.getbase.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainPageTabFragment extends Fragment implements VoteWallItemAdapter.OnReloadClickListener {
    private static final int LIMIT = VoteDataManager.PAGE_COUNT;
    public static String TAG = MainPageTabFragment.class.getSimpleName();

    public static final String KEY_TAB = "tab";
    public static final String KEY_LOGIN_USER = "key_login_user";
    public static final String KEY_TARGET_USER = "key_target_user";

    public static final String TAB_HOT = "HOT";
    public static final String TAB_NEW = "NEW";

    public static final String TAB_CREATE = "CREATE";
    public static final String TAB_PARTICIPATE = "PARTICIPATE";
    public static final String TAB_FAVORITE = "FAVORITE";

    private RecyclerView ryMain;
    private RelativeLayout RootView;
    private String tab = TAB_HOT;
    private List<VoteData> voteDataList;
    private VoteWallItemAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabTop;
    private VoteDataManager voteDataManager;
    private User loginUser;
    private User targetUser;
    private AlertDialog passwordDialog;

    public static MainPageTabFragment newInstance(String tab, User loginUser) {
        return newInstance(tab, loginUser, null);
    }

    public static MainPageTabFragment newInstance(String tab, User loginUser, User targetUser) {
        MainPageTabFragment fragment = new MainPageTabFragment();
        Bundle argument = new Bundle();
        argument.putString(MainPageTabFragment.KEY_TAB, tab);
        argument.putParcelable(MainPageTabFragment.KEY_LOGIN_USER, loginUser);
        argument.putParcelable(MainPageTabFragment.KEY_TARGET_USER, targetUser);
        fragment.setArguments(argument);
        fragment.setRetainInstance(false);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle argument = getArguments();
        this.tab = argument.getString(KEY_TAB);
        this.loginUser = (User) argument.getParcelable(KEY_LOGIN_USER);
        this.targetUser = (User) argument.getParcelable(KEY_TARGET_USER);
        voteDataManager = VoteDataManager.getInstance(getContext().getApplicationContext());
        RootView = (RelativeLayout) inflater.inflate(R.layout.fragment_main_page_tab, container, false);
        fabTop = (FloatingActionButton) RootView.findViewById(R.id.fabTop);
        fabTop.setVisibility(View.GONE);
        ryMain = (RecyclerView) RootView.findViewById(R.id.ryMainPage);
        swipeRefreshLayout = (SwipeRefreshLayout) RootView.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new WallItemOnRefreshListener());

        return RootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayoutManager manager = (LinearLayoutManager) ryMain.getLayoutManager();
        int position = manager.findFirstVisibleItemPosition();
        if (position == 0) {
            // TODO:AUTO UPDATE .
            //refreshData();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void initRecyclerView() {
        voteDataList = new ArrayList<>();
        adapter = new VoteWallItemAdapter(getActivity()
                , voteDataList);
        // if max count is -1 , the list is init.
        adapter.setMaxCount(-1);
        if (tab.equals(TAB_HOT)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH);
            voteDataManager.getHotVoteList(0, loginUser);
        } else if (tab.equals(TAB_NEW)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_REFRESH);
            voteDataManager.getNewVoteList(0, loginUser);
        } else if (tab.equals(TAB_CREATE)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_CREATE_NEW);
            voteDataManager.getUserCreateVoteList(0, loginUser, targetUser);
        } else if (tab.equals(TAB_PARTICIPATE)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_CREATE_NEW);
            voteDataManager.getUserParticipateVoteList(0, loginUser);
        } else if (tab.equals(TAB_FAVORITE)) {
            adapter.setNoVoteTag(VoteWallItemAdapter.TAG_NO_VOTE_CREATE_NEW);
            voteDataManager.getUserFavoriteVoteList(0, loginUser, targetUser);
        }
        adapter.resetItemTypeList();
        adapter.setOnReloadClickListener(this);
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapter);
        scaleInAnimationAdapter.setDuration(1000);
        ryMain.setAdapter(adapter);
        ryMain.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                fabTop.animate().translationY(
                        fabTop.getHeight() + 50)
                        .setInterpolator(new AccelerateInterpolator(2));
            }

            @Override
            public void onShow() {
                this.resetScrollDistance();
                fabTop.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
            }
        });
        //fabTop.setIconDrawable(Util.textAsBitmap(getContext(),"New",20,R.color.md_blue_700));
        //fabTop.setVisibility(View.VISIBLE);
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayoutManager manager = (LinearLayoutManager) ryMain.getLayoutManager();
                int position = manager.findFirstVisibleItemPosition();
                if (position > 5) {
                    ryMain.scrollToPosition(5);
                }
                ryMain.smoothScrollToPosition(0);
                ryMain.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post(new EventBusController.UIControlEvent(
                                EventBusController.UIControlEvent.SCROLL_TO_TOP));
                    }
                }, 200);
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoteControl(EventBusController.VoteDataControlEvent event) {
        if (getUserVisibleHint()) {
            Log.d(TAG, tab + " ,On Vote Control :" + event.message);
            if (event.message.equals(EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_AND_CONTENT)) {
                VoteData data = event.data;
                for (int i = 0; i < voteDataList.size(); i++) {
                    if (data.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                        voteDataList.set(i, data);
                        adapter.resetItemTypeList();
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            } else if (event.message.equals(EventBusController.VoteDataControlEvent.VOTE_FAVORITE)) {
                voteDataManager.favoriteVote(event.data.getVoteCode()
                        , event.data.getIsFavorite(), loginUser);
            } else if (event.message.equals(EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_FOR_FAVORITE)) {
                updateVoteDataToList(event.data);
            } else if (getUserVisibleHint() && event.message.equals(EventBusController.VoteDataControlEvent.VOTE_QUICK_POLL)
                    && isResumed()) {
                if (event.data.getIsNeedPassword()) {
                    Log.d(TAG, "Vote code:" + event.data.getVoteCode() + " pw:" + event.data.getIsNeedPassword());
                    showPasswordDialog(event.data, event.optionList, loginUser);
                } else {
                    voteDataManager.pollVote(event.data.getVoteCode(), null, event.optionList, loginUser);
                }
            }
        }
    }

    private void updateVoteDataToList(VoteData data) {
        for (int i = 0; i < voteDataList.size(); i++) {
            if (data.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                voteDataList.set(i, data);
                adapter.resetItemTypeList();
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }
    private void updateVoteDataFavoriteToList(VoteData data) {
        for (int i = 0; i < voteDataList.size(); i++) {
            if (data.getVoteCode().equals(voteDataList.get(i).getVoteCode())) {
                if (data.getIsFavorite() == voteDataList.get(i).getIsFavorite()) {
                    return;
                }
                voteDataList.get(i).setIsFavorite(data.getIsFavorite());
                voteDataList.set(i, voteDataList.get(i));
                adapter.resetItemTypeList();
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    private void showPasswordDialog(final VoteData data, final List<String> optionList, final User user) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.password_dialog);
        builder.setPositiveButton(getActivity().getResources()
                .getString(R.string.vote_detail_dialog_password_input), null);
        builder.setNegativeButton(getContext().getApplicationContext().getResources()
                .getString(R.string.account_dialog_cancel), null);
        builder.setTitle(getActivity().getString(R.string.vote_detail_dialog_password_title));
        passwordDialog = builder.create();

        passwordDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final EditText password = (EditText) ((AlertDialog) dialogInterface).findViewById(R.id.edtEnterPassword);
                Button ok = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                ok.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        voteDataManager.pollVote(data.getVoteCode(), password.getText().toString(), optionList, user);
                    }
                });
            }
        });
        passwordDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRemoteEvent(EventBusController.RemoteServiceEvent event) {

        boolean refreshFragment = false;
        if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HOT)
                && tab.equals(TAB_HOT)) {
            Log.d(TAG, "Hot vote list size:" + event.voteDataList.size() + " offset:" + event.offset);
            refreshFragment = true;
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_NEW)
                && tab.equals(TAB_NEW)) {
            Log.d(TAG, "New vote list size:" + event.voteDataList.size() + " offset:" + event.offset);
            refreshFragment = true;
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE)
                && tab.equals(TAB_CREATE)) {
            Log.d(TAG, "Create history vote list size:" + event.voteDataList.size() + " offset:" + event.offset);
            refreshFragment = true;
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_PARTICIPATE)
                && tab.equals(TAB_PARTICIPATE)) {
            Log.d(TAG, "Participate history list size:" + event.voteDataList.size() + " offset:" + event.offset);
            refreshFragment = true;
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE)
                && tab.equals(TAB_FAVORITE)) {
            Log.d(TAG, "favorite list size:" + event.voteDataList.size() + " offset:" + event.offset);
            refreshFragment = true;
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.FAVORITE_VOTE)) {
            Log.d(TAG, "favorite vote");
            updateVoteDataFavoriteToList(event.voteData);
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.POLL_VOTE)
                && getUserVisibleHint()) {
            Log.d(TAG, "poll vote");
            if (event.success) {
                if (passwordDialog != null && passwordDialog.isShowing()) {
                    passwordDialog.dismiss();
                }
                if (tab.equals(TAB_PARTICIPATE)) {
                    refreshFragment = true;
                }
                Toast.makeText(getContext().getApplicationContext(), R.string.toast_network_connect_success_poll
                        , Toast.LENGTH_SHORT).show();
            } else {
                if (event.errorResponseMessage.equals("error_invalid_password")) {
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        final EditText password = (EditText) passwordDialog.findViewById(R.id.edtEnterPassword);
                        password.selectAll();
                        Animation shake = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.edittext_shake);
                        password.startAnimation(shake);
                        Toast.makeText(getContext().getApplicationContext(), getString(R.string.vote_detail_dialog_password_toast_retry)
                                , Toast.LENGTH_LONG).show();
                    }
                } else {
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        passwordDialog.dismiss();
                    }
                    Toast.makeText(getContext().getApplicationContext(), R.string.toast_network_connect_error_quick_poll, Toast.LENGTH_LONG).show();
                }
            }
        } else if (event.message.equals(EventBusController.RemoteServiceEvent.CREATE_VOTE) && getUserVisibleHint()
                && tab.equals(TAB_CREATE) && event.success) {
            refreshFragment = true;
        }
        if (refreshFragment) {
            Log.d(TAG, tab + ": refreshFragment , is visible:" + this.getUserVisibleHint());
            refreshData(event.voteDataList, event.offset);
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
            EventBus.getDefault().post(new EventBusController.UIControlEvent(EventBusController.UIControlEvent.HIDE_CIRCLE));
        }
        if (refreshFragment && !event.success && !"error_no_poll_event".equals(event.errorResponseMessage)
                && getUserVisibleHint()) {
            Toast.makeText(getContext().getApplicationContext(), R.string.toast_network_connect_error_get_list, Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new EventBusController.UIControlEvent(EventBusController.UIControlEvent.HIDE_CIRCLE));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private class WallItemOnRefreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            if (loginUser == null) {
                EventBus.getDefault().post(new EventBusController.NetworkEvent(EventBusController.NetworkEvent.RELOAD_USER
                        , tab));
            } else {
                if (tab.equals(TAB_HOT)) {
                    voteDataManager.getHotVoteList(0, loginUser);
                } else if (tab.equals(TAB_NEW)) {
                    voteDataManager.getNewVoteList(0, loginUser);
                } else if (tab.equals(TAB_CREATE)) {
                    voteDataManager.getUserCreateVoteList(0, loginUser, targetUser);
                } else if (tab.equals(TAB_PARTICIPATE)) {
                    voteDataManager.getUserParticipateVoteList(0, loginUser);
                } else if (tab.equals(TAB_FAVORITE)) {
                    voteDataManager.getUserFavoriteVoteList(0, loginUser, targetUser);
                }
            }
        }
    }

    private void refreshData(List<VoteData> voteDataList, int offset) {
        Log.d(TAG, "Network Refresh wall item :" + tab);
        int pageNumber = offset / LIMIT;
        if (offset == 0) {
            this.voteDataList = voteDataList;
            adapter.setVoteList(this.voteDataList);
        } else if (offset >= this.voteDataList.size()) {
            this.voteDataList.addAll(voteDataList);
            adapter.setVoteList(this.voteDataList);
        }
        if (this.voteDataList.size() < LIMIT * (pageNumber + 1)) {
            adapter.setMaxCount(this.voteDataList.size());
            if (offset != 0) {
                Toast.makeText(getContext(), R.string.wall_item_toast_no_vote_refresh, Toast.LENGTH_SHORT).show();
            }
        } else {
            adapter.setMaxCount(LIMIT * (pageNumber + 2));
        }
        adapter.resetItemTypeList();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onReloadClicked() {
        final int offset = voteDataList.size();
        if (tab.equals(TAB_HOT)) {
            voteDataManager.getHotVoteList(offset, loginUser);
        } else if (tab.equals(TAB_NEW)) {
            voteDataManager.getNewVoteList(offset, loginUser);
        } else if (tab.equals(TAB_CREATE)) {
            voteDataManager.getUserCreateVoteList(offset, loginUser, targetUser);
        } else if (tab.equals(TAB_PARTICIPATE)) {
            voteDataManager.getUserParticipateVoteList(offset, loginUser);
        } else if (tab.equals(TAB_FAVORITE)) {
            voteDataManager.getUserFavoriteVoteList(offset, loginUser, targetUser);
        }
    }

    public void setTab(String tab) {
        this.tab = tab;
    }
}
