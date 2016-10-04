package com.android.heaton.easyvote;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

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
            data.startTime = i;
            data.endTime = i + (int) Math.random() * 5;
            data.pollCount = i;
            list.add(data);
        }
        return list;
    }

    public List<VoteData> queryHotVotes(int limit) {
        List<VoteData> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.authorName = "Heaton mock";
            data.authorIcon = "";
            data.authorCode = "heaton_code_mock";
            data.isFavorite = false;

            data.minOption = data.maxOption = 1;

            data.title = "Do your mother know what do you do?:" + i;
            data.startTime = System.currentTimeMillis() - 864200 * 1000;
            int randomImage = (int) (Math.random() * 4);
            switch (randomImage) {
                case 1:
                    data.localImage = R.mipmap.vote_finger;
                    break;
                case 2:
                    data.localImage = R.mipmap.vote_box;
                    break;
                case 3:
                    data.localImage = R.mipmap.handsup;
                    break;
                default:
                    data.localImage = R.mipmap.ballot_box;

            }

            int optionType = i % 10;
            if (optionType == 0) {
                // TWO POLL TYPE
                data.title = "TWO POLL TYPE: option 1 , top are the same.";
                data.isPolled = false;

                data.optionCount = 2;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option1count = 2;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                data.option2Count = 0;
                // option 1 and top is the same
                data.optionTopCode = "option_1_code_mock";
                data.optionTopTitle = "option 1 mock:" + i;
                data.optionTopCount = 2;

                data.pollCount = data.option1count + data.option2Count;

                data.endTime = System.currentTimeMillis() + 3600 * 1000;
            } else if (optionType == 1) {
                // TWO MORE TYPE
                data.title = "TWO MORE TYPE: option 2 , top are the same.";
                data.isPolled = false;

                data.pollCount = 5;
                data.optionCount = 3;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 title mock:" + i;
                data.option1count = 1;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title: mock" + i;
                data.option2Count = 3;
                // option 2 and top is the same
                data.optionTopCode = "option_2_code_mock";
                data.optionTopTitle = "option 2 mock:" + i;

                data.endTime = System.currentTimeMillis() + 3600 * 1000;
            } else if (optionType == 2) {
                // TWO MORE TYPE
                data.title = "TWO MORE TYPE: option 1, 2 is not the same with top.";
                data.isPolled = false;

                data.pollCount = 5;
                data.optionCount = 4;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 title mock:" + i;
                data.option1count = 2;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title mock:" + i;
                data.option2Count = 3;
                // option 1,2 is NOT the same with top.
                data.optionTopCode = "option_top_code_mock";
                data.optionTopTitle = "option top title mock:" + i;
                data.optionTopCount = 4;

                data.endTime = System.currentTimeMillis() + 3600 * 1000;
                data.optionUserChoiceCode = "";
                data.optionUserChoiceTitle = "";
                data.optionUserChoiceCount = 0;
            } else if (optionType == 3) {
                // TOP 1 TYPE.

                data.title = "TOP 1 TYPE: time end and user choiced.";
                data.isPolled = true;

                data.pollCount = 15;
                data.optionCount = 4;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option1count = 2;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                data.option2Count = 3;
                // option user choice are the same with top.
                data.optionTopCode = "option_user_code_mock";
                data.optionTopTitle = "option user mock:" + i;
                data.optionTopCount = 5;

                // time end and user choiced.
                data.endTime = System.currentTimeMillis() - 3600 * 1000;
                data.optionUserChoiceCode = "option_user_code_mock";
                data.optionUserChoiceTitle = "option user mock" + i;

            } else if (optionType == 4) {

                // TOP 1 TYPE.

                data.title = "TOP 1 TYPE: time end and user not choiced.";
                data.isPolled = false;

                data.pollCount = 15;
                data.optionCount = 4;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option1count = 2;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                data.option2Count = 3;
                // option user choice are the same with top.
                data.optionTopCode = "option_top_code_mock";
                data.optionTopTitle = "option top mock:" + i;
                data.optionTopCount = 5;

                // time end and user choiced.
                data.endTime = System.currentTimeMillis() - 3600 * 1000;
                data.optionUserChoiceCode = "";
                data.optionUserChoiceTitle = "" + i;

            } else if (optionType == 5) {
                // TOP 2 TYPE.
                data.title = "TOP 2 TYPE: time end and user choiced.";
                data.isPolled = true;

                data.pollCount = 5;
                data.optionCount = 4;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                // option user choice NOT the same with top.
                data.optionTopCode = "option_top_code_mock";
                data.optionTopTitle = "option top mock:" + i;
                data.optionTopCount = 3;

                data.endTime = System.currentTimeMillis() - 3600 * 1000;
                data.optionUserChoiceCode = "option_user_code_mock";
                data.optionUserChoiceTitle = "option_user_mock" + i;
                data.optionUserChoiceCount = 2;
            } else if (optionType == 6) {

                // TWO POLL TYPE
                data.title = "TWO POLL TYPE: no one poll";
                data.isPolled = false;

                // NO ONE POLL
                data.pollCount = 0;

                data.optionCount = 2;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                // option top is null
                data.optionTopCode = "";
                data.optionTopTitle = "";
                data.optionTopCount = 0;

                data.optionUserChoiceCode = "";
                data.optionUserChoiceTitle = "" + i;
                data.optionUserChoiceCount = 0;

                data.endTime = System.currentTimeMillis() + 3600 * 1000;
            } else if (optionType == 7) {
                // TOP 2 TYPE.
                data.title = "TOP 2 TYPE: time not end and user choiced.";
                data.isPolled = true;

                data.pollCount = 5;
                data.optionCount = 4;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                // option user choice NOT the same with top.
                data.optionTopCode = "option_top_code_mock";
                data.optionTopTitle = "option top mock:" + i;
                data.optionTopCount = 3;

                data.endTime = System.currentTimeMillis() - 3600 * 1000;
                data.optionUserChoiceCode = "option_user_code_mock";
                data.optionUserChoiceTitle = "option_user_mock" + i;
                data.optionUserChoiceCount = 2;
            } else if (optionType == 8) {
                // TOP 1 TYPE.
                data.title = "TOP 1 TYPE: time not end and user choiced.";
                data.isPolled = true;

                data.pollCount = 5;
                data.optionCount = 4;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                // option user choice NOT the same with top.
                data.optionTopCode = "option_top_code_mock";
                data.optionTopTitle = "option top mock:" + i;
                data.optionTopCount = 3;

                data.endTime = System.currentTimeMillis() + 3600 * 1000;
                data.optionUserChoiceCode = "option_top_code_mock";
                data.optionUserChoiceTitle = "option_user_code_mock" + i;
                data.optionUserChoiceCount = 2;
            }  else if (optionType == 9) {
                // TWO MORE TYPE
                data.title = "TWO MORE TYPE: option 2 , top are the same. , multi-choice";
                data.isPolled = false;

                data.pollCount = 5;
                data.optionCount = 5;
                data.option1code = "option_1_code_mock";
                data.option1title = "option 1 mock:" + i;
                data.option1count = 1;
                data.option2code = "option_2_code_mock";
                data.option2title = "option 2 title:" + i;
                data.option2Count = 3;
                // option 2 and top is the same
                data.optionTopCode = "option_2_code_mock";
                data.optionTopTitle = "option 2 mock:" + i;

                data.endTime = System.currentTimeMillis() + 3600 * 1000;

                data.minOption = data.maxOption = 2;
            }
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

    public List<Option> queryCreateOptionsByVoteId() {

        List<Option> list = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Option data = new Option();
            data.setContent("I dont want to tell my mother:" + i);
            list.add(data);
        }
        return list;
    }
}
