package com.android.heaton.easyvote;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 16/4/7.
 */
public class VoteDataLoader {
    private static VoteDataLoader sInstance;
    private Context mContext;

    public static synchronized VoteDataLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VoteDataLoader(context);
        }
        return sInstance;
    }

    public VoteDataLoader(Context context) {
        mContext = context;
    }

    public List<VoteData> queryCreateByVotes(int limit) {
        List<VoteData> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.title = "Do your mother know what do you do?:" + i;
            data.description = " test description";
            data.startTime = i;
            data.endTime = i + (int) Math.random() * 5;
            data.humanCount = i;
            list.add(data);
        }
        return list;
    }

    public List<VoteData> queryHotVotes(int limit) {
        List<VoteData> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.title = "Do your mother know what do you do?:" + i;
            data.description = " test description";
            data.startTime = i;
            data.endTime = i + (int) Math.random() * 5;
            data.humanCount = i;
            list.add(data);
        }
        return list;
    }

    public List<Option> queryOptionsByVoteId(int voteId) {

        List<Option> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Option data = new Option();
            data.setContent("I dont want to tell my mother:" + i);
            list.add(data);
        }
        return list;
    }
}
