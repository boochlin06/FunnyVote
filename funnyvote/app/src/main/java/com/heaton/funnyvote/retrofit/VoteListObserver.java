package com.heaton.funnyvote.retrofit;

import com.heaton.funnyvote.database.VoteData;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.DisposableObserver;

public abstract class VoteListObserver<T> extends DisposableObserver<List<VoteData>> {
    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        if (e != null && ("error_no_poll_event".equals(e.getMessage()) ||
                (e.getMessage() != null && e.getMessage().contains("error_no_poll_event")))) {
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
