package com.heaton.funnyvote.retrofit;

import android.text.TextUtils;

import com.heaton.funnyvote.database.VoteData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import rx.Observer;

public abstract class VoteListObserver<T> implements Observer<List<VoteData>> {
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        String errorMessage = "";
        if (e instanceof HttpException) {
            ResponseBody body = ((HttpException) e).response().errorBody();
            try {
                errorMessage = body.string();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(errorMessage) && errorMessage.equals("error_no_poll_event")) {
            onVoteListLoaded(new ArrayList<VoteData>());
        } else {
            onVoteListNotAvailable(e);
        }
    }

    @Override
    public void onNext(List<VoteData> voteDataList) {
        onVoteListLoaded(voteDataList);
    }

    public abstract void onVoteListNotAvailable(Throwable e);

    public abstract void onVoteListLoaded(List<VoteData> voteDataList);


}
