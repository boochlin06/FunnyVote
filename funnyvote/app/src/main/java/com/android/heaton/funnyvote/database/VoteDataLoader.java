package com.android.heaton.funnyvote.database;

import android.content.Context;

import com.android.heaton.funnyvote.FunnyVoteApplication;
import com.android.heaton.funnyvote.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 16/4/7.
 */
public class VoteDataLoader {
    private static VoteDataLoader sInstance;
    private VoteDataDao voteDataDao;
    private OptionDao optionDao;

    public VoteDataLoader(Context context) {
        voteDataDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getVoteDataDao();
        optionDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getOptionDao();
    }

    public static synchronized VoteDataLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VoteDataLoader(context);
        }
        return sInstance;
    }

    public List<Option> loadAllOption() {
        return optionDao.loadAll();
    }


    public List<Option> mockOptionData(VoteData data, int maxOptionCount, int optionTop
            , int optionUserChoice) {
        List<Option> options = new ArrayList<>();
        int pollCount = 0;
        int topCount = maxOptionCount + 1;
        for (int i = 0; i < data.getOptionCount(); i++) {
            Option option = new Option();
            int randomOptionCount = (int) (Math.random() * maxOptionCount);
            if (maxOptionCount == 0) {
                randomOptionCount = 0;
                topCount = 0;
            }
            option.setVoteCode(data.getVoteCode());
            if (i == 0) {
                data.setOption1Count(randomOptionCount);
                option.setTitle(data.getOption1Title());
                option.setCode(data.getOption1Code());
                option.setCount(data.getOption1Count());
            } else if (i == 1) {
                data.setOption2Count(randomOptionCount);
                option.setTitle(data.getOption2Title());
                option.setCode(data.getOption2Code());
                option.setCount(data.getOption2Count());
            } else {
                option.setTitle("Option mock :" + data.getVoteCode() + "_" + i);
                option.setCount(randomOptionCount);
                option.setCode(data.getVoteCode() + "_" + i);
            }
            if (optionTop == i) {
                if (optionTop == 0) {
                    data.setOption1Count(topCount);
                } else if (optionTop == 1) {
                    data.setOption2Count(topCount);
                }
                option.setCount(topCount);
                data.setOptionTopTitle(option.getTitle());
                data.setOptionTopCode(option.getCode());
                data.setOptionTopCount(topCount);

            }
            if (optionUserChoice == i) {
                data.setOptionUserChoiceCode(option.getCode());
                data.setOptionUserChoiceTitle(option.getTitle());
                data.setOptionUserChoiceCount(option.getCount());
            }
            pollCount = option.getCount() + pollCount;
            options.add(option);
        }
        data.setPollCount(pollCount);
        return options;
    }

    public void mockData(int limit, int maxOptionCount) {
        List<VoteData> list = new ArrayList<>();
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.setIsCanPreviewResult(true);
            data.setVoteCode(System.currentTimeMillis() + "_" + Integer.toString(i));
            data.setAuthorName("Heaton mock" + i);
            data.setAuthorCode("Author_" + i);
            data.setAuthorIcon("");

            data.setIsFavorite(false);
            data.setIsNeedPassword(false);
            data.setIsPolled(false);
            data.setCategory("hot");

            data.setMaxOption(1);
            data.setMinOption(1);
            data.setIsUserCanAddOption(true);

            data.setSecurity("public");
            data.setTitle("Do your mother know what do you do?:" + i);
            data.setStartTime(System.currentTimeMillis() - 864200 * 1000);
            int randomImage = (int) (Math.random() * 4);
            int image;
            switch (randomImage) {
                case 1:
                    image = R.mipmap.vote_finger;
                    break;
                case 2:
                    image = R.mipmap.vote_box;
                    break;
                case 3:
                    image = R.mipmap.handsup;
                    break;
                default:
                    image = R.mipmap.ballot_box;

            }
            data.setLocalImage(image);

            int optionType = i % 11;
            if (optionType == 0) {
                // TWO POLL TYPE
                data.setTitle("TWO POLL TYPE: option 1 , top are the same.");
                data.setIsPolled(false);
                data.setOptionCount(2);

                data.setOption1Code(i + "" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option 1 and top is the same
                data.setOptionTopCode(i + "" + 0);
                data.setOptionTopTitle("option 1 mock:" + i);

                data.setIsUserCanAddOption(false);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);
                options.addAll(mockOptionData(data, maxOptionCount, 0, -1));
            } else if (optionType == 1) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 2 , top are the same.");
                data.setIsPolled(false);

                data.setOptionCount(3);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option 2 and top is the same
                data.setOptionTopCode(i + "_" + 2);
                data.setOptionTopTitle("option 2 title:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 1, -1));
            } else if (optionType == 2) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 1, 2 is not the same with top. top is 4");
                data.setIsPolled(false);

                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title :" + i);
                // option 1,2 is NOT the same with top.
                data.setOptionTopTitle("option top title mock:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 3, -1));
            } else if (optionType == 3) {
                // TOP 1 TYPE.

                data.setTitle("TOP 1 TYPE: time end and user choiced option that not 1 and 2 , is 5, top is 4");
                data.setIsPolled(true);

                data.setOptionCount(15);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice are the same with top.
                data.setOptionTopCode("option_user_code_mock");
                data.setOptionTopTitle("option user mock:" + i);

                // time end and user choiced option that not 1 and 2.
                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);
                data.setOptionUserChoiceCode("option_user_code_mock");
                data.setOptionUserChoiceTitle("option user mock" + i);

                options.addAll(mockOptionData(data, maxOptionCount, 3, 4));

            } else if (optionType == 4) {

                // TOP 1 TYPE.

                data.setTitle("TOP 1 TYPE: time end and user not choiced.top is 3");
                data.setIsPolled(false);

                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);

                // time end and user choiced.
                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 2, -1));
            } else if (optionType == 5) {
                // TOP 2 TYPE.
                data.setTitle("TOP 2 TYPE: time end and user choiced. user choice is 4 , top is 3");
                data.setIsPolled(true);

                data.setOptionCount(5);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice NOT the same with top.
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);

                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);
                options.addAll(mockOptionData(data, maxOptionCount, 2, 3));
            } else if (optionType == 6) {

                // TWO POLL TYPE
                data.setTitle("TWO POLL TYPE: no one poll , time is not end");
                data.setIsPolled(false);

                // NO ONE POLL
                data.setPollCount(0);

                data.setOptionCount(2);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option top is null

                options.addAll(mockOptionData(data, 0, -1, -1));

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);
            } else if (optionType == 7) {
                // TOP 2 TYPE.
                data.setTitle("TOP 2 TYPE: time not end and user choiced. user choice is 1 , top is 2");
                data.setIsPolled(true);

                data.setPollCount(5);
                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice NOT the same with top.
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);
                data.setOptionTopCount(3);

                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 1, 0));
            } else if (optionType == 8) {
                // TOP 1 TYPE.
                data.setTitle("TOP 1 TYPE: time not end and user choiced. user choice and top is 3");
                data.setIsPolled(true);

                data.setPollCount(5);
                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice NOT the same with top.
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);
                data.setOptionTopCount(3);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 2, 2));
            } else if (optionType == 9) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 2 , top is 3. , multi-choice");
                data.setIsPolled(false);

                data.setPollCount(5);
                data.setOptionCount(5);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption1Count(1);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                data.setOption2Count(3);
                // option 2 and top is the same
                data.setOptionTopCode(i + "_" + 2);
                data.setOptionTopTitle("option 2 mock:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                data.setMinOption(2);
                data.setMaxOption(2);

                options.addAll(mockOptionData(data, maxOptionCount, 2, -1));
            } else if (optionType == 10) {
                // ONE POLL TYPE
                data.setTitle("ONE POLL TYPE: no one poll , tiem is end");
                data.setIsPolled(false);

                // NO ONE POLL
                data.setPollCount(0);

                data.setOptionCount(2);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);

                options.addAll(mockOptionData(data, 0, -1, -1));

                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);
            }
            list.add(data);
        }
        voteDataDao.insertInTx(list);
        optionDao.insertInTx(options);
    }

    public List<VoteData> queryCreateByVotes(int limit) {
        List<VoteData> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.setTitle("Do your mother know what do you do?:" + i);
            data.setPollCount(i);
            list.add(data);
        }
        return list;
    }

    public List<VoteData> queryHotVotes(int limit) {
        return voteDataDao.queryBuilder().limit(limit).list();
    }

    public List<Option> queryOptionsByVoteId(String voteCode) {
        return optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteCode)).list();
    }

    public VoteData queryVoteDataById(String code) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.VoteCode.eq(code)).list().get(0);
    }

}
