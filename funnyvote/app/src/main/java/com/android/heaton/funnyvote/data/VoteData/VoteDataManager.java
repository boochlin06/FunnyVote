package com.android.heaton.funnyvote.data.VoteData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.android.heaton.funnyvote.data.RemoteServiceApi;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.Option;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.database.VoteData;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by heaton on 2016/12/25.
 */

public class VoteDataManager {
    private static final String TAG = VoteDataManager.class.getSimpleName();
    private static VoteDataManager INSTANCE = null;
    ExecutorService executorService;

    private Context context;
    private RemoteServiceApi remoteServiceApi;

    public static VoteDataManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (VoteDataManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new VoteDataManager(context, new RemoteServiceApi());
                }
            }
        }
        return INSTANCE;
    }

    public VoteDataManager(Context context, RemoteServiceApi remoteServiceApi) {
        this.context = context;
        this.remoteServiceApi = remoteServiceApi;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void createVote(@NonNull VoteData voteSetting, @NonNull List<String> options, File image) {
        if (voteSetting == null || options == null || options.size() < 2) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.CREAT_VOTE, false, new IllegalArgumentException().toString()));
        } else {
            remoteServiceApi.createVote(voteSetting, options, image, new createVoteResponseCallback());
        }
    }

    public void getVote(@NonNull String voteCode, User user) {
        if (user == null) {
            executorService.execute(new LoadDBRunnable(voteCode, EventBusController
                    .RemoteServiceEvent.GET_VOTE, false));
        } else {
            remoteServiceApi.getVote(voteCode, user, new getVoteResponseCallback(voteCode));
        }
    }

    public void pollVote(@NonNull String voteCode, @NonNull List<String> pollOptions, @NonNull User user) {
        if (TextUtils.isEmpty(voteCode) || pollOptions.size() == 0 || user == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.POLL_VOTE, false, new IllegalArgumentException().toString()));
        } else {
            remoteServiceApi.pollVote(voteCode, pollOptions, user, new pollVoteResponseCallback());
        }
    }

    public class createVoteResponseCallback implements Callback<VoteData> {

        public createVoteResponseCallback() {
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadDBRunnable(response.body(), EventBusController
                        .RemoteServiceEvent.CREAT_VOTE, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.w("test", "createVoteResponseCallback onResponse false:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.CREAT_VOTE, false, errorMessage));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.w("test", "createVoteResponseCallback onFailure:" + call.toString());
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.CREAT_VOTE, false, t.getMessage()));
        }
    }

    public class getVoteResponseCallback implements Callback<VoteData> {

        private String voteCode;

        public getVoteResponseCallback(String voteCode) {
            this.voteCode = voteCode;
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadDBRunnable(response.body(), EventBusController
                        .RemoteServiceEvent.GET_VOTE, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.d("test", "getVoteResponseCallback onResponse false:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executorService.execute(new LoadDBRunnable(voteCode, EventBusController
                        .RemoteServiceEvent.GET_VOTE, false));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.d("test", "getVoteResponseCallback onFailure:" + t.getMessage());
            executorService.execute(new LoadDBRunnable(voteCode, EventBusController
                    .RemoteServiceEvent.GET_VOTE, false));
        }
    }

    public class pollVoteResponseCallback implements Callback<VoteData> {

        public pollVoteResponseCallback() {
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadDBRunnable(response.body(), EventBusController
                        .RemoteServiceEvent.POLL_VOTE, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.d("test", "pollVoteResponseCallback onResponse false:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.POLL_VOTE, false, errorMessage));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.d("test", "pollVoteResponseCallback onFailure:" + call.toString());
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.POLL_VOTE, false, t.getMessage()));
        }
    }

    private class SaveAndLoadDBRunnable implements Runnable {
        private VoteData voteSetting;
        private String message;
        private boolean success;

        public SaveAndLoadDBRunnable(VoteData voteSetting, String message, boolean success) {
            this.voteSetting = voteSetting;
            this.message = message;
            this.success = success;
        }

        @Override
        public void run() {
            List<Option> optionList = voteSetting.getNetOptions();
            voteSetting.setOptionCount(optionList.size());
            int maxOption = 0;
            for (int i = 0; i < optionList.size(); i++) {
                Option option = optionList.get(i);
                option.setVoteCode(voteSetting.getVoteCode());
                if (option.getCount() == null) {
                    option.setCount(0);
                }
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
                if (option.getCount() > maxOption && option.getCount() >= 1) {
                    voteSetting.setOptionTopCount(option.getCount());
                    voteSetting.setOptionTopCode(option.getCode());
                    voteSetting.setOptionTopTitle(option.getTitle());
                }
                if (option.getIsUserChoiced()) {
                    voteSetting.setOptionUserChoiceCode(option.getCode());
                    voteSetting.setOptionUserChoiceTitle(option.getTitle());
                    voteSetting.setOptionUserChoiceCount(option.getCount());
                }

                option.dumpDetail();
            }
            DataLoader.getInstance(context.getApplicationContext()).deleteVoteDataAndOption(voteSetting.getVoteCode());
            DataLoader.getInstance(context.getApplicationContext()).getVoteDataDao().insertOrReplace(voteSetting);
            DataLoader.getInstance(context.getApplicationContext()).getOptionDao().insertOrReplaceInTx(optionList);

            VoteData data = DataLoader.getInstance(context).queryVoteDataById(voteSetting.getVoteCode());
            optionList = data.getOptions();

            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(message, success, data, optionList));
            EventBus.getDefault().post(new EventBusController
                    .VoteDataControlEvent(data, EventBusController.VoteDataControlEvent.VOTE_SYNC_WALL_AND_CONTENT));
        }
    }

    private class LoadDBRunnable implements Runnable {

        private String voteCode;
        private boolean success;
        private String message;

        public LoadDBRunnable(String voteCode, String message, boolean success) {
            this.voteCode = voteCode;
            this.success = success;
            this.message = message;
        }

        @Override
        public void run() {
            VoteData data = DataLoader.getInstance(context).queryVoteDataById(this.voteCode);
            List<Option> optionList = data.getOptions();

            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(this.message
                    , this.success, data, optionList));
        }
    }
}
