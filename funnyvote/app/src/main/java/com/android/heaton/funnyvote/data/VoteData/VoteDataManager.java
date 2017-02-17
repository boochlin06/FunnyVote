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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.android.heaton.funnyvote.eventbus.EventBusController.RemoteServiceEvent.FAVORITE_VOTE;
import static com.android.heaton.funnyvote.eventbus.EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE;
import static com.android.heaton.funnyvote.eventbus.EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE;

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
                    EventBusController.RemoteServiceEvent.CREATE_VOTE, false, new IllegalArgumentException().toString()));
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

    public void pollVote(@NonNull String voteCode, String password, @NonNull List<String> pollOptions, @NonNull User user) {
        if (TextUtils.isEmpty(voteCode) || pollOptions.size() == 0 || user == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.POLL_VOTE, false, new IllegalArgumentException().toString()));
        } else {
            remoteServiceApi.pollVote(voteCode, password, pollOptions, user, new pollVoteResponseCallback());
        }
    }

    public void addNewOption(@NonNull String voteCode, String password, @NonNull List<String> newOptions, @NonNull User user) {
        if (TextUtils.isEmpty(voteCode) || newOptions.size() == 0 || user == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.ADD_NEW_OPTION, false, new IllegalArgumentException().toString()));
        } else {
            remoteServiceApi.addNewOption(voteCode, password, newOptions, user, new addNewOptionResponseCallback());
        }
    }

    public void favoriteVote(@NonNull String voteCode, boolean isFavorite, @NonNull User user) {
        if (TextUtils.isEmpty(voteCode) || user == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    FAVORITE_VOTE, false, new IllegalArgumentException().toString()));
        } else {
            remoteServiceApi.favoriteVote(voteCode, isFavorite, user, new favoriteVoteResponseCallback(voteCode, isFavorite));
        }
    }

    public void getVoteList(int offset, String eventMessage, @NonNull User user) {
        if (TextUtils.isEmpty(eventMessage) || user == null) {
            executorService.execute(new LoadListDBRunnable(offset,eventMessage,false));
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

    public void getUserFavoriteVoteList(int offset, @NonNull User loginUser, User targetUser) {
        if (targetUser == null) {
            getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE, loginUser);
        } else {
            getPersonalFavoriteVoteList(offset, loginUser, targetUser);
        }
    }

    public void getUserCreateVoteList(int offset, @NonNull User loginUser, User targetUser) {
        if (targetUser == null) {
            getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE, loginUser);
        } else {
            getPersonalCreateVoteList(offset, loginUser, targetUser);
        }
    }

    public void getUserParticipateVoteList(int offset, @NonNull User user) {
        getVoteList(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_PARTICIPATE, user);
    }

    public void getSearchVoteList(String keyword, int offset, @NonNull User user) {
        if (TextUtils.isEmpty(keyword) || user == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.GET_VOTE_LIST_SEARCH
                    , false, new IllegalArgumentException().toString()));
        } else {
            int pageNumber = (int) offset / PAGE_COUNT;
            int pageCount = PAGE_COUNT;
            remoteServiceApi.getSearchVoteList(keyword, pageNumber, pageCount, user
                    , new getVoteListResponseCallback(offset, EventBusController.RemoteServiceEvent.GET_VOTE_LIST_SEARCH
                            , user.getUserCode()));
        }
    }

    public void getPersonalCreateVoteList(int offset, @NonNull User loginUser, User targetUser) {
        if (loginUser == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HISTORY_CREATE
                    , false, new IllegalArgumentException().toString()));
        } else {
            int pageNumber = (int) offset / PAGE_COUNT;
            int pageCount = PAGE_COUNT;
            remoteServiceApi.getPersonalCreateVoteList(pageNumber, pageCount, loginUser, targetUser
                    , new getVoteListResponseCallback(offset, GET_VOTE_LIST_HISTORY_CREATE
                            , targetUser.getUserCode(), false));
        }
    }

    public void getPersonalFavoriteVoteList(int offset, @NonNull User loginUser, User targetUser) {
        if (loginUser == null) {
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.GET_VOTE_LIST_FAVORITE
                    , false, new IllegalArgumentException().toString()));
        } else {
            int pageNumber = (int) offset / PAGE_COUNT;
            int pageCount = PAGE_COUNT;
            remoteServiceApi.getPersonalFavoriteVoteList(pageNumber, pageCount, loginUser, targetUser
                    , new getVoteListResponseCallback(offset, GET_VOTE_LIST_FAVORITE
                            , targetUser.getUserCode(), false));
        }
    }

    public class createVoteResponseCallback implements Callback<VoteData> {

        public createVoteResponseCallback() {
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadDBRunnable(response.body(), EventBusController
                        .RemoteServiceEvent.CREATE_VOTE, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "createVoteResponseCallback onResponse false, error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.CREATE_VOTE, false, errorMessage));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "createVoteResponseCallback onResponse onFailure, error message:" + t.getMessage());
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.CREATE_VOTE, false, t.getMessage()));
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
                    Log.e(TAG, "getVoteResponseCallback onResponse false, vote code:" + voteCode
                            + ", error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executorService.execute(new LoadDBRunnable(voteCode, EventBusController
                        .RemoteServiceEvent.GET_VOTE, false));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "getVoteResponseCallback onResponse onFailure, vote code:" + voteCode
                    + ", error message:" + t.getMessage());
            executorService.execute(new LoadDBRunnable(voteCode, EventBusController
                    .RemoteServiceEvent.GET_VOTE, false));
        }
    }

    public class getVoteListResponseCallback implements Callback<List<VoteData>> {

        private String message;
        private int offset;
        private String userCode;
        private boolean isLoginUser = true;

        public getVoteListResponseCallback(int offset, String message) {
            this(offset, message, null);
        }

        public getVoteListResponseCallback(int offset, String message, String userCode) {
            this.message = message;
            this.offset = offset;
            this.userCode = userCode;
            this.isLoginUser = true;
        }

        public getVoteListResponseCallback(int offset, String message, String userCode, boolean isLoginUser) {
            this.message = message;
            this.offset = offset;
            this.userCode = userCode;
            this.isLoginUser = isLoginUser;
        }

        @Override
        public void onResponse(Call<List<VoteData>> call, Response<List<VoteData>> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadListDBRunnable(response.body(), offset
                        , message, true, isLoginUser));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "getVoteListResponseCallback onResponse false, message" + message + "," + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executorService.execute(new LoadListDBRunnable(offset, message, false, userCode
                        , errorMessage, isLoginUser));
            }
        }

        @Override
        public void onFailure(Call<List<VoteData>> call, Throwable t) {
            Log.e(TAG, "getVoteListResponseCallback onFailure:" + t.getMessage() + " message:" + this.message);
            executorService.execute(new LoadListDBRunnable(offset, message, false, userCode
                    , t.getMessage(), isLoginUser));
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
                    Log.e(TAG, "pollVoteResponseCallback onResponse false , error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.POLL_VOTE, false, errorMessage));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "pollVoteResponseCallback onFailure , error message:" + t.getMessage());
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.POLL_VOTE, false, t.getMessage()));
        }
    }

    public class addNewOptionResponseCallback implements Callback<VoteData> {

        public addNewOptionResponseCallback() {
        }

        @Override
        public void onResponse(Call<VoteData> call, Response<VoteData> response) {
            if (response.isSuccessful()) {
                executorService.execute(new SaveAndLoadDBRunnable(response.body(), EventBusController
                        .RemoteServiceEvent.ADD_NEW_OPTION, true));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "addNewOptionResponseCallback onResponse false , error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                        EventBusController.RemoteServiceEvent.ADD_NEW_OPTION, false, errorMessage));
            }
        }

        @Override
        public void onFailure(Call<VoteData> call, Throwable t) {
            Log.e(TAG, "addNewOptionResponseCallback onFailure , error message:" + t.getMessage());
            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(
                    EventBusController.RemoteServiceEvent.ADD_NEW_OPTION, false, t.getMessage()));
        }
    }

    public class favoriteVoteResponseCallback implements Callback<ResponseBody> {
        private VoteData voteData;

        public favoriteVoteResponseCallback(String voteCode, boolean isFavorite) {
            voteData = new VoteData();
            voteData.setIsFavorite(isFavorite);
            voteData.setVoteCode(voteCode);
        }

        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                DataLoader.getInstance(context.getApplicationContext())
                        .updateVoteByVoteCode(voteData.getVoteCode(), this.voteData);
                EventBus.getDefault().post(new EventBusController
                        .RemoteServiceEvent(FAVORITE_VOTE, true, this.voteData, null));
            } else {
                String errorMessage = "";
                try {
                    errorMessage = response.errorBody().string();
                    Log.e(TAG, "favoriteVoteResponseCallback onResponse false, error message:" + errorMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                EventBus.getDefault().post(new EventBusController
                        .RemoteServiceEvent(FAVORITE_VOTE, false, voteData, null));
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Log.e(TAG, "favoriteVoteResponseCallback onFailure , error message:" + t.getMessage());
            EventBus.getDefault().post(new EventBusController
                    .RemoteServiceEvent(FAVORITE_VOTE, false, voteData, null));
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
                    voteSetting.setOption1Polled(option.getIsUserChoiced());
                } else if (i == 1) {
                    voteSetting.setOption2Title(option.getTitle());
                    voteSetting.setOption2Code(option.getCode());
                    voteSetting.setOption2Count(option.getCount());
                    voteSetting.setOption2Polled(option.getIsUserChoiced());
                }
                if (option.getCount() > maxOption && option.getCount() >= 1) {
                    maxOption = option.getCount();
                    voteSetting.setOptionTopCount(option.getCount());
                    voteSetting.setOptionTopCode(option.getCode());
                    voteSetting.setOptionTopTitle(option.getTitle());
                    voteSetting.setOptionTopPolled(option.getIsUserChoiced());
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
        }
    }

    private class SaveAndLoadListDBRunnable implements Runnable {
        private List<VoteData> voteDataList;
        private String message;
        private boolean success;
        private int offset;
        private boolean isLoginUser;

        public SaveAndLoadListDBRunnable(List<VoteData> voteDataList
                , int offset, String message, boolean success, boolean isLoginUser) {
            this.message = message;
            this.success = success;
            this.voteDataList = voteDataList;
            this.offset = offset;
            this.isLoginUser = isLoginUser;
        }

        @Override
        public void run() {
            ArrayList<WhereCondition> whereConditions = new ArrayList<WhereCondition>();
            List<String> favVoteCodeList = new ArrayList<>();
            if (!isLoginUser) {
                // todo : fix the favorite is not for login user by backend
                List<VoteData> favs = DataLoader.getInstance(context.getApplicationContext())
                        .queryFavoriteVotes(0, 100);
                for (int i = 0; i < favs.size(); i++) {
                    favVoteCodeList.add(favs.get(i).getVoteCode());
                }
            }
            for (int i = 0; i < voteDataList.size(); i++) {
                VoteData voteData = voteDataList.get(i);
                if (voteData.getFirstOption() != null) {
                    voteData.setOption1Code(voteData.getFirstOption().getCode());
                    voteData.setOption1Title(voteData.getFirstOption().getTitle());
                    voteData.setOption1Count(voteData.getFirstOption().getCount());
                    voteData.setOption1Polled(voteData.getFirstOption().getIsUserChoiced());
                }
                if (voteData.getSecondOption() != null) {
                    voteData.setOption2Code(voteData.getSecondOption().getCode());
                    voteData.setOption2Title(voteData.getSecondOption().getTitle());
                    voteData.setOption2Count(voteData.getSecondOption().getCount());
                    voteData.setOption2Polled(voteData.getSecondOption().getIsUserChoiced());
                }
                if (voteData.getTopOption() != null) {
                    voteData.setOptionTopCode(voteData.getTopOption().getCode());
                    voteData.setOptionTopTitle(voteData.getTopOption().getTitle());
                    voteData.setOptionTopCount(voteData.getTopOption().getCount());
                    voteData.setOptionTopPolled(voteData.getTopOption().getIsUserChoiced());
                }
                if (voteData.getUserOption() != null) {
                    voteData.setOptionUserChoiceCode(voteData.getUserOption().getCode());
                    voteData.setOptionUserChoiceTitle(voteData.getUserOption().getTitle());
                    voteData.setOptionUserChoiceCount(voteData.getUserOption().getCount());
                }
                if (message.equals(EventBusController.RemoteServiceEvent.GET_VOTE_LIST_HOT)) {
                    voteData.setDisplayOrder((offset) * PAGE_COUNT + i);
                    voteData.setCategory("hot");
                } else {
                    voteData.setCategory(null);
                }
                if (!isLoginUser) {
                    //todo: temp reset fav for login user
                    voteData.setIsFavorite(favVoteCodeList.contains(voteData.getVoteCode()));
                }
                whereConditions.add(VoteDataDao.Properties.VoteCode.eq(voteData.getVoteCode()));
            }
            WhereCondition[] conditionsArray = new WhereCondition[whereConditions.size()];
            conditionsArray = whereConditions.toArray(conditionsArray);
            QueryBuilder queryBuilder = DataLoader.getInstance(context.getApplicationContext()).getVoteDataDao().queryBuilder();
            if (conditionsArray.length > 2) {
                queryBuilder.whereOr(conditionsArray[0], conditionsArray[1], Arrays.copyOfRange(conditionsArray
                        , 2, conditionsArray.length));
            } else if (conditionsArray.length == 2) {
                queryBuilder.whereOr(conditionsArray[0], conditionsArray[1]);
            } else if (conditionsArray.length == 1) {
                queryBuilder.where(conditionsArray[0]);
            }
            queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities();
            DataLoader.getInstance(context.getApplicationContext()).getVoteDataDao().insertOrReplaceInTx(voteDataList);

            Log.d(TAG, "getVoteListResponseCallback onResponse message:" + message
                    + " , success: true , SaveAndLoadListDBRunnable offset:"
                    + offset + ", size:" + voteDataList.size());

            EventBus.getDefault().post(new EventBusController.RemoteServiceEvent(message, success, offset, voteDataList));
        }
    }

    private class LoadListDBRunnable implements Runnable {

        private boolean success;
        private String message;
        private int offset;
        private String authorCode;
        private String errorResponse;
        private boolean isLoginUser;

        public LoadListDBRunnable(int offset, String message, boolean success) {
            this(offset, message, success, null, null, true);
        }

        public LoadListDBRunnable(int offset, String message, boolean success, String authorCode
                , String errorResponse, boolean isLoginUser) {
            this.success = success;
            this.message = message;
            this.offset = offset;
            this.authorCode = authorCode;
            this.errorResponse = errorResponse;
            this.isLoginUser = isLoginUser;
        }

        @Override
        public void run() {
            List<VoteData> voteDataList = new ArrayList<>();
            if (isLoginUser) {
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
            }
            EventBusController.RemoteServiceEvent event = new EventBusController.RemoteServiceEvent(this.message
                    , this.success, this.offset, voteDataList);
            event.errorResponseMessage = errorResponse;
            Log.d(TAG, "getVoteListResponseCallback onResponse message:" + message
                    + " , success: false " + ", error message" + errorResponse
                    + ",LoadListDBRunnable offset:" + offset + ", size:" + voteDataList.size());
            EventBus.getDefault().post(event);
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
