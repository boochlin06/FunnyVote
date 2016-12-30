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
import com.android.heaton.funnyvote.database.VoteDataDao;
import com.android.heaton.funnyvote.eventbus.EventBusController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static final int PAGE_COUNT = 20;
    private static VoteDataManager INSTANCE = null;
    private ExecutorService executorService;

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

    public void getVoteList(int offset, String eventMessage, @NonNull User user) {
        if (TextUtils.isEmpty(eventMessage) || user == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(eventMessage
                    , false, new IllegalArgumentException().toString()));
        } else {
            int pageNumber = (int) offset / PAGE_COUNT;
            int pageCount = PAGE_COUNT;
            if (eventMessage.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE)) {
                remoteServiceApi.getVoteList(pageNumber, pageCount, eventMessage, user
                        , new getVoteListResponseCallback(offset, eventMessage, user.getUserCode()));
            } else {
                remoteServiceApi.getVoteList(pageNumber, pageCount, eventMessage, user
                        , new getVoteListResponseCallback(offset, eventMessage));
            }
        }
    }

    public void getHotVoteList(int offset, @NonNull User user) {
        getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HOT, user);
    }

    public void getNewVoteList(int offset, @NonNull User user) {
        getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_NEW, user);
    }

    public void getFavoriteVoteList(int offset, @NonNull User user) {
        getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE, user);
    }

    public void getUserCreateVoteList(int offset, @NonNull User user) {
        getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE, user);
    }

    public void getUserParticipateVoteList(int offset, @NonNull User user) {
        getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_PARTICIPATE, user);
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

    public class getVoteListResponseCallback implements Callback<List<VoteData>> {

        private String message;
        private int offset;
        private String userCode;

        public getVoteListResponseCallback(int offset, String message) {
            this(offset,message,null);
        }

        public getVoteListResponseCallback(int offset, String message, String userCode) {
            this.message = message;
            this.offset = offset;
            this.userCode = userCode;
        }

        @Override
        public void onResponse(Call<List<VoteData>> call, Response<List<VoteData>> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadListDBRunnable(response.body(), offset
                        , message, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.d("test", "getVoteListResponseCallback onResponse false:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE)) {
                    executorService.execute(new LoadListDBRunnable(offset, message, false, userCode));
                } else {
                    executorService.execute(new LoadListDBRunnable(offset, message, false));
                }
            }
        }

        @Override
        public void onFailure(Call<List<VoteData>> call, Throwable t) {
                Log.d("test", "getVoteListResponseCallback onFailure:" + t.getMessage()+" MESSAGE:"+this.message);
                if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE)) {
                    executorService.execute(new LoadListDBRunnable(offset, message, false, userCode));
                } else {
                    executorService.execute(new LoadListDBRunnable(offset, message, false));
                }
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
            if (voteSetting.getGuestCode() != null && voteSetting.getMemberCode() == null) {
                voteSetting.setAuthorCode(voteSetting.getGuestCode());
            } else {
                voteSetting.setAuthorCode(voteSetting.getMemberCode());
            }
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

    private class SaveAndLoadListDBRunnable implements Runnable {
        private List<VoteData> voteDataList;
        private String message;
        private boolean success;
        private int offset;

        public SaveAndLoadListDBRunnable(List<VoteData> voteDataList, int offset, String message, boolean success) {
            this.message = message;
            this.success = success;
            this.voteDataList = voteDataList;
            this.offset = offset;
        }

        @Override
        public void run() {
            ArrayList<WhereCondition> whereConditions = new ArrayList<WhereCondition>();
            for (int i = 0; i < voteDataList.size(); i++) {
                VoteData voteData = voteDataList.get(i);
                if (voteData.getFirstOption() != null) {
                    voteData.setOption1Code(voteData.getFirstOption().getCode());
                    voteData.setOption1Title(voteData.getFirstOption().getTitle());
                    voteData.setOption1Count(voteData.getFirstOption().getCount());
                }
                if (voteData.getSecondOption() != null) {
                    voteData.setOption2Code(voteData.getSecondOption().getCode());
                    voteData.setOption2Title(voteData.getSecondOption().getTitle());
                    voteData.setOption2Count(voteData.getSecondOption().getCount());
                }
                if (voteData.getTopOption() != null) {
                    voteData.setOptionTopCode(voteData.getTopOption().getCode());
                    voteData.setOptionTopTitle(voteData.getTopOption().getTitle());
                    voteData.setOptionTopCount(voteData.getTopOption().getCount());
                }
                if (voteData.getUserOption() != null) {
                    voteData.setOptionUserChoiceCode(voteData.getUserOption().getCode());
                    voteData.setOptionUserChoiceTitle(voteData.getUserOption().getTitle());
                    voteData.setOptionUserChoiceCount(voteData.getUserOption().getCount());
                }
                if (voteData.getGuestCode() != null && voteData.getMemberCode() == null) {
                    voteData.setAuthorCode(voteData.getGuestCode());
                } else {
                    voteData.setAuthorCode(voteData.getMemberCode());
                }
                if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HOT)) {
                    voteData.setDisplayOrder((offset) * PAGE_COUNT + i);
                    voteData.setCategory("hot");
                } else {
                    voteData.setCategory(null);
                }
                whereConditions.add(VoteDataDao.Properties.VoteCode.eq(voteData.getVoteCode()));
            }
            WhereCondition[] conditionsArray = new WhereCondition[whereConditions.size()];
            conditionsArray = whereConditions.toArray(conditionsArray);
            QueryBuilder queryBuilder = DataLoader.getInstance(context.getApplicationContext()).getVoteDataDao().queryBuilder();
            queryBuilder.whereOr(conditionsArray[0], conditionsArray[1], Arrays.copyOfRange(conditionsArray, 2, conditionsArray.length));
            queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
            DataLoader.getInstance(context.getApplicationContext()).getVoteDataDao().insertOrReplaceInTx(voteDataList);

            Log.d("test", "getVoteListResponseCallback onResponse success: offset:"+offset+" size:" + voteDataList.size());

            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(message, success, offset, voteDataList));
        }
    }

    private class LoadListDBRunnable implements Runnable {

        private boolean success;
        private String message;
        private int offset;
        private String authorCode;

        public LoadListDBRunnable(int offset, String message, boolean success) {
            this(offset, message, success, null);
        }

        public LoadListDBRunnable(int offset, String message, boolean success, String authorCode) {
            this.success = success;
            this.message = message;
            this.offset = offset;
            this.authorCode = authorCode;
        }

        @Override
        public void run() {
            List<VoteData> voteDataList = new ArrayList<>();
            if (this.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HOT)) {
                voteDataList = DataLoader.getInstance(context.getApplicationContext())
                        .queryHotVotes(offset, PAGE_COUNT);
            } else if (this.message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_NEW)) {
                voteDataList = DataLoader.getInstance(context.getApplicationContext())
                        .queryNewVotes(offset, PAGE_COUNT);
            } else if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE)) {
                voteDataList = DataLoader.getInstance(context.getApplicationContext())
                        .queryFavoriteVotes(offset, PAGE_COUNT);
            } else if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE)) {
                voteDataList = DataLoader.getInstance(context.getApplicationContext())
                        .queryVoteDataByAuthor(authorCode, offset, PAGE_COUNT);
            } else if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_PARTICIPATE)) {
                voteDataList = DataLoader.getInstance(context.getApplicationContext())
                        .queryVoteDataByParticipate(offset, PAGE_COUNT);
            }

            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(this.message
                    , this.success, this.offset, voteDataList));
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
